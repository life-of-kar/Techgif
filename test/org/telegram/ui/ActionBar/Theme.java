package org.telegram.ui.ActionBar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import java.lang.reflect.Array;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0691R;

public class Theme {
    public static int ACTION_BAR_ACTION_MODE_TEXT_COLOR;
    public static int ACTION_BAR_AUDIO_SELECTOR_COLOR;
    public static int ACTION_BAR_BLUE_SELECTOR_COLOR;
    public static int ACTION_BAR_CHANNEL_INTRO_COLOR;
    public static int ACTION_BAR_CHANNEL_INTRO_SELECTOR_COLOR;
    public static int ACTION_BAR_COLOR;
    public static int ACTION_BAR_CYAN_SELECTOR_COLOR;
    public static int ACTION_BAR_GREEN_SELECTOR_COLOR;
    public static int ACTION_BAR_MAIN_AVATAR_COLOR;
    public static int ACTION_BAR_MEDIA_PICKER_COLOR;
    public static int ACTION_BAR_MODE_SELECTOR_COLOR;
    public static int ACTION_BAR_ORANGE_SELECTOR_COLOR;
    public static int ACTION_BAR_PHOTO_VIEWER_COLOR;
    public static int ACTION_BAR_PICKER_SELECTOR_COLOR;
    public static int ACTION_BAR_PINK_SELECTOR_COLOR;
    public static int ACTION_BAR_PLAYER_COLOR;
    public static int ACTION_BAR_PROFILE_COLOR;
    public static int ACTION_BAR_PROFILE_SUBTITLE_COLOR;
    public static int ACTION_BAR_RED_SELECTOR_COLOR;
    public static int ACTION_BAR_SELECTOR_COLOR;
    public static int ACTION_BAR_SUBTITLE_COLOR;
    public static int ACTION_BAR_TITLE_COLOR;
    public static int ACTION_BAR_VIOLET_SELECTOR_COLOR;
    public static int ACTION_BAR_WHITE_SELECTOR_COLOR;
    public static int ACTION_BAR_YELLOW_SELECTOR_COLOR;
    public static int ALERT_PANEL_MESSAGE_TEXT_COLOR;
    public static int ALERT_PANEL_NAME_TEXT_COLOR;
    public static int ATTACH_SHEET_TEXT_COLOR;
    public static int AUTODOWNLOAD_SHEET_SAVE_TEXT_COLOR;
    public static int CHAT_ADD_CONTACT_TEXT_COLOR;
    public static int CHAT_BOTTOM_CHAT_OVERLAY_TEXT_COLOR;
    public static int CHAT_BOTTOM_OVERLAY_TEXT_COLOR;
    public static int CHAT_EMPTY_VIEW_TEXT_COLOR;
    public static int CHAT_GIF_HINT_TEXT_COLOR;
    public static int CHAT_REPORT_SPAM_TEXT_COLOR;
    public static int CHAT_SEARCH_COUNT_TEXT_COLOR;
    public static int CHAT_UNREAD_TEXT_COLOR;
    public static int DIALOGS_ATTACH_TEXT_COLOR;
    public static int DIALOGS_DRAFT_TEXT_COLOR;
    public static int DIALOGS_MESSAGE_TEXT_COLOR;
    public static int DIALOGS_NAME_TEXT_COLOR;
    public static int DIALOGS_PRINTING_TEXT_COLOR;
    public static int INAPP_PLAYER_BACKGROUND_COLOR;
    public static int INAPP_PLAYER_PERFORMER_TEXT_COLOR;
    public static int INAPP_PLAYER_TITLE_TEXT_COLOR;
    public static int INPUT_FIELD_SELECTOR_COLOR;
    public static int MSG_BOT_BUTTON_TEXT_COLOR;
    public static int MSG_BOT_PROGRESS_COLOR;
    public static int MSG_IN_AUDIO_DURATION_SELECTED_TEXT_COLOR;
    public static int MSG_IN_AUDIO_DURATION_TEXT_COLOR;
    public static int MSG_IN_AUDIO_PERFORMER_TEXT_COLOR;
    public static int MSG_IN_AUDIO_PROGRESS_COLOR;
    public static int MSG_IN_AUDIO_SEEKBAR_COLOR;
    public static int MSG_IN_AUDIO_SEEKBAR_FILL_COLOR;
    public static int MSG_IN_AUDIO_SEEKBAR_SELECTED_COLOR;
    public static int MSG_IN_AUDIO_SELECTED_PROGRESS_COLOR;
    public static int MSG_IN_AUDIO_TITLE_TEXT_COLOR;
    public static int MSG_IN_CONTACT_NAME_TEXT_COLOR;
    public static int MSG_IN_CONTACT_PHONE_TEXT_COLOR;
    public static int MSG_IN_FILE_BACKGROUND_COLOR;
    public static int MSG_IN_FILE_BACKGROUND_SELECTED_COLOR;
    public static int MSG_IN_FILE_INFO_SELECTED_TEXT_COLOR;
    public static int MSG_IN_FILE_INFO_TEXT_COLOR;
    public static int MSG_IN_FILE_NAME_TEXT_COLOR;
    public static int MSG_IN_FILE_PROGRESS_COLOR;
    public static int MSG_IN_FILE_PROGRESS_SELECTED_COLOR;
    public static int MSG_IN_FORDWARDED_NAME_TEXT_COLOR;
    public static int MSG_IN_REPLY_LINE_COLOR;
    public static int MSG_IN_REPLY_MEDIA_MESSAGE_SELETED_TEXT_COLOR;
    public static int MSG_IN_REPLY_MEDIA_MESSAGE_TEXT_COLOR;
    public static int MSG_IN_REPLY_MESSAGE_TEXT_COLOR;
    public static int MSG_IN_REPLY_NAME_TEXT_COLOR;
    public static int MSG_IN_SITE_NAME_TEXT_COLOR;
    public static int MSG_IN_TIME_SELECTED_TEXT_COLOR;
    public static int MSG_IN_TIME_TEXT_COLOR;
    public static int MSG_IN_VENUE_INFO_SELECTED_TEXT_COLOR;
    public static int MSG_IN_VENUE_INFO_TEXT_COLOR;
    public static int MSG_IN_VENUE_NAME_TEXT_COLOR;
    public static int MSG_IN_VIA_BOT_NAME_TEXT_COLOR;
    public static int MSG_IN_VOICE_SEEKBAR_COLOR;
    public static int MSG_IN_VOICE_SEEKBAR_FILL_COLOR;
    public static int MSG_IN_VOICE_SEEKBAR_SELECTED_COLOR;
    public static int MSG_IN_WEB_PREVIEW_LINE_COLOR;
    public static int MSG_LINK_SELECT_BACKGROUND_COLOR;
    public static int MSG_LINK_TEXT_COLOR;
    public static int MSG_MEDIA_INFO_TEXT_COLOR;
    public static int MSG_MEDIA_PROGRESS_COLOR;
    public static int MSG_MEDIA_TIME_TEXT_COLOR;
    public static int MSG_OUT_AUDIO_DURATION_SELECTED_TEXT_COLOR;
    public static int MSG_OUT_AUDIO_DURATION_TEXT_COLOR;
    public static int MSG_OUT_AUDIO_PERFORMER_TEXT_COLOR;
    public static int MSG_OUT_AUDIO_PROGRESS_COLOR;
    public static int MSG_OUT_AUDIO_SEEKBAR_COLOR;
    public static int MSG_OUT_AUDIO_SEEKBAR_FILL_COLOR;
    public static int MSG_OUT_AUDIO_SEEKBAR_SELECTED_COLOR;
    public static int MSG_OUT_AUDIO_SELECTED_PROGRESS_COLOR;
    public static int MSG_OUT_AUDIO_TITLE_TEXT_COLOR;
    public static int MSG_OUT_CONTACT_NAME_TEXT_COLOR;
    public static int MSG_OUT_CONTACT_PHONE_TEXT_COLOR;
    public static int MSG_OUT_FILE_BACKGROUND_COLOR;
    public static int MSG_OUT_FILE_BACKGROUND_SELECTED_COLOR;
    public static int MSG_OUT_FILE_INFO_SELECTED_TEXT_COLOR;
    public static int MSG_OUT_FILE_INFO_TEXT_COLOR;
    public static int MSG_OUT_FILE_NAME_TEXT_COLOR;
    public static int MSG_OUT_FILE_PROGRESS_COLOR;
    public static int MSG_OUT_FILE_PROGRESS_SELECTED_COLOR;
    public static int MSG_OUT_FORDWARDED_NAME_TEXT_COLOR;
    public static int MSG_OUT_REPLY_LINE_COLOR;
    public static int MSG_OUT_REPLY_MEDIA_MESSAGE_SELETED_TEXT_COLOR;
    public static int MSG_OUT_REPLY_MEDIA_MESSAGE_TEXT_COLOR;
    public static int MSG_OUT_REPLY_MESSAGE_TEXT_COLOR;
    public static int MSG_OUT_REPLY_NAME_TEXT_COLOR;
    public static int MSG_OUT_SITE_NAME_TEXT_COLOR;
    public static int MSG_OUT_TIME_SELECTED_TEXT_COLOR;
    public static int MSG_OUT_TIME_TEXT_COLOR;
    public static int MSG_OUT_VENUE_INFO_SELECTED_TEXT_COLOR;
    public static int MSG_OUT_VENUE_INFO_TEXT_COLOR;
    public static int MSG_OUT_VENUE_NAME_TEXT_COLOR;
    public static int MSG_OUT_VIA_BOT_NAME_TEXT_COLOR;
    public static int MSG_OUT_VOICE_SEEKBAR_COLOR;
    public static int MSG_OUT_VOICE_SEEKBAR_FILL_COLOR;
    public static int MSG_OUT_VOICE_SEEKBAR_SELECTED_COLOR;
    public static int MSG_OUT_WEB_PREVIEW_LINE_COLOR;
    public static int MSG_SECRET_TIME_TEXT_COLOR;
    public static int MSG_SELECTED_BACKGROUND_COLOR;
    public static int MSG_STICKER_NAME_TEXT_COLOR;
    public static int MSG_STICKER_REPLY_LINE_COLOR;
    public static int MSG_STICKER_REPLY_MESSAGE_TEXT_COLOR;
    public static int MSG_STICKER_REPLY_NAME_TEXT_COLOR;
    public static int MSG_STICKER_VIA_BOT_NAME_TEXT_COLOR;
    public static int MSG_TEXT_COLOR;
    public static int MSG_TEXT_SELECT_BACKGROUND_COLOR;
    public static int MSG_WEB_PREVIEW_DURATION_TEXT_COLOR;
    public static int PINNED_PANEL_MESSAGE_TEXT_COLOR;
    public static int PINNED_PANEL_NAME_TEXT_COLOR;
    public static int REPLY_PANEL_MESSAGE_TEXT_COLOR;
    public static int REPLY_PANEL_NAME_TEXT_COLOR;
    public static int SECRET_CHAT_INFO_TEXT_COLOR;
    public static int SHARE_SHEET_BADGE_TEXT_COLOR;
    public static int SHARE_SHEET_COPY_TEXT_COLOR;
    public static int SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR;
    public static int SHARE_SHEET_EDIT_TEXT_COLOR;
    public static int SHARE_SHEET_SEND_DISABLED_TEXT_COLOR;
    public static int SHARE_SHEET_SEND_TEXT_COLOR;
    public static int STICKERS_SHEET_ADD_TEXT_COLOR;
    public static int STICKERS_SHEET_CLOSE_TEXT_COLOR;
    public static int STICKERS_SHEET_REMOVE_TEXT_COLOR;
    public static int STICKERS_SHEET_SEND_TEXT_COLOR;
    public static int STICKERS_SHEET_TITLE_TEXT_COLOR;
    public static Drawable[] attachButtonDrawables;
    public static Drawable backgroundBluePressed;
    public static Drawable backgroundDrawableIn;
    public static Drawable backgroundDrawableInSelected;
    public static Drawable backgroundDrawableOut;
    public static Drawable backgroundDrawableOutSelected;
    public static Drawable backgroundMediaDrawableIn;
    public static Drawable backgroundMediaDrawableInSelected;
    public static Drawable backgroundMediaDrawableOut;
    public static Drawable backgroundMediaDrawableOutSelected;
    public static Drawable botInline;
    public static Drawable botLink;
    public static Drawable broadcastDrawable;
    public static Drawable broadcastMediaDrawable;
    public static Drawable checkDrawable;
    public static Drawable checkMediaDrawable;
    public static Drawable[] clockChannelDrawable;
    public static Drawable clockDrawable;
    public static Drawable clockMediaDrawable;
    public static PorterDuffColorFilter colorFilter;
    public static PorterDuffColorFilter colorPressedFilter;
    public static Drawable[] contactDrawable;
    public static Drawable[] cornerInner;
    public static Drawable[] cornerOuter;
    private static int currentColor;
    public static Drawable[] docMenuDrawable;
    public static Drawable errorDrawable;
    public static Drawable[][] fileStatesDrawable;
    public static Drawable geoInDrawable;
    public static Drawable geoOutDrawable;
    public static Drawable halfCheckDrawable;
    public static Drawable halfCheckMediaDrawable;
    public static Drawable inlineAudioDrawable;
    public static Drawable inlineDocDrawable;
    public static Drawable inlineLocationDrawable;
    private static Paint maskPaint;
    public static Drawable[][] photoStatesDrawables;
    public static Drawable shareDrawable;
    public static Drawable shareIconDrawable;
    public static Drawable systemDrawable;
    public static Drawable timeBackgroundDrawable;
    public static Drawable timeStickerBackgroundDrawable;
    public static Drawable[] viewsCountDrawable;
    public static Drawable viewsMediaCountDrawable;
    public static Drawable viewsOutCountDrawable;

