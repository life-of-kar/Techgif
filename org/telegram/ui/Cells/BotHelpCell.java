package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewCompat;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.exoplayer.C0747C;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.TypefaceSpan;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.StickersActivity.TouchHelperCallback;

public class BotHelpCell extends View {
    private BotHelpCellDelegate delegate;
    private int height;
    private String oldText;
    private ClickableSpan pressedLink;
    private StaticLayout textLayout;
    private TextPaint textPaint;
    private int textX;
    private int textY;
    private Paint urlPaint;
    private LinkPath urlPath;
    private int width;

    public interface BotHelpCellDelegate {
        void didPressUrl(String str);
    }

    public BotHelpCell(Context context) {
        super(context);
        this.urlPath = new LinkPath();
        this.textPaint = new TextPaint(1);
        this.textPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
        this.textPaint.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.textPaint.linkColor = Theme.MSG_LINK_TEXT_COLOR;
        this.urlPaint = new Paint();
        this.urlPaint.setColor(Theme.MSG_LINK_SELECT_BACKGROUND_COLOR);
    }

    public void setDelegate(BotHelpCellDelegate botHelpCellDelegate) {
        this.delegate = botHelpCellDelegate;
    }

    private void resetPressedLink() {
        if (this.pressedLink != null) {
            this.pressedLink = null;
        }
        invalidate();
    }

    public void setText(String text) {
        if (text == null || text.length() == 0) {
            setVisibility(8);
        } else if (text == null || this.oldText == null || !text.equals(this.oldText)) {
            this.oldText = text;
            setVisibility(0);
            if (AndroidUtilities.isTablet()) {
                this.width = (int) (((float) AndroidUtilities.getMinTabletSide()) * 0.7f);
            } else {
                this.width = (int) (((float) Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) * 0.7f);
            }
            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
            String help = LocaleController.getString("BotInfoTitle", C0691R.string.BotInfoTitle);
            stringBuilder.append(help);
            stringBuilder.append("\n\n");
            stringBuilder.append(text);
            MessageObject.addLinks(stringBuilder);
            stringBuilder.setSpan(new TypefaceSpan(AndroidUtilities.getTypeface("fonts/rmedium.ttf")), 0, help.length(), 33);
            Emoji.replaceEmoji(stringBuilder, this.textPaint.getFontMetricsInt(), AndroidUtilities.dp(20.0f), false);
            try {
                this.textLayout = new StaticLayout(stringBuilder, this.textPaint, this.width, Alignment.ALIGN_NORMAL, TouchHelperCallback.ALPHA_FULL, 0.0f, false);
                this.width = 0;
                this.height = this.textLayout.getHeight() + AndroidUtilities.dp(22.0f);
                int count = this.textLayout.getLineCount();
                for (int a = 0; a < count; a++) {
                    this.width = (int) Math.ceil((double) Math.max((float) this.width, this.textLayout.getLineWidth(a) + this.textLayout.getLineLeft(a)));
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessage", e);
            }
            this.width += AndroidUtilities.dp(22.0f);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        boolean result = false;
        if (this.textLayout != null) {
            if (event.getAction() == 0 || (this.pressedLink != null && event.getAction() == 1)) {
                if (event.getAction() == 0) {
                    resetPressedLink();
                    try {
                        int x2 = (int) (x - ((float) this.textX));
                        int line = this.textLayout.getLineForVertical((int) (y - ((float) this.textY)));
                        int off = this.textLayout.getOffsetForHorizontal(line, (float) x2);
                        float left = this.textLayout.getLineLeft(line);
                        if (left > ((float) x2) || this.textLayout.getLineWidth(line) + left < ((float) x2)) {
                            resetPressedLink();
                        } else {
                            Spannable buffer = (Spannable) this.textLayout.getText();
                            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
                            if (link.length != 0) {
                                resetPressedLink();
                                this.pressedLink = link[0];
                                result = true;
                                try {
                                    int start = buffer.getSpanStart(this.pressedLink);
                                    this.urlPath.setCurrentLayout(this.textLayout, start, 0.0f);
                                    this.textLayout.getSelectionPath(start, buffer.getSpanEnd(this.pressedLink), this.urlPath);
                                } catch (Throwable e) {
                                    FileLog.m13e("tmessages", e);
                                }
                            } else {
                                resetPressedLink();
                            }
                        }
                    } catch (Throwable e2) {
                        resetPressedLink();
                        FileLog.m13e("tmessages", e2);
                    }
                } else if (this.pressedLink != null) {
                    try {
                        if (this.pressedLink instanceof URLSpanNoUnderline) {
                            String url = ((URLSpanNoUnderline) this.pressedLink).getURL();
                            if ((url.startsWith("@") || url.startsWith("#") || url.startsWith("/")) && this.delegate != null) {
                                this.delegate.didPressUrl(url);
                            }
                        } else if (this.pressedLink instanceof URLSpan) {
                            Browser.openUrl(getContext(), ((URLSpan) this.pressedLink).getURL());
                        } else {
                            this.pressedLink.onClick(this);
                        }
                    } catch (Throwable e22) {
                        FileLog.m13e("tmessages", e22);
                    }
                    resetPressedLink();
                    result = true;
                }
            } else if (event.getAction() == 3) {
                resetPressedLink();
            }
        }
        if (result || super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), C0747C.ENCODING_PCM_32BIT), this.height + AndroidUtilities.dp(8.0f));
    }

    protected void onDraw(Canvas canvas) {
        int x = (canvas.getWidth() - this.width) / 2;
        int y = AndroidUtilities.dp(4.0f);
        Theme.backgroundMediaDrawableIn.setBounds(x, y, this.width + x, this.height + y);
        Theme.backgroundMediaDrawableIn.draw(canvas);
        canvas.save();
        int dp = AndroidUtilities.dp(11.0f) + x;
        this.textX = dp;
        float f = (float) dp;
        int dp2 = AndroidUtilities.dp(11.0f) + y;
        this.textY = dp2;
        canvas.translate(f, (float) dp2);
        if (this.pressedLink != null) {
            canvas.drawPath(this.urlPath, this.urlPaint);
        }
        if (this.textLayout != null) {
            this.textLayout.draw(canvas);
        }
        canvas.restore();
    }
}
