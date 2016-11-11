package org.telegram.messenger.support.customtabs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.AnimRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.BundleCompat;
import java.util.ArrayList;

public final class CustomTabsIntent {
    public static final String EXTRA_ACTION_BUTTON_BUNDLE = "android.support.customtabs.extra.ACTION_BUTTON_BUNDLE";
    public static final String EXTRA_CLOSE_BUTTON_ICON = "android.support.customtabs.extra.CLOSE_BUTTON_ICON";
    public static final String EXTRA_DEFAULT_SHARE_MENU_ITEM = "android.support.customtabs.extra.SHARE_MENU_ITEM";
    public static final String EXTRA_ENABLE_URLBAR_HIDING = "android.support.customtabs.extra.ENABLE_URLBAR_HIDING";
    public static final String EXTRA_EXIT_ANIMATION_BUNDLE = "android.support.customtabs.extra.EXIT_ANIMATION_BUNDLE";
    public static final String EXTRA_MENU_ITEMS = "android.support.customtabs.extra.MENU_ITEMS";
    public static final String EXTRA_SECONDARY_TOOLBAR_COLOR = "android.support.customtabs.extra.SECONDARY_TOOLBAR_COLOR";
    public static final String EXTRA_SESSION = "android.support.customtabs.extra.SESSION";
    public static final String EXTRA_TINT_ACTION_BUTTON = "android.support.customtabs.extra.TINT_ACTION_BUTTON";
    public static final String EXTRA_TITLE_VISIBILITY_STATE = "android.support.customtabs.extra.TITLE_VISIBILITY";
    public static final String EXTRA_TOOLBAR_COLOR = "android.support.customtabs.extra.TOOLBAR_COLOR";
    public static final String EXTRA_TOOLBAR_ITEMS = "android.support.customtabs.extra.TOOLBAR_ITEMS";
    public static final String KEY_DESCRIPTION = "android.support.customtabs.customaction.DESCRIPTION";
    public static final String KEY_ICON = "android.support.customtabs.customaction.ICON";
    public static final String KEY_ID = "android.support.customtabs.customaction.ID";
    public static final String KEY_MENU_ITEM_TITLE = "android.support.customtabs.customaction.MENU_ITEM_TITLE";
    public static final String KEY_PENDING_INTENT = "android.support.customtabs.customaction.PENDING_INTENT";
    private static final int MAX_TOOLBAR_ITEMS = 5;
    public static final int NO_TITLE = 0;
    public static final int SHOW_PAGE_TITLE = 1;
    public static final int TOOLBAR_ACTION_BUTTON_ID = 0;
    @NonNull
    public final Intent intent;
    @Nullable
    public final Bundle startAnimationBundle;

    public static final class Builder {
        private ArrayList<Bundle> mActionButtons;
        private final Intent mIntent;
        private ArrayList<Bundle> mMenuItems;
        private Bundle mStartAnimationBundle;

        public Builder() {
            this(null);
        }

        public Builder(@Nullable CustomTabsSession session) {
            IBinder iBinder = null;
            this.mIntent = new Intent("android.intent.action.VIEW");
            this.mMenuItems = null;
            this.mStartAnimationBundle = null;
            this.mActionButtons = null;
            if (session != null) {
                this.mIntent.setPackage(session.getComponentName().getPackageName());
            }
            Bundle bundle = new Bundle();
            String str = CustomTabsIntent.EXTRA_SESSION;
            if (session != null) {
                iBinder = session.getBinder();
            }
            BundleCompat.putBinder(bundle, str, iBinder);
            this.mIntent.putExtras(bundle);
        }

        public Builder setToolbarColor(@ColorInt int color) {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_TOOLBAR_COLOR, color);
            return this;
        }