    /* renamed from: org.telegram.ui.ActionBar.Theme.1 */
    static class C09151 extends Drawable {
        C09151() {
        }

        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.drawCircle((float) bounds.centerX(), (float) bounds.centerY(), (float) AndroidUtilities.dp(18.0f), Theme.maskPaint);
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public int getOpacity() {
            return 0;
        }
    }

    static {
        ACTION_BAR_COLOR = -11371101;
        ACTION_BAR_PHOTO_VIEWER_COLOR = 2130706432;
        ACTION_BAR_MEDIA_PICKER_COLOR = -13421773;
        ACTION_BAR_CHANNEL_INTRO_COLOR = -1;
        ACTION_BAR_PLAYER_COLOR = -1;
        ACTION_BAR_TITLE_COLOR = -1;
        ACTION_BAR_SUBTITLE_COLOR = -2758409;
        ACTION_BAR_PROFILE_COLOR = -10907718;
        ACTION_BAR_PROFILE_SUBTITLE_COLOR = -2626822;
        ACTION_BAR_MAIN_AVATAR_COLOR = -11500111;
        ACTION_BAR_ACTION_MODE_TEXT_COLOR = -9211021;
        ACTION_BAR_SELECTOR_COLOR = -12554860;
        INPUT_FIELD_SELECTOR_COLOR = -2697514;
        ACTION_BAR_PICKER_SELECTOR_COLOR = -12763843;
        ACTION_BAR_WHITE_SELECTOR_COLOR = 1090519039;
        ACTION_BAR_AUDIO_SELECTOR_COLOR = 788529152;
        ACTION_BAR_CHANNEL_INTRO_SELECTOR_COLOR = 788529152;
        ACTION_BAR_MODE_SELECTOR_COLOR = -986896;
        ACTION_BAR_BLUE_SELECTOR_COLOR = -11959891;
        ACTION_BAR_CYAN_SELECTOR_COLOR = -13007715;
        ACTION_BAR_GREEN_SELECTOR_COLOR = -12020419;
        ACTION_BAR_ORANGE_SELECTOR_COLOR = -1674199;
        ACTION_BAR_PINK_SELECTOR_COLOR = -2863493;
        ACTION_BAR_RED_SELECTOR_COLOR = -4437183;
        ACTION_BAR_VIOLET_SELECTOR_COLOR = -9216066;
        ACTION_BAR_YELLOW_SELECTOR_COLOR = -1073399;
        ATTACH_SHEET_TEXT_COLOR = -9079435;
        DIALOGS_MESSAGE_TEXT_COLOR = -7368817;
        DIALOGS_NAME_TEXT_COLOR = -11697229;
        DIALOGS_ATTACH_TEXT_COLOR = -11697229;
        DIALOGS_PRINTING_TEXT_COLOR = -11697229;
        DIALOGS_DRAFT_TEXT_COLOR = -2274503;
        CHAT_UNREAD_TEXT_COLOR = -11102772;
        CHAT_ADD_CONTACT_TEXT_COLOR = -11894091;
        CHAT_REPORT_SPAM_TEXT_COLOR = -3188393;
        CHAT_BOTTOM_OVERLAY_TEXT_COLOR = -8421505;
        CHAT_BOTTOM_CHAT_OVERLAY_TEXT_COLOR = -12940081;
        CHAT_GIF_HINT_TEXT_COLOR = -1;
        CHAT_EMPTY_VIEW_TEXT_COLOR = -1;
        CHAT_SEARCH_COUNT_TEXT_COLOR = -11625772;
        INAPP_PLAYER_PERFORMER_TEXT_COLOR = -13683656;
        INAPP_PLAYER_TITLE_TEXT_COLOR = -13683656;
        INAPP_PLAYER_BACKGROUND_COLOR = -1;
        REPLY_PANEL_NAME_TEXT_COLOR = -12940081;
        REPLY_PANEL_MESSAGE_TEXT_COLOR = -14540254;
        ALERT_PANEL_NAME_TEXT_COLOR = -12940081;
        ALERT_PANEL_MESSAGE_TEXT_COLOR = -6710887;
        AUTODOWNLOAD_SHEET_SAVE_TEXT_COLOR = -12940081;
        SHARE_SHEET_COPY_TEXT_COLOR = -12940081;
        SHARE_SHEET_SEND_TEXT_COLOR = -12664327;
        SHARE_SHEET_SEND_DISABLED_TEXT_COLOR = -5000269;
        SHARE_SHEET_EDIT_TEXT_COLOR = -14606047;
        SHARE_SHEET_EDIT_PLACEHOLDER_TEXT_COLOR = -6842473;
        SHARE_SHEET_BADGE_TEXT_COLOR = -1;
        STICKERS_SHEET_TITLE_TEXT_COLOR = -14606047;
        STICKERS_SHEET_SEND_TEXT_COLOR = -12940081;
        STICKERS_SHEET_ADD_TEXT_COLOR = -12940081;
        STICKERS_SHEET_CLOSE_TEXT_COLOR = -12940081;
        STICKERS_SHEET_REMOVE_TEXT_COLOR = -3319206;
        PINNED_PANEL_NAME_TEXT_COLOR = -12940081;
        PINNED_PANEL_MESSAGE_TEXT_COLOR = -6710887;
        SECRET_CHAT_INFO_TEXT_COLOR = -1;
        MSG_SELECTED_BACKGROUND_COLOR = 1714664933;
        MSG_WEB_PREVIEW_DURATION_TEXT_COLOR = -1;
        MSG_SECRET_TIME_TEXT_COLOR = -1776928;
        MSG_STICKER_NAME_TEXT_COLOR = -1;
        MSG_BOT_BUTTON_TEXT_COLOR = -1;
        MSG_BOT_PROGRESS_COLOR = -1;
        MSG_IN_FORDWARDED_NAME_TEXT_COLOR = -13072697;
        MSG_OUT_FORDWARDED_NAME_TEXT_COLOR = -11162801;
        MSG_IN_VIA_BOT_NAME_TEXT_COLOR = -12940081;
        MSG_OUT_VIA_BOT_NAME_TEXT_COLOR = -11162801;
        MSG_STICKER_VIA_BOT_NAME_TEXT_COLOR = -1;
        MSG_IN_REPLY_LINE_COLOR = -9390872;
        MSG_OUT_REPLY_LINE_COLOR = -7812741;
        MSG_STICKER_REPLY_LINE_COLOR = -1;
        MSG_IN_REPLY_NAME_TEXT_COLOR = -12940081;
        MSG_OUT_REPLY_NAME_TEXT_COLOR = -11162801;
        MSG_STICKER_REPLY_NAME_TEXT_COLOR = -1;
        MSG_IN_REPLY_MESSAGE_TEXT_COLOR = ViewCompat.MEASURED_STATE_MASK;
        MSG_OUT_REPLY_MESSAGE_TEXT_COLOR = ViewCompat.MEASURED_STATE_MASK;
        MSG_IN_REPLY_MEDIA_MESSAGE_TEXT_COLOR = -6182221;
        MSG_OUT_REPLY_MEDIA_MESSAGE_TEXT_COLOR = -10112933;
        MSG_IN_REPLY_MEDIA_MESSAGE_SELETED_TEXT_COLOR = -7752511;
        MSG_OUT_REPLY_MEDIA_MESSAGE_SELETED_TEXT_COLOR = -10112933;
        MSG_STICKER_REPLY_MESSAGE_TEXT_COLOR = -1;
        MSG_IN_WEB_PREVIEW_LINE_COLOR = -9390872;
        MSG_OUT_WEB_PREVIEW_LINE_COLOR = -7812741;
        MSG_IN_SITE_NAME_TEXT_COLOR = -12940081;
        MSG_OUT_SITE_NAME_TEXT_COLOR = -11162801;
        MSG_IN_CONTACT_NAME_TEXT_COLOR = -11625772;
        MSG_OUT_CONTACT_NAME_TEXT_COLOR = -11162801;
        MSG_IN_CONTACT_PHONE_TEXT_COLOR = -13683656;
        MSG_OUT_CONTACT_PHONE_TEXT_COLOR = -13286860;
        MSG_MEDIA_PROGRESS_COLOR = -1;
        MSG_IN_AUDIO_PROGRESS_COLOR = -1;
        MSG_OUT_AUDIO_PROGRESS_COLOR = -1048610;
        MSG_IN_AUDIO_SELECTED_PROGRESS_COLOR = -1902337;
        MSG_OUT_AUDIO_SELECTED_PROGRESS_COLOR = -2820676;
        MSG_MEDIA_TIME_TEXT_COLOR = -1;
        MSG_IN_TIME_TEXT_COLOR = -6182221;
        MSG_OUT_TIME_TEXT_COLOR = -9391780;
        MSG_IN_TIME_SELECTED_TEXT_COLOR = -7752511;
        MSG_OUT_TIME_SELECTED_TEXT_COLOR = -9391780;
        MSG_IN_AUDIO_PERFORMER_TEXT_COLOR = -13683656;
        MSG_OUT_AUDIO_PERFORMER_TEXT_COLOR = -13286860;
        MSG_IN_AUDIO_TITLE_TEXT_COLOR = -11625772;
        MSG_OUT_AUDIO_TITLE_TEXT_COLOR = -11162801;
        MSG_IN_AUDIO_DURATION_TEXT_COLOR = -6182221;
        MSG_OUT_AUDIO_DURATION_TEXT_COLOR = -10112933;
        MSG_IN_AUDIO_DURATION_SELECTED_TEXT_COLOR = -7752511;
        MSG_OUT_AUDIO_DURATION_SELECTED_TEXT_COLOR = -10112933;
        MSG_IN_AUDIO_SEEKBAR_COLOR = -1774864;
        MSG_OUT_AUDIO_SEEKBAR_COLOR = -4463700;
        MSG_IN_AUDIO_SEEKBAR_SELECTED_COLOR = -4399384;
        MSG_OUT_AUDIO_SEEKBAR_SELECTED_COLOR = -5644906;
        MSG_IN_AUDIO_SEEKBAR_FILL_COLOR = -9259544;
        MSG_OUT_AUDIO_SEEKBAR_FILL_COLOR = -8863118;
        MSG_IN_VOICE_SEEKBAR_COLOR = -2169365;
        MSG_OUT_VOICE_SEEKBAR_COLOR = -4463700;
        MSG_IN_VOICE_SEEKBAR_SELECTED_COLOR = -4399384;
        MSG_OUT_VOICE_SEEKBAR_SELECTED_COLOR = -5644906;
        MSG_IN_VOICE_SEEKBAR_FILL_COLOR = -9259544;
        MSG_OUT_VOICE_SEEKBAR_FILL_COLOR = -8863118;
        MSG_IN_FILE_PROGRESS_COLOR = -1314571;
        MSG_OUT_FILE_PROGRESS_COLOR = -2427453;
        MSG_IN_FILE_PROGRESS_SELECTED_COLOR = -3413258;
        MSG_OUT_FILE_PROGRESS_SELECTED_COLOR = -3806041;
        MSG_IN_FILE_NAME_TEXT_COLOR = -11625772;
        MSG_OUT_FILE_NAME_TEXT_COLOR = -11162801;
        MSG_IN_FILE_INFO_TEXT_COLOR = -6182221;
        MSG_OUT_FILE_INFO_TEXT_COLOR = -10112933;
        MSG_IN_FILE_INFO_SELECTED_TEXT_COLOR = -7752511;
        MSG_OUT_FILE_INFO_SELECTED_TEXT_COLOR = -10112933;
        MSG_IN_FILE_BACKGROUND_COLOR = -1314571;
        MSG_OUT_FILE_BACKGROUND_COLOR = -2427453;
        MSG_IN_FILE_BACKGROUND_SELECTED_COLOR = -3413258;
        MSG_OUT_FILE_BACKGROUND_SELECTED_COLOR = -3806041;
        MSG_IN_VENUE_NAME_TEXT_COLOR = -11625772;
        MSG_OUT_VENUE_NAME_TEXT_COLOR = -11162801;
        MSG_IN_VENUE_INFO_TEXT_COLOR = -6182221;
        MSG_OUT_VENUE_INFO_TEXT_COLOR = -10112933;
        MSG_IN_VENUE_INFO_SELECTED_TEXT_COLOR = -7752511;
        MSG_OUT_VENUE_INFO_SELECTED_TEXT_COLOR = -10112933;
        MSG_MEDIA_INFO_TEXT_COLOR = -1;
        MSG_TEXT_COLOR = ViewCompat.MEASURED_STATE_MASK;
        MSG_LINK_TEXT_COLOR = -14255946;
        MSG_LINK_SELECT_BACKGROUND_COLOR = 862104035;
        MSG_TEXT_SELECT_BACKGROUND_COLOR = 1717742051;
        clockChannelDrawable = new Drawable[2];
        cornerOuter = new Drawable[4];
        cornerInner = new Drawable[4];
        viewsCountDrawable = new Drawable[2];
        contactDrawable = new Drawable[2];
        fileStatesDrawable = (Drawable[][]) Array.newInstance(Drawable.class, new int[]{10, 2});
        photoStatesDrawables = (Drawable[][]) Array.newInstance(Drawable.class, new int[]{13, 2});
        docMenuDrawable = new Drawable[4];
        attachButtonDrawables = new Drawable[8];
        maskPaint = new Paint(1);
    }

