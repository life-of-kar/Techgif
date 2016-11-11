package org.telegram.ui.Components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.StickersActivity.TouchHelperCallback;

public class CheckBox extends View {
    private static Paint backgroundPaint = null;
    private static Paint checkPaint = null;
    private static Paint eraser = null;
    private static Paint eraser2 = null;
    private static Paint paint = null;
    private static final float progressBounceDiff = 0.2f;
    private boolean attachedToWindow;
    private Canvas bitmapCanvas;
    private ObjectAnimator checkAnimator;
    private Bitmap checkBitmap;
    private Canvas checkCanvas;
    private Drawable checkDrawable;
    private int checkOffset;
    private int color;
    private boolean drawBackground;
    private Bitmap drawBitmap;
    private boolean isCheckAnimation;
    private boolean isChecked;
    private float progress;
    private int size;

    public CheckBox(Context context, int resId) {
        super(context);
        this.isCheckAnimation = true;
        this.size = 22;
        this.color = -10567099;
        if (paint == null) {
            paint = new Paint(1);
            eraser = new Paint(1);
            eraser.setColor(0);
            eraser.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            eraser2 = new Paint(1);
            eraser2.setColor(0);
            eraser2.setStyle(Style.STROKE);
            eraser2.setStrokeWidth((float) AndroidUtilities.dp(28.0f));
            eraser2.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
            backgroundPaint = new Paint(1);
            backgroundPaint.setColor(-1);
            backgroundPaint.setStyle(Style.STROKE);
            backgroundPaint.setStrokeWidth((float) AndroidUtilities.dp(2.0f));
        }
        this.checkDrawable = context.getResources().getDrawable(resId);
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 0 && this.drawBitmap == null) {
            this.drawBitmap = Bitmap.createBitmap(AndroidUtilities.dp((float) this.size), AndroidUtilities.dp((float) this.size), Config.ARGB_4444);
            this.bitmapCanvas = new Canvas(this.drawBitmap);
            this.checkBitmap = Bitmap.createBitmap(AndroidUtilities.dp((float) this.size), AndroidUtilities.dp((float) this.size), Config.ARGB_4444);
            this.checkCanvas = new Canvas(this.checkBitmap);
        }
    }

    public void setProgress(float value) {
        if (this.progress != value) {
            this.progress = value;
            invalidate();
        }
    }

    public void setDrawBackground(boolean value) {
        this.drawBackground = value;
    }

    public void setCheckOffset(int value) {
        this.checkOffset = value;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setColor(int value) {
        this.color = value;
    }

    private void cancelCheckAnimator() {
        if (this.checkAnimator != null) {
            this.checkAnimator.cancel();
        }
    }

    private void animateToCheckedState(boolean newCheckedState) {
        this.isCheckAnimation = newCheckedState;
        String str = NotificationCompatApi21.CATEGORY_PROGRESS;
        float[] fArr = new float[1];
        fArr[0] = newCheckedState ? TouchHelperCallback.ALPHA_FULL : 0.0f;
        this.checkAnimator = ObjectAnimator.ofFloat(this, str, fArr);
        this.checkAnimator.setDuration(300);
        this.checkAnimator.start();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.attachedToWindow = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.attachedToWindow = false;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checked != this.isChecked) {
            this.isChecked = checked;
            if (this.attachedToWindow && animated) {
                animateToCheckedState(checked);
                return;
            }
            cancelCheckAnimator();
            setProgress(checked ? TouchHelperCallback.ALPHA_FULL : 0.0f);
        }
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    protected void onDraw(Canvas canvas) {
        if (getVisibility() == 0) {
            if (this.drawBackground || this.progress != 0.0f) {
                eraser2.setStrokeWidth((float) AndroidUtilities.dp((float) (this.size + 6)));
                this.drawBitmap.eraseColor(0);
                float rad = (float) (getMeasuredWidth() / 2);
                float roundProgress = this.progress >= 0.5f ? TouchHelperCallback.ALPHA_FULL : this.progress / 0.5f;
                float checkProgress = this.progress < 0.5f ? 0.0f : (this.progress - 0.5f) / 0.5f;
                float roundProgressCheckState = this.isCheckAnimation ? this.progress : TouchHelperCallback.ALPHA_FULL - this.progress;
                if (roundProgressCheckState < progressBounceDiff) {
                    rad -= (((float) AndroidUtilities.dp(2.0f)) * roundProgressCheckState) / progressBounceDiff;
                } else if (roundProgressCheckState < 0.4f) {
                    rad -= ((float) AndroidUtilities.dp(2.0f)) - ((((float) AndroidUtilities.dp(2.0f)) * (roundProgressCheckState - progressBounceDiff)) / progressBounceDiff);
                }
                if (this.drawBackground) {
                    paint.setColor(1140850688);
                    canvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), rad - ((float) AndroidUtilities.dp(TouchHelperCallback.ALPHA_FULL)), paint);
                    canvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), rad - ((float) AndroidUtilities.dp(TouchHelperCallback.ALPHA_FULL)), backgroundPaint);
                }
                paint.setColor(this.color);
                this.bitmapCanvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), rad, paint);
                this.bitmapCanvas.drawCircle((float) (getMeasuredWidth() / 2), (float) (getMeasuredHeight() / 2), (TouchHelperCallback.ALPHA_FULL - roundProgress) * rad, eraser);
                canvas.drawBitmap(this.drawBitmap, 0.0f, 0.0f, null);
                this.checkBitmap.eraseColor(0);
                int w = this.checkDrawable.getIntrinsicWidth();
                int h = this.checkDrawable.getIntrinsicHeight();
                int x = (getMeasuredWidth() - w) / 2;
                int y = (getMeasuredHeight() - h) / 2;
                this.checkDrawable.setBounds(x, this.checkOffset + y, x + w, (y + h) + this.checkOffset);
                this.checkDrawable.draw(this.checkCanvas);
                this.checkCanvas.drawCircle((float) ((getMeasuredWidth() / 2) - AndroidUtilities.dp(2.5f)), (float) ((getMeasuredHeight() / 2) + AndroidUtilities.dp(4.0f)), ((float) ((getMeasuredWidth() + AndroidUtilities.dp(6.0f)) / 2)) * (TouchHelperCallback.ALPHA_FULL - checkProgress), eraser2);
                canvas.drawBitmap(this.checkBitmap, 0.0f, 0.0f, null);
            }
        }
    }
}
