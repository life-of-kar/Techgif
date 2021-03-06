package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.ContactsActivity.ContactsActivityDelegate;

public class BlockedUsersActivity extends BaseFragment implements NotificationCenterDelegate, ContactsActivityDelegate {
    private static final int block_user = 1;
    private TextView emptyTextView;
    private ListView listView;
    private ListAdapter listViewAdapter;
    private FrameLayout progressView;
    private int selectedUserId;

    /* renamed from: org.telegram.ui.BlockedUsersActivity.2 */
    class C09692 implements OnTouchListener {
        C09692() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: org.telegram.ui.BlockedUsersActivity.3 */
    class C09703 implements OnItemClickListener {
        C09703() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i < MessagesController.getInstance().blockedUsers.size()) {
                Bundle args = new Bundle();
                args.putInt("user_id", ((Integer) MessagesController.getInstance().blockedUsers.get(i)).intValue());
                BlockedUsersActivity.this.presentFragment(new ProfileActivity(args));
            }
        }
    }

    /* renamed from: org.telegram.ui.BlockedUsersActivity.4 */
    class C09724 implements OnItemLongClickListener {

        /* renamed from: org.telegram.ui.BlockedUsersActivity.4.1 */
        class C09711 implements OnClickListener {
            C09711() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    MessagesController.getInstance().unblockUser(BlockedUsersActivity.this.selectedUserId);
                }
            }
        }

        C09724() {
        }

        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i >= 0 && i < MessagesController.getInstance().blockedUsers.size() && BlockedUsersActivity.this.getParentActivity() != null) {
                BlockedUsersActivity.this.selectedUserId = ((Integer) MessagesController.getInstance().blockedUsers.get(i)).intValue();
                Builder builder = new Builder(BlockedUsersActivity.this.getParentActivity());
                CharSequence[] items = new CharSequence[BlockedUsersActivity.block_user];
                items[0] = LocaleController.getString("Unblock", C0691R.string.Unblock);
                builder.setItems(items, new C09711());
                BlockedUsersActivity.this.showDialog(builder.create());
            }
            return true;
        }
    }

    /* renamed from: org.telegram.ui.BlockedUsersActivity.1 */
    class C17621 extends ActionBarMenuOnItemClick {
        C17621() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                BlockedUsersActivity.this.finishFragment();
            } else if (id == BlockedUsersActivity.block_user) {
                Bundle args = new Bundle();
                args.putBoolean("onlyUsers", true);
                args.putBoolean("destroyAfterSelect", true);
                args.putBoolean("returnAsResult", true);
                ContactsActivity fragment = new ContactsActivity(args);
                fragment.setDelegate(BlockedUsersActivity.this);
                BlockedUsersActivity.this.presentFragment(fragment);
            }
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int i) {
            return i != MessagesController.getInstance().blockedUsers.size();
        }

        public int getCount() {
            if (MessagesController.getInstance().blockedUsers.isEmpty()) {
                return 0;
            }
            return MessagesController.getInstance().blockedUsers.size() + BlockedUsersActivity.block_user;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return false;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new UserCell(this.mContext, BlockedUsersActivity.block_user, 0, false);
                }
                User user = MessagesController.getInstance().getUser((Integer) MessagesController.getInstance().blockedUsers.get(i));
                if (user == null) {
                    return view;
                }
                String number;
                if (user.bot) {
                    number = LocaleController.getString("Bot", C0691R.string.Bot).substring(0, BlockedUsersActivity.block_user).toUpperCase() + LocaleController.getString("Bot", C0691R.string.Bot).substring(BlockedUsersActivity.block_user);
                } else if (user.phone == null || user.phone.length() == 0) {
                    number = LocaleController.getString("NumberUnknown", C0691R.string.NumberUnknown);
                } else {
                    number = PhoneFormat.getInstance().format("+" + user.phone);
                }
                ((UserCell) view).setData(user, null, number, 0);
                return view;
            } else if (type != BlockedUsersActivity.block_user || view != null) {
                return view;
            } else {
                view = new TextInfoCell(this.mContext);
                ((TextInfoCell) view).setText(LocaleController.getString("UnblockText", C0691R.string.UnblockText));
                return view;
            }
        }

        public int getItemViewType(int i) {
            if (i == MessagesController.getInstance().blockedUsers.size()) {
                return BlockedUsersActivity.block_user;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean isEmpty() {
            return MessagesController.getInstance().blockedUsers.isEmpty();
        }
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.blockedUsersDidLoaded);
        MessagesController.getInstance().getBlockedUsers(false);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.blockedUsersDidLoaded);
    }

    public View createView(Context context) {
        int i = block_user;
        this.actionBar.setBackButtonImage(C0691R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("BlockedUsers", C0691R.string.BlockedUsers));
        this.actionBar.setActionBarMenuOnItemClick(new C17621());
        this.actionBar.createMenu().addItem((int) block_user, (int) C0691R.drawable.plus);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.emptyTextView = new TextView(context);
        this.emptyTextView.setTextColor(-8355712);
        this.emptyTextView.setTextSize(20.0f);
        this.emptyTextView.setGravity(17);
        this.emptyTextView.setVisibility(4);
        this.emptyTextView.setText(LocaleController.getString("NoBlocked", C0691R.string.NoBlocked));
        frameLayout.addView(this.emptyTextView, LayoutHelper.createFrame(-1, -1, 51));
        this.emptyTextView.setOnTouchListener(new C09692());
        this.progressView = new FrameLayout(context);
        frameLayout.addView(this.progressView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.progressView.addView(new ProgressBar(context), LayoutHelper.createFrame(-2, -2, 17));
        this.listView = new ListView(context);
        this.listView.setEmptyView(this.emptyTextView);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        ListView listView = this.listView;
        android.widget.ListAdapter listAdapter = new ListAdapter(context);
        this.listViewAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        listView = this.listView;
        if (!LocaleController.isRTL) {
            i = 2;
        }
        listView.setVerticalScrollbarPosition(i);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.listView.setOnItemClickListener(new C09703());
        this.listView.setOnItemLongClickListener(new C09724());
        if (MessagesController.getInstance().loadingBlockedUsers) {
            this.progressView.setVisibility(0);
            this.emptyTextView.setVisibility(8);
            this.listView.setEmptyView(null);
        } else {
            this.progressView.setVisibility(8);
            this.listView.setEmptyView(this.emptyTextView);
        }
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if ((mask & 2) != 0 || (mask & block_user) != 0) {
                updateVisibleRows(mask);
            }
        } else if (id == NotificationCenter.blockedUsersDidLoaded) {
            if (this.progressView != null) {
                this.progressView.setVisibility(8);
            }
            if (this.listView != null && this.listView.getEmptyView() == null) {
                this.listView.setEmptyView(this.emptyTextView);
            }
            if (this.listViewAdapter != null) {
                this.listViewAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateVisibleRows(int mask) {
        if (this.listView != null) {
            int count = this.listView.getChildCount();
            for (int a = 0; a < count; a += block_user) {
                View child = this.listView.getChildAt(a);
                if (child instanceof UserCell) {
                    ((UserCell) child).update(mask);
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listViewAdapter != null) {
            this.listViewAdapter.notifyDataSetChanged();
        }
    }

    public void didSelectContact(User user, String param) {
        if (user != null) {
            MessagesController.getInstance().blockUser(user.id);
        }
    }
}