        public Builder enableUrlBarHiding() {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_ENABLE_URLBAR_HIDING, true);
            return this;
        }

        public Builder setCloseButtonIcon(@NonNull Bitmap icon) {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_CLOSE_BUTTON_ICON, icon);
            return this;
        }

        public Builder setShowTitle(boolean showTitle) {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE, showTitle ? CustomTabsIntent.SHOW_PAGE_TITLE : CustomTabsIntent.NO_TITLE);
            return this;
        }

        public Builder addMenuItem(@NonNull String label, @NonNull PendingIntent pendingIntent) {
            if (this.mMenuItems == null) {
                this.mMenuItems = new ArrayList();
            }
            Bundle bundle = new Bundle();
            bundle.putString(CustomTabsIntent.KEY_MENU_ITEM_TITLE, label);
            bundle.putParcelable(CustomTabsIntent.KEY_PENDING_INTENT, pendingIntent);
            this.mMenuItems.add(bundle);
            return this;
        }

        public Builder addDefaultShareMenuItem() {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_DEFAULT_SHARE_MENU_ITEM, true);
            return this;
        }

        public Builder setActionButton(@NonNull Bitmap icon, @NonNull String description, @NonNull PendingIntent pendingIntent, boolean shouldTint) {
            Bundle bundle = new Bundle();
            bundle.putInt(CustomTabsIntent.KEY_ID, CustomTabsIntent.NO_TITLE);
            bundle.putParcelable(CustomTabsIntent.KEY_ICON, icon);
            bundle.putString(CustomTabsIntent.KEY_DESCRIPTION, description);
            bundle.putParcelable(CustomTabsIntent.KEY_PENDING_INTENT, pendingIntent);
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_ACTION_BUTTON_BUNDLE, bundle);
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_TINT_ACTION_BUTTON, shouldTint);
            return this;
        }

        public Builder setActionButton(@NonNull Bitmap icon, @NonNull String description, @NonNull PendingIntent pendingIntent) {
            return setActionButton(icon, description, pendingIntent, false);
        }

        public Builder addToolbarItem(int id, @NonNull Bitmap icon, @NonNull String description, PendingIntent pendingIntent) throws IllegalStateException {
            if (this.mActionButtons == null) {
                this.mActionButtons = new ArrayList();
            }
            if (this.mActionButtons.size() >= CustomTabsIntent.MAX_TOOLBAR_ITEMS) {
                throw new IllegalStateException("Exceeded maximum toolbar item count of 5");
            }
            Bundle bundle = new Bundle();
            bundle.putInt(CustomTabsIntent.KEY_ID, id);
            bundle.putParcelable(CustomTabsIntent.KEY_ICON, icon);
            bundle.putString(CustomTabsIntent.KEY_DESCRIPTION, description);
            bundle.putParcelable(CustomTabsIntent.KEY_PENDING_INTENT, pendingIntent);
            this.mActionButtons.add(bundle);
            return this;
        }

        public Builder setSecondaryToolbarColor(@ColorInt int color) {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_SECONDARY_TOOLBAR_COLOR, color);
            return this;
        }

        public Builder setStartAnimations(@NonNull Context context, @AnimRes int enterResId, @AnimRes int exitResId) {
            this.mStartAnimationBundle = ActivityOptionsCompat.makeCustomAnimation(context, enterResId, exitResId).toBundle();
            return this;
        }

        public Builder setExitAnimations(@NonNull Context context, @AnimRes int enterResId, @AnimRes int exitResId) {
            this.mIntent.putExtra(CustomTabsIntent.EXTRA_EXIT_ANIMATION_BUNDLE, ActivityOptionsCompat.makeCustomAnimation(context, enterResId, exitResId).toBundle());
            return this;
        }

        public CustomTabsIntent build() {
            if (this.mMenuItems != null) {
                this.mIntent.putParcelableArrayListExtra(CustomTabsIntent.EXTRA_MENU_ITEMS, this.mMenuItems);
            }
            if (this.mActionButtons != null) {
                this.mIntent.putParcelableArrayListExtra(CustomTabsIntent.EXTRA_TOOLBAR_ITEMS, this.mActionButtons);
            }
            return new CustomTabsIntent(this.mStartAnimationBundle, null);
        }
    }

    public void launchUrl(Activity context, Uri url) {
        this.intent.setData(url);
        ActivityCompat.startActivity(context, this.intent, this.startAnimationBundle);
    }

    private CustomTabsIntent(Intent intent, Bundle startAnimationBundle) {
        this.intent = intent;
        this.startAnimationBundle = startAnimationBundle;
    }

    public static int getMaxToolbarItems() {
        return MAX_TOOLBAR_ITEMS;
    }
}
