package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.exoplayer.C0747C;
import org.telegram.messenger.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.TL_channelFull;
import org.telegram.tgnet.TLRPC.TL_chatFull;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.ProfileActivity;

public class ChatAvatarContainer extends FrameLayout {
    private AvatarDrawable avatarDrawable;
    private BackupImageView avatarImageView;
    private int onlineCount;
    private ChatActivity parentFragment;
    private RecordStatusDrawable recordStatusDrawable;
    private SendingFileExDrawable sendingFileDrawable;
    private SimpleTextView subtitleTextView;
    private ImageView timeItem;
    private TimerDrawable timerDrawable;
    private SimpleTextView titleTextView;
    private TypingDotsDrawable typingDotsDrawable;

    /* renamed from: org.telegram.ui.Components.ChatAvatarContainer.1 */
    class C11251 implements OnClickListener {
        C11251() {
        }

        public void onClick(View v) {
            ChatAvatarContainer.this.parentFragment.showDialog(AndroidUtilities.buildTTLAlert(ChatAvatarContainer.this.getContext(), ChatAvatarContainer.this.parentFragment.getCurrentEncryptedChat()).create());
        }
    }

    /* renamed from: org.telegram.ui.Components.ChatAvatarContainer.2 */
    class C11262 implements OnClickListener {
        C11262() {
        }

        public void onClick(View v) {
            User user = ChatAvatarContainer.this.parentFragment.getCurrentUser();
            Chat chat = ChatAvatarContainer.this.parentFragment.getCurrentChat();
            Bundle args;
            ProfileActivity fragment;
            if (user != null) {
                args = new Bundle();
                args.putInt("user_id", user.id);
                if (ChatAvatarContainer.this.timeItem != null) {
                    args.putLong("dialog_id", ChatAvatarContainer.this.parentFragment.getDialogId());
                }
                fragment = new ProfileActivity(args);
                fragment.setPlayProfileAnimation(true);
                ChatAvatarContainer.this.parentFragment.presentFragment(fragment);
            } else if (chat != null) {
                args = new Bundle();
                args.putInt("chat_id", chat.id);
                fragment = new ProfileActivity(args);
                fragment.setChatInfo(ChatAvatarContainer.this.parentFragment.getCurrentChatInfo());
                fragment.setPlayProfileAnimation(true);
                ChatAvatarContainer.this.parentFragment.presentFragment(fragment);
            }
        }
    }

