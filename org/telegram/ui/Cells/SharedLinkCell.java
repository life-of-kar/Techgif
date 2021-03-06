package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.Locale;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.exoplayer.C0747C;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.MessageEntity;
import org.telegram.tgnet.TLRPC.PhotoSize;
import org.telegram.tgnet.TLRPC.TL_messageEntityEmail;
import org.telegram.tgnet.TLRPC.TL_messageEntityTextUrl;
import org.telegram.tgnet.TLRPC.TL_messageEntityUrl;
import org.telegram.tgnet.TLRPC.TL_messageMediaWebPage;
import org.telegram.tgnet.TLRPC.TL_webPage;
import org.telegram.tgnet.TLRPC.WebPage;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBox;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LetterDrawable;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.StickersActivity.TouchHelperCallback;

public class SharedLinkCell extends FrameLayout {
    private static TextPaint descriptionTextPaint;
    private static Paint paint;
    private static TextPaint titleTextPaint;
    private static Paint urlPaint;
    private CheckBox checkBox;
    private SharedLinkCellDelegate delegate;
    private int description2Y;
    private StaticLayout descriptionLayout;
    private StaticLayout descriptionLayout2;
    private int descriptionY;
    private boolean drawLinkImageView;
    private LetterDrawable letterDrawable;
    private ImageReceiver linkImageView;
    private ArrayList<StaticLayout> linkLayout;
    private boolean linkPreviewPressed;
    private int linkY;
    ArrayList<String> links;
    private MessageObject message;
    private boolean needDivider;
    private int pressedLink;
    private StaticLayout titleLayout;
    private int titleY;
    private LinkPath urlPath;

    public interface SharedLinkCellDelegate {
        boolean canPerformActions();

        void needOpenWebView(WebPage webPage);
    }

    public SharedLinkCell(Context context) {
        super(context);
        this.urlPath = new LinkPath();
        this.links = new ArrayList();
        this.linkLayout = new ArrayList();
        this.titleY = AndroidUtilities.dp(7.0f);
        this.descriptionY = AndroidUtilities.dp(27.0f);
        this.description2Y = AndroidUtilities.dp(27.0f);
        if (titleTextPaint == null) {
            titleTextPaint = new TextPaint(1);
            titleTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleTextPaint.setColor(-14606047);
            titleTextPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
            descriptionTextPaint = new TextPaint(1);
            descriptionTextPaint.setTextSize((float) AndroidUtilities.dp(16.0f));
            paint = new Paint();
            paint.setColor(-2500135);
            paint.setStrokeWidth(TouchHelperCallback.ALPHA_FULL);
            urlPaint = new Paint();
            urlPaint.setColor(Theme.MSG_LINK_SELECT_BACKGROUND_COLOR);
        }
        setWillNotDraw(false);
        this.linkImageView = new ImageReceiver(this);
        this.letterDrawable = new LetterDrawable();
        this.checkBox = new CheckBox(context, C0691R.drawable.round_check2);
        this.checkBox.setVisibility(4);
        addView(this.checkBox, LayoutHelper.createFrame(22, 22.0f, (LocaleController.isRTL ? 5 : 3) | 48, LocaleController.isRTL ? 0.0f : 44.0f, 44.0f, LocaleController.isRTL ? 44.0f : 0.0f, 0.0f));
    }