    public static void loadRecources(Context context) {
        ACTION_BAR_COLOR = context.getResources().getColor(C0691R.color.header);
        ACTION_BAR_PROFILE_COLOR = context.getResources().getColor(C0691R.color.header);
        ACTION_BAR_MAIN_AVATAR_COLOR = context.getResources().getColor(C0691R.color.header);
        ACTION_BAR_SELECTOR_COLOR = context.getResources().getColor(C0691R.color.header);
        INPUT_FIELD_SELECTOR_COLOR = context.getResources().getColor(C0691R.color.message_bar_mic_background);
        if (backgroundDrawableIn == null) {
            backgroundDrawableIn = context.getResources().getDrawable(C0691R.drawable.msg_in);
            backgroundDrawableInSelected = context.getResources().getDrawable(C0691R.drawable.msg_in_selected);
            backgroundDrawableOut = context.getResources().getDrawable(C0691R.drawable.msg_out);
            backgroundDrawableOutSelected = context.getResources().getDrawable(C0691R.drawable.msg_out_selected);
            backgroundMediaDrawableIn = context.getResources().getDrawable(C0691R.drawable.msg_in_photo);
            backgroundMediaDrawableInSelected = context.getResources().getDrawable(C0691R.drawable.msg_in_photo_selected);
            backgroundMediaDrawableOut = context.getResources().getDrawable(C0691R.drawable.msg_out_photo);
            backgroundMediaDrawableOutSelected = context.getResources().getDrawable(C0691R.drawable.msg_out_photo_selected);
            checkDrawable = context.getResources().getDrawable(C0691R.drawable.msg_check);
            halfCheckDrawable = context.getResources().getDrawable(C0691R.drawable.msg_halfcheck);
            clockDrawable = context.getResources().getDrawable(C0691R.drawable.msg_clock);
            checkMediaDrawable = context.getResources().getDrawable(C0691R.drawable.msg_check_w);
            halfCheckMediaDrawable = context.getResources().getDrawable(C0691R.drawable.msg_halfcheck_w);
            clockMediaDrawable = context.getResources().getDrawable(C0691R.drawable.msg_clock_photo);
            clockChannelDrawable[0] = context.getResources().getDrawable(C0691R.drawable.msg_clock2);
            clockChannelDrawable[1] = context.getResources().getDrawable(C0691R.drawable.msg_clock2_s);
            errorDrawable = context.getResources().getDrawable(C0691R.drawable.msg_warning);
            timeBackgroundDrawable = context.getResources().getDrawable(C0691R.drawable.phototime2_b);
            timeStickerBackgroundDrawable = context.getResources().getDrawable(C0691R.drawable.phototime2);
            broadcastDrawable = context.getResources().getDrawable(C0691R.drawable.broadcast3);
            broadcastMediaDrawable = context.getResources().getDrawable(C0691R.drawable.broadcast4);
            systemDrawable = context.getResources().getDrawable(C0691R.drawable.system);
            botLink = context.getResources().getDrawable(C0691R.drawable.bot_link);
            botInline = context.getResources().getDrawable(C0691R.drawable.bot_lines);
            viewsCountDrawable[0] = context.getResources().getDrawable(C0691R.drawable.post_views);
            viewsCountDrawable[1] = context.getResources().getDrawable(C0691R.drawable.post_views_s);
            viewsOutCountDrawable = context.getResources().getDrawable(C0691R.drawable.post_viewsg);
            viewsMediaCountDrawable = context.getResources().getDrawable(C0691R.drawable.post_views_w);
            fileStatesDrawable[0][0] = context.getResources().getDrawable(C0691R.drawable.play_g);
            fileStatesDrawable[0][1] = context.getResources().getDrawable(C0691R.drawable.play_g_s);
            fileStatesDrawable[1][0] = context.getResources().getDrawable(C0691R.drawable.pause_g);
            fileStatesDrawable[1][1] = context.getResources().getDrawable(C0691R.drawable.pause_g_s);
            fileStatesDrawable[2][0] = context.getResources().getDrawable(C0691R.drawable.file_g_load);
            fileStatesDrawable[2][1] = context.getResources().getDrawable(C0691R.drawable.file_g_load_s);
            fileStatesDrawable[3][0] = context.getResources().getDrawable(C0691R.drawable.file_g);
            fileStatesDrawable[3][1] = context.getResources().getDrawable(C0691R.drawable.file_g_s);
            fileStatesDrawable[4][0] = context.getResources().getDrawable(C0691R.drawable.file_g_cancel);
            fileStatesDrawable[4][1] = context.getResources().getDrawable(C0691R.drawable.file_g_cancel_s);
            fileStatesDrawable[5][0] = context.getResources().getDrawable(C0691R.drawable.play_b);
            fileStatesDrawable[5][1] = context.getResources().getDrawable(C0691R.drawable.play_b_s);
            fileStatesDrawable[6][0] = context.getResources().getDrawable(C0691R.drawable.pause_b);
            fileStatesDrawable[6][1] = context.getResources().getDrawable(C0691R.drawable.pause_b_s);
            fileStatesDrawable[7][0] = context.getResources().getDrawable(C0691R.drawable.file_b_load);
            fileStatesDrawable[7][1] = context.getResources().getDrawable(C0691R.drawable.file_b_load_s);
            fileStatesDrawable[8][0] = context.getResources().getDrawable(C0691R.drawable.file_b);
            fileStatesDrawable[8][1] = context.getResources().getDrawable(C0691R.drawable.file_b_s);
            fileStatesDrawable[9][0] = context.getResources().getDrawable(C0691R.drawable.file_b_cancel);
            fileStatesDrawable[9][1] = context.getResources().getDrawable(C0691R.drawable.file_b_cancel_s);
            photoStatesDrawables[0][0] = context.getResources().getDrawable(C0691R.drawable.photoload);
            photoStatesDrawables[0][1] = context.getResources().getDrawable(C0691R.drawable.photoload_pressed);
            photoStatesDrawables[1][0] = context.getResources().getDrawable(C0691R.drawable.photocancel);
            photoStatesDrawables[1][1] = context.getResources().getDrawable(C0691R.drawable.photocancel_pressed);
            photoStatesDrawables[2][0] = context.getResources().getDrawable(C0691R.drawable.photogif);
            photoStatesDrawables[2][1] = context.getResources().getDrawable(C0691R.drawable.photogif_pressed);
            photoStatesDrawables[3][0] = context.getResources().getDrawable(C0691R.drawable.playvideo);
            photoStatesDrawables[3][1] = context.getResources().getDrawable(C0691R.drawable.playvideo_pressed);
            Drawable[] drawableArr = photoStatesDrawables[4];
            Drawable[] drawableArr2 = photoStatesDrawables[4];
            Drawable drawable = context.getResources().getDrawable(C0691R.drawable.burn);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            drawableArr = photoStatesDrawables[5];
            drawableArr2 = photoStatesDrawables[5];
            drawable = context.getResources().getDrawable(C0691R.drawable.circle);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            drawableArr = photoStatesDrawables[6];
            drawableArr2 = photoStatesDrawables[6];
            drawable = context.getResources().getDrawable(C0691R.drawable.photocheck);
            drawableArr2[1] = drawable;
            drawableArr[0] = drawable;
            photoStatesDrawables[7][0] = context.getResources().getDrawable(C0691R.drawable.photoload_g);
            photoStatesDrawables[7][1] = context.getResources().getDrawable(C0691R.drawable.photoload_g_s);
            photoStatesDrawables[8][0] = context.getResources().getDrawable(C0691R.drawable.photocancel_g);
            photoStatesDrawables[8][1] = context.getResources().getDrawable(C0691R.drawable.photocancel_g_s);
            photoStatesDrawables[9][0] = context.getResources().getDrawable(C0691R.drawable.doc_green);
            photoStatesDrawables[9][1] = context.getResources().getDrawable(C0691R.drawable.doc_green);
            photoStatesDrawables[10][0] = context.getResources().getDrawable(C0691R.drawable.photoload_b);
            photoStatesDrawables[10][1] = context.getResources().getDrawable(C0691R.drawable.photoload_b_s);
            photoStatesDrawables[11][0] = context.getResources().getDrawable(C0691R.drawable.photocancel_b);
            photoStatesDrawables[11][1] = context.getResources().getDrawable(C0691R.drawable.photocancel_b_s);
            photoStatesDrawables[12][0] = context.getResources().getDrawable(C0691R.drawable.doc_blue);
            photoStatesDrawables[12][1] = context.getResources().getDrawable(C0691R.drawable.doc_blue_s);
            docMenuDrawable[0] = context.getResources().getDrawable(C0691R.drawable.doc_actions_b);
            docMenuDrawable[1] = context.getResources().getDrawable(C0691R.drawable.doc_actions_g);
            docMenuDrawable[2] = context.getResources().getDrawable(C0691R.drawable.doc_actions_b_s);
            docMenuDrawable[3] = context.getResources().getDrawable(C0691R.drawable.video_actions);
            contactDrawable[0] = context.getResources().getDrawable(C0691R.drawable.contact_blue);
            contactDrawable[1] = context.getResources().getDrawable(C0691R.drawable.contact_green);
            shareDrawable = context.getResources().getDrawable(C0691R.drawable.share_round);
            shareIconDrawable = context.getResources().getDrawable(C0691R.drawable.share_arrow);
            geoInDrawable = context.getResources().getDrawable(C0691R.drawable.location_b);
            geoOutDrawable = context.getResources().getDrawable(C0691R.drawable.location_g);
            cornerOuter[0] = context.getResources().getDrawable(C0691R.drawable.corner_out_tl);
            cornerOuter[1] = context.getResources().getDrawable(C0691R.drawable.corner_out_tr);
            cornerOuter[2] = context.getResources().getDrawable(C0691R.drawable.corner_out_br);
            cornerOuter[3] = context.getResources().getDrawable(C0691R.drawable.corner_out_bl);
            cornerInner[0] = context.getResources().getDrawable(C0691R.drawable.corner_in_tr);
            cornerInner[1] = context.getResources().getDrawable(C0691R.drawable.corner_in_tl);
            cornerInner[2] = context.getResources().getDrawable(C0691R.drawable.corner_in_br);
            cornerInner[3] = context.getResources().getDrawable(C0691R.drawable.corner_in_bl);
            inlineDocDrawable = context.getResources().getDrawable(C0691R.drawable.bot_file);
            inlineAudioDrawable = context.getResources().getDrawable(C0691R.drawable.bot_music);
            inlineLocationDrawable = context.getResources().getDrawable(C0691R.drawable.bot_location);
        }
        int color = ApplicationLoader.getServiceMessageColor();
        if (currentColor != color) {
            colorFilter = new PorterDuffColorFilter(color, Mode.MULTIPLY);
            colorPressedFilter = new PorterDuffColorFilter(ApplicationLoader.getServiceSelectedMessageColor(), Mode.MULTIPLY);
            currentColor = color;
            for (int a = 0; a < 4; a++) {
                cornerOuter[a].setColorFilter(colorFilter);
                cornerInner[a].setColorFilter(colorFilter);
            }
            timeStickerBackgroundDrawable.setColorFilter(colorFilter);
        }
    }

