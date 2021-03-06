package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC.InputUser;
import org.telegram.tgnet.TLRPC.PrivacyRule;
import org.telegram.tgnet.TLRPC.TL_account_privacyRules;
import org.telegram.tgnet.TLRPC.TL_account_setPrivacy;
import org.telegram.tgnet.TLRPC.TL_error;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyChatInvite;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyKeyStatusTimestamp;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowContacts;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_inputPrivacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueAllowUsers;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowAll;
import org.telegram.tgnet.TLRPC.TL_privacyValueDisallowUsers;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.GroupCreateActivity.GroupCreateActivityDelegate;
import org.telegram.ui.PrivacyUsersActivity.PrivacyActivityDelegate;

public class PrivacyControlActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int done_button = 1;
    private int alwaysShareRow;
    private ArrayList<Integer> currentMinus;
    private ArrayList<Integer> currentPlus;
    private int currentType;
    private int detailRow;
    private View doneButton;
    private boolean enableAnimation;
    private int everybodyRow;
    private boolean isGroup;
    private int lastCheckedType;
    private ListAdapter listAdapter;
    private int myContactsRow;
    private int neverShareRow;
    private int nobodyRow;
    private int rowCount;
    private int sectionRow;
    private int shareDetailRow;
    private int shareSectionRow;

    /* renamed from: org.telegram.ui.PrivacyControlActivity.2 */
    class C13842 implements OnItemClickListener {

        /* renamed from: org.telegram.ui.PrivacyControlActivity.2.1 */
        class C19201 implements GroupCreateActivityDelegate {
            final /* synthetic */ int val$i;

            C19201(int i) {
                this.val$i = i;
            }

            public void didSelectUsers(ArrayList<Integer> ids) {
                int a;
                if (this.val$i == PrivacyControlActivity.this.neverShareRow) {
                    PrivacyControlActivity.this.currentMinus = ids;
                    for (a = 0; a < PrivacyControlActivity.this.currentMinus.size(); a += PrivacyControlActivity.done_button) {
                        PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(a));
                    }
                } else {
                    PrivacyControlActivity.this.currentPlus = ids;
                    for (a = 0; a < PrivacyControlActivity.this.currentPlus.size(); a += PrivacyControlActivity.done_button) {
                        PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(a));
                    }
                }
                PrivacyControlActivity.this.doneButton.setVisibility(0);
                PrivacyControlActivity.this.lastCheckedType = -1;
                PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
            }
        }

        /* renamed from: org.telegram.ui.PrivacyControlActivity.2.2 */
        class C19212 implements PrivacyActivityDelegate {
            final /* synthetic */ int val$i;

            C19212(int i) {
                this.val$i = i;
            }

            public void didUpdatedUserList(ArrayList<Integer> ids, boolean added) {
                int a;
                if (this.val$i == PrivacyControlActivity.this.neverShareRow) {
                    PrivacyControlActivity.this.currentMinus = ids;
                    if (added) {
                        for (a = 0; a < PrivacyControlActivity.this.currentMinus.size(); a += PrivacyControlActivity.done_button) {
                            PrivacyControlActivity.this.currentPlus.remove(PrivacyControlActivity.this.currentMinus.get(a));
                        }
                    }
                } else {
                    PrivacyControlActivity.this.currentPlus = ids;
                    if (added) {
                        for (a = 0; a < PrivacyControlActivity.this.currentPlus.size(); a += PrivacyControlActivity.done_button) {
                            PrivacyControlActivity.this.currentMinus.remove(PrivacyControlActivity.this.currentPlus.get(a));
                        }
                    }
                }
                PrivacyControlActivity.this.doneButton.setVisibility(0);
                PrivacyControlActivity.this.listAdapter.notifyDataSetChanged();
            }
        }

        C13842() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            boolean z = false;
            if (i == PrivacyControlActivity.this.nobodyRow || i == PrivacyControlActivity.this.everybodyRow || i == PrivacyControlActivity.this.myContactsRow) {
                int newType = PrivacyControlActivity.this.currentType;
                if (i == PrivacyControlActivity.this.nobodyRow) {
                    newType = PrivacyControlActivity.done_button;
                } else if (i == PrivacyControlActivity.this.everybodyRow) {
                    newType = 0;
                } else if (i == PrivacyControlActivity.this.myContactsRow) {
                    newType = 2;
                }
                if (newType != PrivacyControlActivity.this.currentType) {
                    PrivacyControlActivity.this.enableAnimation = true;
                    PrivacyControlActivity.this.doneButton.setVisibility(0);
                    PrivacyControlActivity.this.lastCheckedType = PrivacyControlActivity.this.currentType;
                    PrivacyControlActivity.this.currentType = newType;
                    PrivacyControlActivity.this.updateRows();
                }
            } else if (i == PrivacyControlActivity.this.neverShareRow || i == PrivacyControlActivity.this.alwaysShareRow) {
                ArrayList<Integer> createFromArray;
                if (i == PrivacyControlActivity.this.neverShareRow) {
                    createFromArray = PrivacyControlActivity.this.currentMinus;
                } else {
                    createFromArray = PrivacyControlActivity.this.currentPlus;
                }
                if (createFromArray.isEmpty()) {
                    Bundle args = new Bundle();
                    args.putBoolean(i == PrivacyControlActivity.this.neverShareRow ? "isNeverShare" : "isAlwaysShare", true);
                    args.putBoolean("isGroup", PrivacyControlActivity.this.isGroup);
                    GroupCreateActivity fragment = new GroupCreateActivity(args);
                    fragment.setDelegate(new C19201(i));
                    PrivacyControlActivity.this.presentFragment(fragment);
                    return;
                }
                boolean access$100 = PrivacyControlActivity.this.isGroup;
                if (i == PrivacyControlActivity.this.alwaysShareRow) {
                    z = true;
                }
                PrivacyUsersActivity fragment2 = new PrivacyUsersActivity(createFromArray, access$100, z);
                fragment2.setDelegate(new C19212(i));
                PrivacyControlActivity.this.presentFragment(fragment2);
            }
        }
    }

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        private LinkMovementMethodMy() {
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
                return false;
            }
        }
    }

    /* renamed from: org.telegram.ui.PrivacyControlActivity.1 */
    class C19191 extends ActionBarMenuOnItemClick {

        /* renamed from: org.telegram.ui.PrivacyControlActivity.1.1 */
        class C13831 implements OnClickListener {
            final /* synthetic */ SharedPreferences val$preferences;

            C13831(SharedPreferences sharedPreferences) {
                this.val$preferences = sharedPreferences;
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                PrivacyControlActivity.this.applyCurrentPrivacySettings();
                this.val$preferences.edit().putBoolean("privacyAlertShowed", true).commit();
            }
        }

        C19191() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PrivacyControlActivity.this.finishFragment();
            } else if (id == PrivacyControlActivity.done_button && PrivacyControlActivity.this.getParentActivity() != null) {
                if (!(PrivacyControlActivity.this.currentType == 0 || PrivacyControlActivity.this.isGroup)) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (!preferences.getBoolean("privacyAlertShowed", false)) {
                        Builder builder = new Builder(PrivacyControlActivity.this.getParentActivity());
                        if (PrivacyControlActivity.this.isGroup) {
                            builder.setMessage(LocaleController.getString("WhoCanAddMeInfo", C0691R.string.WhoCanAddMeInfo));
                        } else {
                            builder.setMessage(LocaleController.getString("CustomHelp", C0691R.string.CustomHelp));
                        }
                        builder.setTitle(LocaleController.getString("AppName", C0691R.string.AppName));
                        builder.setPositiveButton(LocaleController.getString("OK", C0691R.string.OK), new C13831(preferences));
                        builder.setNegativeButton(LocaleController.getString("Cancel", C0691R.string.Cancel), null);
                        PrivacyControlActivity.this.showDialog(builder.create());
                        return;
                    }
                }
                PrivacyControlActivity.this.applyCurrentPrivacySettings();
            }
        }
    }

    /* renamed from: org.telegram.ui.PrivacyControlActivity.3 */
    class C19223 implements RequestDelegate {
        final /* synthetic */ ProgressDialog val$progressDialogFinal;

        /* renamed from: org.telegram.ui.PrivacyControlActivity.3.1 */
        class C13851 implements Runnable {
            final /* synthetic */ TL_error val$error;
            final /* synthetic */ TLObject val$response;

            C13851(TL_error tL_error, TLObject tLObject) {
                this.val$error = tL_error;
                this.val$response = tLObject;
            }

            public void run() {
                try {
                    if (C19223.this.val$progressDialogFinal != null) {
                        C19223.this.val$progressDialogFinal.dismiss();
                    }
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
                if (this.val$error == null) {
                    PrivacyControlActivity.this.finishFragment();
                    TL_account_privacyRules rules = this.val$response;
                    MessagesController.getInstance().putUsers(rules.users, false);
                    ContactsController.getInstance().setPrivacyRules(rules.rules, PrivacyControlActivity.this.isGroup);
                    return;
                }
                PrivacyControlActivity.this.showErrorAlert();
            }
        }

        C19223(ProgressDialog progressDialog) {
            this.val$progressDialogFinal = progressDialog;
        }

        public void run(TLObject response, TL_error error) {
            AndroidUtilities.runOnUIThread(new C13851(error, response));
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
            return i == PrivacyControlActivity.this.nobodyRow || i == PrivacyControlActivity.this.everybodyRow || i == PrivacyControlActivity.this.myContactsRow || i == PrivacyControlActivity.this.neverShareRow || i == PrivacyControlActivity.this.alwaysShareRow;
        }

        public int getCount() {
            return PrivacyControlActivity.this.rowCount;
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
            boolean z = true;
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                String value;
                if (i == PrivacyControlActivity.this.alwaysShareRow) {
                    if (PrivacyControlActivity.this.currentPlus.size() != 0) {
                        value = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentPlus.size());
                    } else {
                        value = LocaleController.getString("EmpryUsersPlaceholder", C0691R.string.EmpryUsersPlaceholder);
                    }
                    String string;
                    if (PrivacyControlActivity.this.isGroup) {
                        string = LocaleController.getString("AlwaysAllow", C0691R.string.AlwaysAllow);
                        if (PrivacyControlActivity.this.neverShareRow == -1) {
                            z = false;
                        }
                        textCell.setTextAndValue(string, value, z);
                    } else {
                        string = LocaleController.getString("AlwaysShareWith", C0691R.string.AlwaysShareWith);
                        if (PrivacyControlActivity.this.neverShareRow == -1) {
                            z = false;
                        }
                        textCell.setTextAndValue(string, value, z);
                    }
                } else if (i == PrivacyControlActivity.this.neverShareRow) {
                    if (PrivacyControlActivity.this.currentMinus.size() != 0) {
                        value = LocaleController.formatPluralString("Users", PrivacyControlActivity.this.currentMinus.size());
                    } else {
                        value = LocaleController.getString("EmpryUsersPlaceholder", C0691R.string.EmpryUsersPlaceholder);
                    }
                    if (PrivacyControlActivity.this.isGroup) {
                        textCell.setTextAndValue(LocaleController.getString("NeverAllow", C0691R.string.NeverAllow), value, false);
                    } else {
                        textCell.setTextAndValue(LocaleController.getString("NeverShareWith", C0691R.string.NeverShareWith), value, false);
                    }
                }
            } else if (type == PrivacyControlActivity.done_button) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                if (i == PrivacyControlActivity.this.detailRow) {
                    if (PrivacyControlActivity.this.isGroup) {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("WhoCanAddMeInfo", C0691R.string.WhoCanAddMeInfo));
                    } else {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomHelp", C0691R.string.CustomHelp));
                    }
                    view.setBackgroundResource(C0691R.drawable.greydivider);
                } else if (i == PrivacyControlActivity.this.shareDetailRow) {
                    if (PrivacyControlActivity.this.isGroup) {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomShareInfo", C0691R.string.CustomShareInfo));
                    } else {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("CustomShareSettingsHelp", C0691R.string.CustomShareSettingsHelp));
                    }
                    view.setBackgroundResource(C0691R.drawable.greydivider_bottom);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new HeaderCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                if (i == PrivacyControlActivity.this.sectionRow) {
                    if (PrivacyControlActivity.this.isGroup) {
                        ((HeaderCell) view).setText(LocaleController.getString("WhoCanAddMe", C0691R.string.WhoCanAddMe));
                    } else {
                        ((HeaderCell) view).setText(LocaleController.getString("LastSeenTitle", C0691R.string.LastSeenTitle));
                    }
                } else if (i == PrivacyControlActivity.this.shareSectionRow) {
                    ((HeaderCell) view).setText(LocaleController.getString("AddExceptions", C0691R.string.AddExceptions));
                }
            } else if (type == 3) {
                if (view == null) {
                    view = new RadioCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                RadioCell textCell2 = (RadioCell) view;
                int checkedType = 0;
                if (i == PrivacyControlActivity.this.everybodyRow) {
                    textCell2.setText(LocaleController.getString("LastSeenEverybody", C0691R.string.LastSeenEverybody), PrivacyControlActivity.this.lastCheckedType == 0, true);
                    checkedType = 0;
                } else if (i == PrivacyControlActivity.this.myContactsRow) {
                    String string2 = LocaleController.getString("LastSeenContacts", C0691R.string.LastSeenContacts);
                    if (PrivacyControlActivity.this.lastCheckedType == 2) {
                        r6 = true;
                    } else {
                        r6 = false;
                    }
                    textCell2.setText(string2, r6, PrivacyControlActivity.this.nobodyRow != -1);
                    checkedType = 2;
                } else if (i == PrivacyControlActivity.this.nobodyRow) {
                    String string3 = LocaleController.getString("LastSeenNobody", C0691R.string.LastSeenNobody);
                    if (PrivacyControlActivity.this.lastCheckedType == PrivacyControlActivity.done_button) {
                        r6 = true;
                    } else {
                        r6 = false;
                    }
                    textCell2.setText(string3, r6, false);
                    checkedType = PrivacyControlActivity.done_button;
                }
                if (PrivacyControlActivity.this.lastCheckedType == checkedType) {
                    textCell2.setChecked(false, PrivacyControlActivity.this.enableAnimation);
                } else if (PrivacyControlActivity.this.currentType == checkedType) {
                    textCell2.setChecked(true, PrivacyControlActivity.this.enableAnimation);
                }
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == PrivacyControlActivity.this.alwaysShareRow || i == PrivacyControlActivity.this.neverShareRow) {
                return 0;
            }
            if (i == PrivacyControlActivity.this.shareDetailRow || i == PrivacyControlActivity.this.detailRow) {
                return PrivacyControlActivity.done_button;
            }
            if (i == PrivacyControlActivity.this.sectionRow || i == PrivacyControlActivity.this.shareSectionRow) {
                return 2;
            }
            if (i == PrivacyControlActivity.this.everybodyRow || i == PrivacyControlActivity.this.myContactsRow || i == PrivacyControlActivity.this.nobodyRow) {
                return 3;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 4;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public PrivacyControlActivity(boolean group) {
        this.currentType = 0;
        this.lastCheckedType = -1;
        this.isGroup = group;
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        checkPrivacy();
        updateRows();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.privacyRulesUpdated);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.privacyRulesUpdated);
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0691R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        if (this.isGroup) {
            this.actionBar.setTitle(LocaleController.getString("GroupsAndChannels", C0691R.string.GroupsAndChannels));
        } else {
            this.actionBar.setTitle(LocaleController.getString("PrivacyLastSeen", C0691R.string.PrivacyLastSeen));
        }
        this.actionBar.setActionBarMenuOnItemClick(new C19191());
        this.doneButton = this.actionBar.createMenu().addItemWithWidth(done_button, C0691R.drawable.ic_done, AndroidUtilities.dp(56.0f));
        this.doneButton.setVisibility(8);
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(-986896);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDrawSelectorOnTop(true);
        frameLayout.addView(listView);
        LayoutParams layoutParams = (LayoutParams) listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.gravity = 48;
        listView.setLayoutParams(layoutParams);
        listView.setAdapter(this.listAdapter);
        listView.setOnItemClickListener(new C13842());
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.privacyRulesUpdated) {
            checkPrivacy();
        }
    }

    private void applyCurrentPrivacySettings() {
        int a;
        User user;
        InputUser inputUser;
        TL_account_setPrivacy req = new TL_account_setPrivacy();
        if (this.isGroup) {
            req.key = new TL_inputPrivacyKeyChatInvite();
        } else {
            req.key = new TL_inputPrivacyKeyStatusTimestamp();
        }
        if (this.currentType != 0 && this.currentPlus.size() > 0) {
            TL_inputPrivacyValueAllowUsers rule = new TL_inputPrivacyValueAllowUsers();
            for (a = 0; a < this.currentPlus.size(); a += done_button) {
                user = MessagesController.getInstance().getUser((Integer) this.currentPlus.get(a));
                if (user != null) {
                    inputUser = MessagesController.getInputUser(user);
                    if (inputUser != null) {
                        rule.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule);
        }
        if (this.currentType != done_button && this.currentMinus.size() > 0) {
            TL_inputPrivacyValueDisallowUsers rule2 = new TL_inputPrivacyValueDisallowUsers();
            for (a = 0; a < this.currentMinus.size(); a += done_button) {
                user = MessagesController.getInstance().getUser((Integer) this.currentMinus.get(a));
                if (user != null) {
                    inputUser = MessagesController.getInputUser(user);
                    if (inputUser != null) {
                        rule2.users.add(inputUser);
                    }
                }
            }
            req.rules.add(rule2);
        }
        if (this.currentType == 0) {
            req.rules.add(new TL_inputPrivacyValueAllowAll());
        } else if (this.currentType == done_button) {
            req.rules.add(new TL_inputPrivacyValueDisallowAll());
        } else if (this.currentType == 2) {
            req.rules.add(new TL_inputPrivacyValueAllowContacts());
        }
        ProgressDialog progressDialog = null;
        if (getParentActivity() != null) {
            progressDialog = new ProgressDialog(getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", C0691R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        ConnectionsManager.getInstance().sendRequest(req, new C19223(progressDialog), 2);
    }

    private void showErrorAlert() {
        if (getParentActivity() != null) {
            Builder builder = new Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", C0691R.string.AppName));
            builder.setMessage(LocaleController.getString("PrivacyFloodControlError", C0691R.string.PrivacyFloodControlError));
            builder.setPositiveButton(LocaleController.getString("OK", C0691R.string.OK), null);
            showDialog(builder.create());
        }
    }

    private void checkPrivacy() {
        this.currentPlus = new ArrayList();
        this.currentMinus = new ArrayList();
        ArrayList<PrivacyRule> privacyRules = ContactsController.getInstance().getPrivacyRules(this.isGroup);
        if (privacyRules.size() == 0) {
            this.currentType = done_button;
            return;
        }
        int type = -1;
        for (int a = 0; a < privacyRules.size(); a += done_button) {
            PrivacyRule rule = (PrivacyRule) privacyRules.get(a);
            if (rule instanceof TL_privacyValueAllowUsers) {
                this.currentPlus.addAll(rule.users);
            } else if (rule instanceof TL_privacyValueDisallowUsers) {
                this.currentMinus.addAll(rule.users);
            } else if (rule instanceof TL_privacyValueAllowAll) {
                type = 0;
            } else if (rule instanceof TL_privacyValueDisallowAll) {
                type = done_button;
            } else {
                type = 2;
            }
        }
        if (type == 0 || (type == -1 && this.currentMinus.size() > 0)) {
            this.currentType = 0;
        } else if (type == 2 || (type == -1 && this.currentMinus.size() > 0 && this.currentPlus.size() > 0)) {
            this.currentType = 2;
        } else if (type == done_button || (type == -1 && this.currentPlus.size() > 0)) {
            this.currentType = done_button;
        }
        if (this.doneButton != null) {
            this.doneButton.setVisibility(8);
        }
        updateRows();
    }

    private void updateRows() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + done_button;
        this.sectionRow = i;
        i = this.rowCount;
        this.rowCount = i + done_button;
        this.everybodyRow = i;
        i = this.rowCount;
        this.rowCount = i + done_button;
        this.myContactsRow = i;
        if (this.isGroup) {
            this.nobodyRow = -1;
        } else {
            i = this.rowCount;
            this.rowCount = i + done_button;
            this.nobodyRow = i;
        }
        i = this.rowCount;
        this.rowCount = i + done_button;
        this.detailRow = i;
        i = this.rowCount;
        this.rowCount = i + done_button;
        this.shareSectionRow = i;
        if (this.currentType == done_button || this.currentType == 2) {
            i = this.rowCount;
            this.rowCount = i + done_button;
            this.alwaysShareRow = i;
        } else {
            this.alwaysShareRow = -1;
        }
        if (this.currentType == 0 || this.currentType == 2) {
            i = this.rowCount;
            this.rowCount = i + done_button;
            this.neverShareRow = i;
        } else {
            this.neverShareRow = -1;
        }
        i = this.rowCount;
        this.rowCount = i + done_button;
        this.shareDetailRow = i;
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void onResume() {
        super.onResume();
        this.lastCheckedType = -1;
        this.enableAnimation = false;
    }
}