    @SuppressLint({"DrawAllocation"})
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int a;
        String link;
        int height;
        int x;
        this.drawLinkImageView = false;
        this.descriptionLayout = null;
        this.titleLayout = null;
        this.descriptionLayout2 = null;
        this.description2Y = this.descriptionY;
        this.linkLayout.clear();
        this.links.clear();
        int maxWidth = (MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp((float) AndroidUtilities.leftBaseline)) - AndroidUtilities.dp(8.0f);
        String title = null;
        String description = null;
        String description2 = null;
        String webPageLink = null;
        boolean hasPhoto = false;
        if ((this.message.messageOwner.media instanceof TL_messageMediaWebPage) && (this.message.messageOwner.media.webpage instanceof TL_webPage)) {
            WebPage webPage = this.message.messageOwner.media.webpage;
            if (this.message.photoThumbs == null && webPage.photo != null) {
                this.message.generateThumbs(true);
            }
            hasPhoto = (webPage.photo == null || this.message.photoThumbs == null) ? false : true;
            title = webPage.title;
            if (title == null) {
                title = webPage.site_name;
            }
            description = webPage.description;
            webPageLink = webPage.url;
        }
        if (!(this.message == null || this.message.messageOwner.entities.isEmpty())) {
            for (a = 0; a < this.message.messageOwner.entities.size(); a++) {
                MessageEntity entity = (MessageEntity) this.message.messageOwner.entities.get(a);
                if (entity.length > 0 && entity.offset >= 0 && entity.offset < this.message.messageOwner.message.length()) {
                    if (entity.offset + entity.length > this.message.messageOwner.message.length()) {
                        entity.length = this.message.messageOwner.message.length() - entity.offset;
                    }
                    if (!(a != 0 || webPageLink == null || (entity.offset == 0 && entity.length == this.message.messageOwner.message.length()))) {
                        if (this.message.messageOwner.entities.size() != 1) {
                            description2 = this.message.messageOwner.message;
                        } else if (description == null) {
                            description2 = this.message.messageOwner.message;
                        }
                    }
                    link = null;
                    try {
                        if ((entity instanceof TL_messageEntityTextUrl) || (entity instanceof TL_messageEntityUrl)) {
                            if (entity instanceof TL_messageEntityUrl) {
                                link = this.message.messageOwner.message.substring(entity.offset, entity.offset + entity.length);
                            } else {
                                link = entity.url;
                            }
                            if (title == null || title.length() == 0) {
                                title = Uri.parse(link).getHost();
                                if (title == null) {
                                    title = link;
                                }
                                if (title != null) {
                                    int index = title.lastIndexOf(46);
                                    if (index >= 0) {
                                        title = title.substring(0, index);
                                        index = title.lastIndexOf(46);
                                        if (index >= 0) {
                                            title = title.substring(index + 1);
                                        }
                                        title = title.substring(0, 1).toUpperCase() + title.substring(1);
                                    }
                                }
                                if (!(entity.offset == 0 && entity.length == this.message.messageOwner.message.length())) {
                                    description = this.message.messageOwner.message;
                                }
                            }
                        } else if ((entity instanceof TL_messageEntityEmail) && (title == null || title.length() == 0)) {
                            link = "mailto:" + this.message.messageOwner.message.substring(entity.offset, entity.offset + entity.length);
                            title = this.message.messageOwner.message.substring(entity.offset, entity.offset + entity.length);
                            if (!(entity.offset == 0 && entity.length == this.message.messageOwner.message.length())) {
                                description = this.message.messageOwner.message;
                            }
                        }
                        if (link != null) {
                            if (link.toLowerCase().indexOf("http") == 0 || link.toLowerCase().indexOf("mailto") == 0) {
                                this.links.add(link);
                            } else {
                                this.links.add("http://" + link);
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.m13e("tmessages", e);
                    }
                }
            }
        }
        if (webPageLink != null && this.links.isEmpty()) {
            this.links.add(webPageLink);
        }
        if (title != null) {
            try {
                this.titleLayout = new StaticLayout(TextUtils.ellipsize(title.replace('\n', ' '), titleTextPaint, (float) Math.min((int) Math.ceil((double) titleTextPaint.measureText(title)), maxWidth), TruncateAt.END), titleTextPaint, maxWidth, Alignment.ALIGN_NORMAL, TouchHelperCallback.ALPHA_FULL, 0.0f, false);
            } catch (Throwable e2) {
                FileLog.m13e("tmessages", e2);
            }
            this.letterDrawable.setTitle(title);
        }
        if (description != null) {
            try {
                this.descriptionLayout = ChatMessageCell.generateStaticLayout(description, descriptionTextPaint, maxWidth, maxWidth, 0, 3);
                if (this.descriptionLayout.getLineCount() > 0) {
                    this.description2Y = (this.descriptionY + this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1)) + AndroidUtilities.dp(TouchHelperCallback.ALPHA_FULL);
                }
            } catch (Throwable e22) {
                FileLog.m13e("tmessages", e22);
            }
        }
        if (description2 != null) {
            try {
                this.descriptionLayout2 = ChatMessageCell.generateStaticLayout(description2, descriptionTextPaint, maxWidth, maxWidth, 0, 3);
                height = this.descriptionLayout2.getLineBottom(this.descriptionLayout2.getLineCount() - 1);
                if (this.descriptionLayout != null) {
                    this.description2Y += AndroidUtilities.dp(10.0f);
                }
            } catch (Throwable e222) {
                FileLog.m13e("tmessages", e222);
            }
        }
        if (!this.links.isEmpty()) {
            for (a = 0; a < this.links.size(); a++) {
                try {
                    link = (String) this.links.get(a);
                    StaticLayout layout = new StaticLayout(TextUtils.ellipsize(link.replace('\n', ' '), descriptionTextPaint, (float) Math.min((int) Math.ceil((double) descriptionTextPaint.measureText(link)), maxWidth), TruncateAt.MIDDLE), descriptionTextPaint, maxWidth, Alignment.ALIGN_NORMAL, TouchHelperCallback.ALPHA_FULL, 0.0f, false);
                    this.linkY = this.description2Y;
                    if (!(this.descriptionLayout2 == null || this.descriptionLayout2.getLineCount() == 0)) {
                        this.linkY += this.descriptionLayout2.getLineBottom(this.descriptionLayout2.getLineCount() - 1) + AndroidUtilities.dp(TouchHelperCallback.ALPHA_FULL);
                    }
                    this.linkLayout.add(layout);
                } catch (Throwable e2222) {
                    FileLog.m13e("tmessages", e2222);
                }
            }
        }
        int maxPhotoWidth = AndroidUtilities.dp(52.0f);
        if (LocaleController.isRTL) {
            x = (MeasureSpec.getSize(widthMeasureSpec) - AndroidUtilities.dp(10.0f)) - maxPhotoWidth;
        } else {
            x = AndroidUtilities.dp(10.0f);
        }
        this.letterDrawable.setBounds(x, AndroidUtilities.dp(10.0f), x + maxPhotoWidth, AndroidUtilities.dp(62.0f));
        if (hasPhoto) {
            TLObject currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(this.message.photoThumbs, maxPhotoWidth, true);
            PhotoSize currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(this.message.photoThumbs, 80);
            if (currentPhotoObjectThumb == currentPhotoObject) {
                currentPhotoObjectThumb = null;
            }
            currentPhotoObject.size = -1;
            if (currentPhotoObjectThumb != null) {
                currentPhotoObjectThumb.size = -1;
            }
            this.linkImageView.setImageCoords(x, AndroidUtilities.dp(10.0f), maxPhotoWidth, maxPhotoWidth);
            String fileName = FileLoader.getAttachFileName(currentPhotoObject);
            boolean photoExist = true;
            if (!FileLoader.getPathToAttach(currentPhotoObject, true).exists()) {
                photoExist = false;
            }
            String filter = String.format(Locale.US, "%d_%d", new Object[]{Integer.valueOf(maxPhotoWidth), Integer.valueOf(maxPhotoWidth)});
            if (photoExist || MediaController.getInstance().canDownloadMedia(1) || FileLoader.getInstance().isLoadingFile(fileName)) {
                this.linkImageView.setImage(currentPhotoObject.location, filter, currentPhotoObjectThumb != null ? currentPhotoObjectThumb.location : null, String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(maxPhotoWidth), Integer.valueOf(maxPhotoWidth)}), 0, null, false);
            } else if (currentPhotoObjectThumb != null) {
                this.linkImageView.setImage(null, null, currentPhotoObjectThumb.location, String.format(Locale.US, "%d_%d_b", new Object[]{Integer.valueOf(maxPhotoWidth), Integer.valueOf(maxPhotoWidth)}), 0, null, false);
            } else {
                this.linkImageView.setImageBitmap((Drawable) null);
            }
            this.drawLinkImageView = true;
        }
        height = 0;
        if (!(this.titleLayout == null || this.titleLayout.getLineCount() == 0)) {
            height = 0 + this.titleLayout.getLineBottom(this.titleLayout.getLineCount() - 1);
        }
        if (!(this.descriptionLayout == null || this.descriptionLayout.getLineCount() == 0)) {
            height += this.descriptionLayout.getLineBottom(this.descriptionLayout.getLineCount() - 1);
        }
        if (!(this.descriptionLayout2 == null || this.descriptionLayout2.getLineCount() == 0)) {
            height += this.descriptionLayout2.getLineBottom(this.descriptionLayout2.getLineCount() - 1);
            if (this.descriptionLayout != null) {
                height += AndroidUtilities.dp(10.0f);
            }
        }
        for (a = 0; a < this.linkLayout.size(); a++) {
            layout = (StaticLayout) this.linkLayout.get(a);
            if (layout.getLineCount() > 0) {
                height += layout.getLineBottom(layout.getLineCount() - 1);
            }
        }
        if (hasPhoto) {
            height = Math.max(AndroidUtilities.dp(48.0f), height);
        }
        this.checkBox.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0f), C0747C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(22.0f), C0747C.ENCODING_PCM_32BIT));
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (this.needDivider ? 1 : 0) + Math.max(AndroidUtilities.dp(72.0f), AndroidUtilities.dp(16.0f) + height));
    }

    public void setLink(MessageObject messageObject, boolean divider) {
        this.needDivider = divider;
        resetPressedLink();
        this.message = messageObject;
        requestLayout();
    }

    public void setDelegate(SharedLinkCellDelegate sharedLinkCellDelegate) {
        this.delegate = sharedLinkCellDelegate;
    }

    public MessageObject getMessage() {
        return this.message;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.drawLinkImageView) {
            this.linkImageView.onDetachedFromWindow();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.drawLinkImageView) {
            this.linkImageView.onAttachedToWindow();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        if (this.message == null || this.linkLayout.isEmpty() || this.delegate == null || !this.delegate.canPerformActions()) {
            resetPressedLink();
        } else if (event.getAction() == 0 || (this.linkPreviewPressed && event.getAction() == 1)) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int offset = 0;
            boolean ok = false;
            for (int a = 0; a < this.linkLayout.size(); a++) {
                StaticLayout layout = (StaticLayout) this.linkLayout.get(a);
                if (layout.getLineCount() > 0) {
                    int height = layout.getLineBottom(layout.getLineCount() - 1);
                    int linkPosX = AndroidUtilities.dp(LocaleController.isRTL ? 8.0f : (float) AndroidUtilities.leftBaseline);
                    if (((float) x) < ((float) linkPosX) + layout.getLineLeft(0) || ((float) x) > ((float) linkPosX) + layout.getLineWidth(0) || y < this.linkY + offset || y > (this.linkY + offset) + height) {
                        offset += height;
                    } else {
                        ok = true;
                        if (event.getAction() == 0) {
                            resetPressedLink();
                            this.pressedLink = a;
                            this.linkPreviewPressed = true;
                            try {
                                this.urlPath.setCurrentLayout(layout, 0, 0.0f);
                                layout.getSelectionPath(0, layout.getText().length(), this.urlPath);
                            } catch (Throwable e) {
                                FileLog.m13e("tmessages", e);
                            }
                            result = true;
                        } else if (this.linkPreviewPressed) {
                            try {
                                WebPage webPage = (this.pressedLink != 0 || this.message.messageOwner.media == null) ? null : this.message.messageOwner.media.webpage;
                                if (webPage == null || VERSION.SDK_INT < 16 || webPage.embed_url == null || webPage.embed_url.length() == 0) {
                                    Browser.openUrl(getContext(), (String) this.links.get(this.pressedLink));
                                    resetPressedLink();
                                    result = true;
                                } else {
                                    this.delegate.needOpenWebView(webPage);
                                    resetPressedLink();
                                    result = true;
                                }
                            } catch (Throwable e2) {
                                FileLog.m13e("tmessages", e2);
                            }
                        }
                        if (!ok) {
                            resetPressedLink();
                        }
                    }
                }
            }
            if (ok) {
                resetPressedLink();
            }
        } else if (event.getAction() == 3) {
            resetPressedLink();
        }
        if (result || super.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    public String getLink(int num) {
        if (num < 0 || num >= this.links.size()) {
            return null;
        }
        return (String) this.links.get(num);
    }

    protected void resetPressedLink() {
        this.pressedLink = -1;
        this.linkPreviewPressed = false;
        invalidate();
    }

    public void setChecked(boolean checked, boolean animated) {
        if (this.checkBox.getVisibility() != 0) {
            this.checkBox.setVisibility(0);
        }
        this.checkBox.setChecked(checked, animated);
    }

    protected void onDraw(Canvas canvas) {
        float f;
        if (this.titleLayout != null) {
            canvas.save();
            canvas.translate((float) AndroidUtilities.dp(LocaleController.isRTL ? 8.0f : (float) AndroidUtilities.leftBaseline), (float) this.titleY);
            this.titleLayout.draw(canvas);
            canvas.restore();
        }
        if (this.descriptionLayout != null) {
            descriptionTextPaint.setColor(-14606047);
            canvas.save();
            canvas.translate((float) AndroidUtilities.dp(LocaleController.isRTL ? 8.0f : (float) AndroidUtilities.leftBaseline), (float) this.descriptionY);
            this.descriptionLayout.draw(canvas);
            canvas.restore();
        }
        if (this.descriptionLayout2 != null) {
            descriptionTextPaint.setColor(-14606047);
            canvas.save();
            if (LocaleController.isRTL) {
                f = 8.0f;
            } else {
                f = (float) AndroidUtilities.leftBaseline;
            }
            canvas.translate((float) AndroidUtilities.dp(f), (float) this.description2Y);
            this.descriptionLayout2.draw(canvas);
            canvas.restore();
        }
        if (!this.linkLayout.isEmpty()) {
            descriptionTextPaint.setColor(Theme.MSG_LINK_TEXT_COLOR);
            int offset = 0;
            for (int a = 0; a < this.linkLayout.size(); a++) {
                StaticLayout layout = (StaticLayout) this.linkLayout.get(a);
                if (layout.getLineCount() > 0) {
                    canvas.save();
                    if (LocaleController.isRTL) {
                        f = 8.0f;
                    } else {
                        f = (float) AndroidUtilities.leftBaseline;
                    }
                    canvas.translate((float) AndroidUtilities.dp(f), (float) (this.linkY + offset));
                    if (this.pressedLink == a) {
                        canvas.drawPath(this.urlPath, urlPaint);
                    }
                    layout.draw(canvas);
                    canvas.restore();
                    offset += layout.getLineBottom(layout.getLineCount() - 1);
                }
            }
        }
        this.letterDrawable.draw(canvas);
        if (this.drawLinkImageView) {
            this.linkImageView.draw(canvas);
        }
        if (!this.needDivider) {
            return;
        }
        if (LocaleController.isRTL) {
            canvas.drawLine(0.0f, (float) (getMeasuredHeight() - 1), (float) (getMeasuredWidth() - AndroidUtilities.dp((float) AndroidUtilities.leftBaseline)), (float) (getMeasuredHeight() - 1), paint);
        } else {
            canvas.drawLine((float) AndroidUtilities.dp((float) AndroidUtilities.leftBaseline), (float) (getMeasuredHeight() - 1), (float) getMeasuredWidth(), (float) (getMeasuredHeight() - 1), paint);
        }
    }
}