    public static void loadChatResources(Context context) {
        if (attachButtonDrawables[0] == null) {
            attachButtonDrawables[0] = context.getResources().getDrawable(C0691R.drawable.attach_camera_states);
            attachButtonDrawables[1] = context.getResources().getDrawable(C0691R.drawable.attach_gallery_states);
            attachButtonDrawables[2] = context.getResources().getDrawable(C0691R.drawable.attach_video_states);
            attachButtonDrawables[3] = context.getResources().getDrawable(C0691R.drawable.attach_audio_states);
            attachButtonDrawables[4] = context.getResources().getDrawable(C0691R.drawable.attach_file_states);
            attachButtonDrawables[5] = context.getResources().getDrawable(C0691R.drawable.attach_contact_states);
            attachButtonDrawables[6] = context.getResources().getDrawable(C0691R.drawable.attach_location_states);
            attachButtonDrawables[7] = context.getResources().getDrawable(C0691R.drawable.attach_hide_states);
        }
    }

    public static Drawable createBarSelectorDrawable(int color) {
        return createBarSelectorDrawable(color, true);
    }

    public static Drawable createBarSelectorDrawable(int color, boolean masked) {
        if (VERSION.SDK_INT >= 21) {
            Drawable maskDrawable = null;
            if (masked) {
                maskPaint.setColor(-1);
                maskDrawable = new C09151();
            }
            return new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{color}), null, maskDrawable);
        }
        Drawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{16842919}, new ColorDrawable(color));
        stateListDrawable.addState(new int[]{16842908}, new ColorDrawable(color));
        stateListDrawable.addState(new int[]{16842913}, new ColorDrawable(color));
        stateListDrawable.addState(new int[]{16843518}, new ColorDrawable(color));
        stateListDrawable.addState(new int[0], new ColorDrawable(0));
        return stateListDrawable;
    }
}