    public ChatAvatarContainer(Context context, ChatActivity chatActivity, boolean needTime) {
        boolean z;
        boolean z2 = true;
        super(context);
        this.avatarDrawable = new AvatarDrawable();
        this.onlineCount = -1;
        this.parentFragment = chatActivity;
        this.avatarImageView = new BackupImageView(context);
        this.avatarImageView.setRoundRadius(AndroidUtilities.dp(21.0f));
        addView(this.avatarImageView);
        this.titleTextView = new SimpleTextView(context);
        this.titleTextView.setTextColor(Theme.ACTION_BAR_TITLE_COLOR);
        this.titleTextView.setTextSize(18);
        this.titleTextView.setGravity(3);
        this.titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        this.titleTextView.setLeftDrawableTopPadding(-AndroidUtilities.dp(1.3f));
        this.titleTextView.setRightDrawableTopPadding(-AndroidUtilities.dp(1.3f));
        addView(this.titleTextView);
        this.subtitleTextView = new SimpleTextView(context);
        this.subtitleTextView.setTextColor(Theme.ACTION_BAR_SUBTITLE_COLOR);
        this.subtitleTextView.setTextSize(14);
        this.subtitleTextView.setGravity(3);
        addView(this.subtitleTextView);
        if (needTime) {
            this.timeItem = new ImageView(context);
            this.timeItem.setPadding(AndroidUtilities.dp(10.0f), AndroidUtilities.dp(10.0f), AndroidUtilities.dp(5.0f), AndroidUtilities.dp(5.0f));
            this.timeItem.setScaleType(ScaleType.CENTER);
            ImageView imageView = this.timeItem;
            Drawable timerDrawable = new TimerDrawable(context);
            this.timerDrawable = timerDrawable;
            imageView.setImageDrawable(timerDrawable);
            addView(this.timeItem);
            this.timeItem.setOnClickListener(new C11251());
        }
        setOnClickListener(new C11262());
        Chat chat = this.parentFragment.getCurrentChat();
        this.typingDotsDrawable = new TypingDotsDrawable();
        this.typingDotsDrawable.setIsChat(chat != null);
        this.recordStatusDrawable = new RecordStatusDrawable();
        RecordStatusDrawable recordStatusDrawable = this.recordStatusDrawable;
        if (chat != null) {
            z = true;
        } else {
            z = false;
        }
        recordStatusDrawable.setIsChat(z);
        this.sendingFileDrawable = new SendingFileExDrawable();
        SendingFileExDrawable sendingFileExDrawable = this.sendingFileDrawable;
        if (chat == null) {
            z2 = false;
        }
        sendingFileExDrawable.setIsChat(z2);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = width - AndroidUtilities.dp(70.0f);
        this.avatarImageView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42.0f), C0747C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(42.0f), C0747C.ENCODING_PCM_32BIT));
        this.titleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, LinearLayoutManager.INVALID_OFFSET), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24.0f), LinearLayoutManager.INVALID_OFFSET));
        this.subtitleTextView.measure(MeasureSpec.makeMeasureSpec(availableWidth, LinearLayoutManager.INVALID_OFFSET), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(20.0f), LinearLayoutManager.INVALID_OFFSET));
        if (this.timeItem != null) {
            this.timeItem.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(34.0f), C0747C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(34.0f), C0747C.ENCODING_PCM_32BIT));
        }
        setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int viewTop = ((ActionBar.getCurrentActionBarHeight() - AndroidUtilities.dp(42.0f)) / 2) + (VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
        this.avatarImageView.layout(AndroidUtilities.dp(8.0f), viewTop, AndroidUtilities.dp(50.0f), AndroidUtilities.dp(42.0f) + viewTop);
        this.titleTextView.layout(AndroidUtilities.dp(62.0f), AndroidUtilities.dp(1.3f) + viewTop, AndroidUtilities.dp(62.0f) + this.titleTextView.getMeasuredWidth(), (this.titleTextView.getTextHeight() + viewTop) + AndroidUtilities.dp(1.3f));
        if (this.timeItem != null) {
            this.timeItem.layout(AndroidUtilities.dp(24.0f), AndroidUtilities.dp(15.0f) + viewTop, AndroidUtilities.dp(58.0f), AndroidUtilities.dp(49.0f) + viewTop);
        }
        this.subtitleTextView.layout(AndroidUtilities.dp(62.0f), AndroidUtilities.dp(24.0f) + viewTop, AndroidUtilities.dp(62.0f) + this.subtitleTextView.getMeasuredWidth(), (this.subtitleTextView.getTextHeight() + viewTop) + AndroidUtilities.dp(24.0f));
    }

    public void showTimeItem() {
        if (this.timeItem != null) {
            this.timeItem.setVisibility(0);
        }
    }

    public void hideTimeItem() {
        if (this.timeItem != null) {
            this.timeItem.setVisibility(8);
        }
    }

    public void setTime(int value) {
        if (this.timerDrawable != null) {
            this.timerDrawable.setTime(value);
        }
    }

    public void setTitleIcons(int leftIcon, int rightIcon) {
        this.titleTextView.setLeftDrawable(leftIcon);
        this.titleTextView.setRightDrawable(rightIcon);
    }

    public void setTitle(CharSequence value) {
        this.titleTextView.setText(value);
    }

    private void setTypingAnimation(boolean start) {
        if (start) {
            try {
                Integer type = (Integer) MessagesController.getInstance().printingStringsTypes.get(Long.valueOf(this.parentFragment.getDialogId()));
                if (type.intValue() == 0) {
                    this.subtitleTextView.setLeftDrawable(this.typingDotsDrawable);
                    this.typingDotsDrawable.start();
                    this.recordStatusDrawable.stop();
                    this.sendingFileDrawable.stop();
                    return;
                } else if (type.intValue() == 1) {
                    this.subtitleTextView.setLeftDrawable(this.recordStatusDrawable);
                    this.recordStatusDrawable.start();
                    this.typingDotsDrawable.stop();
                    this.sendingFileDrawable.stop();
                    return;
                } else if (type.intValue() == 2) {
                    this.subtitleTextView.setLeftDrawable(this.sendingFileDrawable);
                    this.sendingFileDrawable.start();
                    this.typingDotsDrawable.stop();
                    this.recordStatusDrawable.stop();
                    return;
                } else {
                    return;
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                return;
            }
        }
        this.subtitleTextView.setLeftDrawable(null);
        this.typingDotsDrawable.stop();
        this.recordStatusDrawable.stop();
        this.sendingFileDrawable.stop();
    }

    public void updateSubtitle() {
        User user = this.parentFragment.getCurrentUser();
        Chat chat = this.parentFragment.getCurrentChat();
        CharSequence printString = (CharSequence) MessagesController.getInstance().printingStrings.get(Long.valueOf(this.parentFragment.getDialogId()));
        if (printString != null) {
            printString = TextUtils.replace(printString, new String[]{"..."}, new String[]{TtmlNode.ANONYMOUS_REGION_ID});
        }
        if (printString == null || printString.length() == 0 || (ChatObject.isChannel(chat) && !chat.megagroup)) {
            setTypingAnimation(false);
            if (chat != null) {
                ChatFull info = this.parentFragment.getCurrentChatInfo();
                if (ChatObject.isChannel(chat)) {
                    if (info == null || info.participants_count == 0) {
                        if (chat.megagroup) {
                            this.subtitleTextView.setText(LocaleController.getString("Loading", C0691R.string.Loading).toLowerCase());
                            return;
                        } else if ((chat.flags & 64) != 0) {
                            this.subtitleTextView.setText(LocaleController.getString("ChannelPublic", C0691R.string.ChannelPublic).toLowerCase());
                            return;
                        } else {
                            this.subtitleTextView.setText(LocaleController.getString("ChannelPrivate", C0691R.string.ChannelPrivate).toLowerCase());
                            return;
                        }
                    } else if (!chat.megagroup || info.participants_count > Callback.DEFAULT_DRAG_ANIMATION_DURATION) {
                        int[] result = new int[1];
                        String shortNumber = LocaleController.formatShortNumber(info.participants_count, result);
                        this.subtitleTextView.setText(LocaleController.formatPluralString("Members", result[0]).replace(String.format("%d", new Object[]{Integer.valueOf(result[0])}), shortNumber));
                        return;
                    } else if (this.onlineCount <= 1 || info.participants_count == 0) {
                        this.subtitleTextView.setText(LocaleController.formatPluralString("Members", info.participants_count));
                        return;
                    } else {
                        this.subtitleTextView.setText(String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", info.participants_count), LocaleController.formatPluralString("Online", this.onlineCount)}));
                        return;
                    }
                } else if (ChatObject.isKickedFromChat(chat)) {
                    this.subtitleTextView.setText(LocaleController.getString("YouWereKicked", C0691R.string.YouWereKicked));
                    return;
                } else if (ChatObject.isLeftFromChat(chat)) {
                    this.subtitleTextView.setText(LocaleController.getString("YouLeft", C0691R.string.YouLeft));
                    return;
                } else {
                    int count = chat.participants_count;
                    if (!(info == null || info.participants == null)) {
                        count = info.participants.participants.size();
                    }
                    if (this.onlineCount <= 1 || count == 0) {
                        this.subtitleTextView.setText(LocaleController.formatPluralString("Members", count));
                        return;
                    }
                    this.subtitleTextView.setText(String.format("%s, %s", new Object[]{LocaleController.formatPluralString("Members", count), LocaleController.formatPluralString("Online", this.onlineCount)}));
                    return;
                }
            } else if (user != null) {
                String newStatus;
                user = MessagesController.getInstance().getUser(Integer.valueOf(user.id));
                if (user.id == 333000 || user.id == 777000) {
                    newStatus = LocaleController.getString("ServiceNotifications", C0691R.string.ServiceNotifications);
                } else if (user.bot) {
                    newStatus = LocaleController.getString("Bot", C0691R.string.Bot);
                } else {
                    newStatus = LocaleController.formatUserStatus(user);
                }
                this.subtitleTextView.setText(newStatus);
                return;
            } else {
                return;
            }
        }
        this.subtitleTextView.setText(printString);
        setTypingAnimation(true);
    }

    public void checkAndUpdateAvatar() {
        TLObject newPhoto = null;
        User user = this.parentFragment.getCurrentUser();
        Chat chat = this.parentFragment.getCurrentChat();
        if (user != null) {
            if (user.photo != null) {
                newPhoto = user.photo.photo_small;
            }
            this.avatarDrawable.setInfo(user);
        } else if (chat != null) {
            if (chat.photo != null) {
                newPhoto = chat.photo.photo_small;
            }
            this.avatarDrawable.setInfo(chat);
        }
        if (this.avatarImageView != null) {
            this.avatarImageView.setImage(newPhoto, "50_50", this.avatarDrawable);
        }
    }

    public void updateOnlineCount() {
        this.onlineCount = 0;
        ChatFull info = this.parentFragment.getCurrentChatInfo();
        if (info != null) {
            int currentTime = ConnectionsManager.getInstance().getCurrentTime();
            if ((info instanceof TL_chatFull) || ((info instanceof TL_channelFull) && info.participants_count <= Callback.DEFAULT_DRAG_ANIMATION_DURATION && info.participants != null)) {
                for (int a = 0; a < info.participants.participants.size(); a++) {
                    User user = MessagesController.getInstance().getUser(Integer.valueOf(((ChatParticipant) info.participants.participants.get(a)).user_id));
                    if (!(user == null || user.status == null || ((user.status.expires <= currentTime && user.id != UserConfig.getClientUserId()) || user.status.expires <= AdaptiveEvaluator.DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS))) {
                        this.onlineCount++;
                    }
                }
            }
        }
    }
}
