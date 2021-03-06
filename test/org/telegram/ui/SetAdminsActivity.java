package org.telegram.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC.Chat;
import org.telegram.tgnet.TLRPC.ChatFull;
import org.telegram.tgnet.TLRPC.ChatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipant;
import org.telegram.tgnet.TLRPC.TL_chatParticipantAdmin;
import org.telegram.tgnet.TLRPC.TL_chatParticipantCreator;
import org.telegram.tgnet.TLRPC.User;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuItem.ActionBarMenuItemSearchListener;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.LayoutHelper;

public class SetAdminsActivity extends BaseFragment implements NotificationCenterDelegate {
    private int allAdminsInfoRow;
    private int allAdminsRow;
    private Chat chat;
    private int chat_id;
    private ChatFull info;
    private ListAdapter listAdapter;
    private ListView listView;
    private ArrayList<ChatParticipant> participants;
    private int rowCount;
    private SearchAdapter searchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private ActionBarMenuItem searchItem;
    private boolean searchWas;
    private boolean searching;
    private int usersEndRow;
    private int usersStartRow;

    /* renamed from: org.telegram.ui.SetAdminsActivity.3 */
    class C14263 implements OnItemClickListener {
        C14263() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter || (i >= SetAdminsActivity.this.usersStartRow && i < SetAdminsActivity.this.usersEndRow)) {
                ChatParticipant participant;
                UserCell userCell = (UserCell) view;
                SetAdminsActivity.this.chat = MessagesController.getInstance().getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                int index = -1;
                if (SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter) {
                    participant = SetAdminsActivity.this.searchAdapter.getItem(i);
                    for (int a = 0; a < SetAdminsActivity.this.participants.size(); a++) {
                        if (((ChatParticipant) SetAdminsActivity.this.participants.get(a)).user_id == participant.user_id) {
                            index = a;
                            break;
                        }
                    }
                } else {
                    index = i - SetAdminsActivity.this.usersStartRow;
                    participant = (ChatParticipant) SetAdminsActivity.this.participants.get(index);
                }
                if (index != -1 && !(participant instanceof TL_chatParticipantCreator)) {
                    ChatParticipant newParticipant;
                    if (participant instanceof TL_chatParticipant) {
                        newParticipant = new TL_chatParticipantAdmin();
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                    } else {
                        newParticipant = new TL_chatParticipant();
                        newParticipant.user_id = participant.user_id;
                        newParticipant.date = participant.date;
                        newParticipant.inviter_id = participant.inviter_id;
                    }
                    SetAdminsActivity.this.participants.set(index, newParticipant);
                    index = SetAdminsActivity.this.info.participants.participants.indexOf(participant);
                    if (index != -1) {
                        SetAdminsActivity.this.info.participants.participants.set(index, newParticipant);
                    }
                    if (SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter) {
                        SetAdminsActivity.this.searchAdapter.searchResult.set(i, newParticipant);
                    }
                    participant = newParticipant;
                    boolean z = ((participant instanceof TL_chatParticipant) && (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled)) ? false : true;
                    userCell.setChecked(z, true);
                    if (SetAdminsActivity.this.chat != null && SetAdminsActivity.this.chat.admins_enabled) {
                        MessagesController instance = MessagesController.getInstance();
                        int access$1100 = SetAdminsActivity.this.chat_id;
                        int i2 = participant.user_id;
                        if (participant instanceof TL_chatParticipant) {
                            z = false;
                        } else {
                            z = true;
                        }
                        instance.toggleUserAdmin(access$1100, i2, z);
                    }
                }
            } else if (i == SetAdminsActivity.this.allAdminsRow) {
                SetAdminsActivity.this.chat = MessagesController.getInstance().getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                if (SetAdminsActivity.this.chat != null) {
                    SetAdminsActivity.this.chat.admins_enabled = !SetAdminsActivity.this.chat.admins_enabled;
                    ((TextCheckCell) view).setChecked(!SetAdminsActivity.this.chat.admins_enabled);
                    MessagesController.getInstance().toggleAdminMode(SetAdminsActivity.this.chat_id, SetAdminsActivity.this.chat.admins_enabled);
                }
            }
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity.4 */
    class C14274 implements Comparator<ChatParticipant> {
        C14274() {
        }

        public int compare(ChatParticipant lhs, ChatParticipant rhs) {
            int type1 = SetAdminsActivity.this.getChatAdminParticipantType(lhs);
            int type2 = SetAdminsActivity.this.getChatAdminParticipantType(rhs);
            if (type1 > type2) {
                return 1;
            }
            if (type1 < type2) {
                return -1;
            }
            if (type1 == type2) {
                User user1 = MessagesController.getInstance().getUser(Integer.valueOf(rhs.user_id));
                User user2 = MessagesController.getInstance().getUser(Integer.valueOf(lhs.user_id));
                int status1 = 0;
                int status2 = 0;
                if (!(user1 == null || user1.status == null)) {
                    status1 = user1.status.expires;
                }
                if (!(user2 == null || user2.status == null)) {
                    status2 = user2.status.expires;
                }
                if (status1 <= 0 || status2 <= 0) {
                    if (status1 >= 0 || status2 >= 0) {
                        if ((status1 < 0 && status2 > 0) || (status1 == 0 && status2 != 0)) {
                            return -1;
                        }
                        if (status2 < 0 && status1 > 0) {
                            return 1;
                        }
                        if (status2 == 0 && status1 != 0) {
                            return 1;
                        }
                    } else if (status1 > status2) {
                        return 1;
                    } else {
                        if (status1 < status2) {
                            return -1;
                        }
                        return 0;
                    }
                } else if (status1 > status2) {
                    return 1;
                } else {
                    if (status1 < status2) {
                        return -1;
                    }
                    return 0;
                }
            }
            return 0;
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity.1 */
    class C19451 extends ActionBarMenuOnItemClick {
        C19451() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                SetAdminsActivity.this.finishFragment();
            }
        }
    }

    /* renamed from: org.telegram.ui.SetAdminsActivity.2 */
    class C19462 extends ActionBarMenuItemSearchListener {
        C19462() {
        }

        public void onSearchExpand() {
            SetAdminsActivity.this.searching = true;
            SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.searchEmptyView);
        }

        public void onSearchCollapse() {
            SetAdminsActivity.this.searching = false;
            SetAdminsActivity.this.searchWas = false;
            if (SetAdminsActivity.this.listView != null) {
                SetAdminsActivity.this.listView.setEmptyView(null);
                SetAdminsActivity.this.searchEmptyView.setVisibility(8);
                if (SetAdminsActivity.this.listView.getAdapter() != SetAdminsActivity.this.listAdapter) {
                    SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.listAdapter);
                    SetAdminsActivity.this.fragmentView.setBackgroundColor(-986896);
                }
            }
            if (SetAdminsActivity.this.searchAdapter != null) {
                SetAdminsActivity.this.searchAdapter.search(null);
            }
        }

        public void onTextChanged(EditText editText) {
            String text = editText.getText().toString();
            if (text.length() != 0) {
                SetAdminsActivity.this.searchWas = true;
                if (!(SetAdminsActivity.this.searchAdapter == null || SetAdminsActivity.this.listView.getAdapter() == SetAdminsActivity.this.searchAdapter)) {
                    SetAdminsActivity.this.listView.setAdapter(SetAdminsActivity.this.searchAdapter);
                    SetAdminsActivity.this.fragmentView.setBackgroundColor(-1);
                }
                if (!(SetAdminsActivity.this.searchEmptyView == null || SetAdminsActivity.this.listView.getEmptyView() == SetAdminsActivity.this.searchEmptyView)) {
                    SetAdminsActivity.this.searchEmptyView.showTextView();
                    SetAdminsActivity.this.listView.setEmptyView(SetAdminsActivity.this.searchEmptyView);
                }
            }
            if (SetAdminsActivity.this.searchAdapter != null) {
                SetAdminsActivity.this.searchAdapter.search(text);
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
            if (i == SetAdminsActivity.this.allAdminsRow) {
                return true;
            }
            if (i < SetAdminsActivity.this.usersStartRow || i >= SetAdminsActivity.this.usersEndRow || (((ChatParticipant) SetAdminsActivity.this.participants.get(i - SetAdminsActivity.this.usersStartRow)) instanceof TL_chatParticipantCreator)) {
                return false;
            }
            return true;
        }

        public int getCount() {
            return SetAdminsActivity.this.rowCount;
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
            boolean z2 = false;
            int type = getItemViewType(i);
            if (type == 0) {
                if (view == null) {
                    view = new TextCheckCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextCheckCell checkCell = (TextCheckCell) view;
                SetAdminsActivity.this.chat = MessagesController.getInstance().getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                String string = LocaleController.getString("SetAdminsAll", C0691R.string.SetAdminsAll);
                if (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled) {
                    z = false;
                }
                checkCell.setTextAndCheck(string, z, false);
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                }
                if (i == SetAdminsActivity.this.allAdminsInfoRow) {
                    if (SetAdminsActivity.this.chat.admins_enabled) {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SetAdminsNotAllInfo", C0691R.string.SetAdminsNotAllInfo));
                    } else {
                        ((TextInfoPrivacyCell) view).setText(LocaleController.getString("SetAdminsAllInfo", C0691R.string.SetAdminsAllInfo));
                    }
                    if (SetAdminsActivity.this.usersStartRow != -1) {
                        view.setBackgroundResource(C0691R.drawable.greydivider);
                    } else {
                        view.setBackgroundResource(C0691R.drawable.greydivider_bottom);
                    }
                } else if (i == SetAdminsActivity.this.usersEndRow) {
                    ((TextInfoPrivacyCell) view).setText(TtmlNode.ANONYMOUS_REGION_ID);
                    view.setBackgroundResource(C0691R.drawable.greydivider_bottom);
                }
            } else if (type == 2) {
                boolean z3;
                if (view == null) {
                    view = new UserCell(this.mContext, 1, 2, false);
                    view.setBackgroundColor(-1);
                }
                UserCell userCell = (UserCell) view;
                ChatParticipant part = (ChatParticipant) SetAdminsActivity.this.participants.get(i - SetAdminsActivity.this.usersStartRow);
                userCell.setData(MessagesController.getInstance().getUser(Integer.valueOf(part.user_id)), null, null, 0);
                SetAdminsActivity.this.chat = MessagesController.getInstance().getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
                if ((part instanceof TL_chatParticipant) && (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled)) {
                    z3 = false;
                } else {
                    z3 = true;
                }
                userCell.setChecked(z3, false);
                if (SetAdminsActivity.this.chat == null || !SetAdminsActivity.this.chat.admins_enabled || part.user_id == UserConfig.getClientUserId()) {
                    z2 = true;
                }
                userCell.setCheckDisabled(z2);
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == SetAdminsActivity.this.allAdminsRow) {
                return 0;
            }
            if (i == SetAdminsActivity.this.allAdminsInfoRow || i == SetAdminsActivity.this.usersEndRow) {
                return 1;
            }
            return 2;
        }

        public int getViewTypeCount() {
            return 3;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public class SearchAdapter extends BaseFragmentAdapter {
        private Context mContext;
        private ArrayList<ChatParticipant> searchResult;
        private ArrayList<CharSequence> searchResultNames;
        private Timer searchTimer;

        /* renamed from: org.telegram.ui.SetAdminsActivity.SearchAdapter.1 */
        class C14281 extends TimerTask {
            final /* synthetic */ String val$query;

            C14281(String str) {
                this.val$query = str;
            }

            public void run() {
                try {
                    SearchAdapter.this.searchTimer.cancel();
                    SearchAdapter.this.searchTimer = null;
                } catch (Throwable e) {
                    FileLog.m13e("tmessages", e);
                }
                SearchAdapter.this.processSearch(this.val$query);
            }
        }

        /* renamed from: org.telegram.ui.SetAdminsActivity.SearchAdapter.2 */
        class C14302 implements Runnable {
            final /* synthetic */ String val$query;

            /* renamed from: org.telegram.ui.SetAdminsActivity.SearchAdapter.2.1 */
            class C14291 implements Runnable {
                final /* synthetic */ ArrayList val$contactsCopy;

                C14291(ArrayList arrayList) {
                    this.val$contactsCopy = arrayList;
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                    r21 = this;
                    r0 = r21;
                    r0 = org.telegram.ui.SetAdminsActivity.SearchAdapter.C14302.this;
                    r17 = r0;
                    r0 = r17;
                    r0 = r0.val$query;
                    r17 = r0;
                    r17 = r17.trim();
                    r13 = r17.toLowerCase();
                    r17 = r13.length();
                    if (r17 != 0) goto L_0x0034;
                L_0x001a:
                    r0 = r21;
                    r0 = org.telegram.ui.SetAdminsActivity.SearchAdapter.C14302.this;
                    r17 = r0;
                    r0 = r17;
                    r0 = org.telegram.ui.SetAdminsActivity.SearchAdapter.this;
                    r17 = r0;
                    r18 = new java.util.ArrayList;
                    r18.<init>();
                    r19 = new java.util.ArrayList;
                    r19.<init>();
                    r17.updateSearchResults(r18, r19);
                L_0x0033:
                    return;
                L_0x0034:
                    r17 = org.telegram.messenger.LocaleController.getInstance();
                    r0 = r17;
                    r14 = r0.getTranslitString(r13);
                    r17 = r13.equals(r14);
                    if (r17 != 0) goto L_0x004a;
                L_0x0044:
                    r17 = r14.length();
                    if (r17 != 0) goto L_0x004b;
                L_0x004a:
                    r14 = 0;
                L_0x004b:
                    if (r14 == 0) goto L_0x00a9;
                L_0x004d:
                    r17 = 1;
                L_0x004f:
                    r17 = r17 + 1;
                    r0 = r17;
                    r12 = new java.lang.String[r0];
                    r17 = 0;
                    r12[r17] = r13;
                    if (r14 == 0) goto L_0x005f;
                L_0x005b:
                    r17 = 1;
                    r12[r17] = r14;
                L_0x005f:
                    r10 = new java.util.ArrayList;
                    r10.<init>();
                    r11 = new java.util.ArrayList;
                    r11.<init>();
                    r2 = 0;
                L_0x006a:
                    r0 = r21;
                    r0 = r0.val$contactsCopy;
                    r17 = r0;
                    r17 = r17.size();
                    r0 = r17;
                    if (r2 >= r0) goto L_0x019e;
                L_0x0078:
                    r0 = r21;
                    r0 = r0.val$contactsCopy;
                    r17 = r0;
                    r0 = r17;
                    r8 = r0.get(r2);
                    r8 = (org.telegram.tgnet.TLRPC.ChatParticipant) r8;
                    r17 = org.telegram.messenger.MessagesController.getInstance();
                    r0 = r8.user_id;
                    r18 = r0;
                    r18 = java.lang.Integer.valueOf(r18);
                    r16 = r17.getUser(r18);
                    r0 = r16;
                    r0 = r0.id;
                    r17 = r0;
                    r18 = org.telegram.messenger.UserConfig.getClientUserId();
                    r0 = r17;
                    r1 = r18;
                    if (r0 != r1) goto L_0x00ac;
                L_0x00a6:
                    r2 = r2 + 1;
                    goto L_0x006a;
                L_0x00a9:
                    r17 = 0;
                    goto L_0x004f;
                L_0x00ac:
                    r0 = r16;
                    r0 = r0.first_name;
                    r17 = r0;
                    r0 = r16;
                    r0 = r0.last_name;
                    r18 = r0;
                    r17 = org.telegram.messenger.ContactsController.formatName(r17, r18);
                    r7 = r17.toLowerCase();
                    r17 = org.telegram.messenger.LocaleController.getInstance();
                    r0 = r17;
                    r15 = r0.getTranslitString(r7);
                    r17 = r7.equals(r15);
                    if (r17 == 0) goto L_0x00d1;
                L_0x00d0:
                    r15 = 0;
                L_0x00d1:
                    r4 = 0;
                    r3 = r12;
                    r6 = r3.length;
                    r5 = 0;
                L_0x00d5:
                    if (r5 >= r6) goto L_0x00a6;
                L_0x00d7:
                    r9 = r3[r5];
                    r17 = r7.startsWith(r9);
                    if (r17 != 0) goto L_0x0121;
                L_0x00df:
                    r17 = new java.lang.StringBuilder;
                    r17.<init>();
                    r18 = " ";
                    r17 = r17.append(r18);
                    r0 = r17;
                    r17 = r0.append(r9);
                    r17 = r17.toString();
                    r0 = r17;
                    r17 = r7.contains(r0);
                    if (r17 != 0) goto L_0x0121;
                L_0x00fc:
                    if (r15 == 0) goto L_0x0148;
                L_0x00fe:
                    r17 = r15.startsWith(r9);
                    if (r17 != 0) goto L_0x0121;
                L_0x0104:
                    r17 = new java.lang.StringBuilder;
                    r17.<init>();
                    r18 = " ";
                    r17 = r17.append(r18);
                    r0 = r17;
                    r17 = r0.append(r9);
                    r17 = r17.toString();
                    r0 = r17;
                    r17 = r15.contains(r0);
                    if (r17 == 0) goto L_0x0148;
                L_0x0121:
                    r4 = 1;
                L_0x0122:
                    if (r4 == 0) goto L_0x019a;
                L_0x0124:
                    r17 = 1;
                    r0 = r17;
                    if (r4 != r0) goto L_0x0160;
                L_0x012a:
                    r0 = r16;
                    r0 = r0.first_name;
                    r17 = r0;
                    r0 = r16;
                    r0 = r0.last_name;
                    r18 = r0;
                    r0 = r17;
                    r1 = r18;
                    r17 = org.telegram.messenger.AndroidUtilities.generateSearchName(r0, r1, r9);
                    r0 = r17;
                    r11.add(r0);
                L_0x0143:
                    r10.add(r8);
                    goto L_0x00a6;
                L_0x0148:
                    r0 = r16;
                    r0 = r0.username;
                    r17 = r0;
                    if (r17 == 0) goto L_0x0122;
                L_0x0150:
                    r0 = r16;
                    r0 = r0.username;
                    r17 = r0;
                    r0 = r17;
                    r17 = r0.startsWith(r9);
                    if (r17 == 0) goto L_0x0122;
                L_0x015e:
                    r4 = 2;
                    goto L_0x0122;
                L_0x0160:
                    r17 = new java.lang.StringBuilder;
                    r17.<init>();
                    r18 = "@";
                    r17 = r17.append(r18);
                    r0 = r16;
                    r0 = r0.username;
                    r18 = r0;
                    r17 = r17.append(r18);
                    r17 = r17.toString();
                    r18 = 0;
                    r19 = new java.lang.StringBuilder;
                    r19.<init>();
                    r20 = "@";
                    r19 = r19.append(r20);
                    r0 = r19;
                    r19 = r0.append(r9);
                    r19 = r19.toString();
                    r17 = org.telegram.messenger.AndroidUtilities.generateSearchName(r17, r18, r19);
                    r0 = r17;
                    r11.add(r0);
                    goto L_0x0143;
                L_0x019a:
                    r5 = r5 + 1;
                    goto L_0x00d5;
                L_0x019e:
                    r0 = r21;
                    r0 = org.telegram.ui.SetAdminsActivity.SearchAdapter.C14302.this;
                    r17 = r0;
                    r0 = r17;
                    r0 = org.telegram.ui.SetAdminsActivity.SearchAdapter.this;
                    r17 = r0;
                    r0 = r17;
                    r0.updateSearchResults(r10, r11);
                    goto L_0x0033;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: org.telegram.ui.SetAdminsActivity.SearchAdapter.2.1.run():void");
                }
            }

            C14302(String str) {
                this.val$query = str;
            }

            public void run() {
                ArrayList<ChatParticipant> contactsCopy = new ArrayList();
                contactsCopy.addAll(SetAdminsActivity.this.participants);
                Utilities.searchQueue.postRunnable(new C14291(contactsCopy));
            }
        }

        /* renamed from: org.telegram.ui.SetAdminsActivity.SearchAdapter.3 */
        class C14313 implements Runnable {
            final /* synthetic */ ArrayList val$names;
            final /* synthetic */ ArrayList val$users;

            C14313(ArrayList arrayList, ArrayList arrayList2) {
                this.val$users = arrayList;
                this.val$names = arrayList2;
            }

            public void run() {
                SearchAdapter.this.searchResult = this.val$users;
                SearchAdapter.this.searchResultNames = this.val$names;
                SearchAdapter.this.notifyDataSetChanged();
            }
        }

        public SearchAdapter(Context context) {
            this.searchResult = new ArrayList();
            this.searchResultNames = new ArrayList();
            this.mContext = context;
        }

        public void search(String query) {
            try {
                if (this.searchTimer != null) {
                    this.searchTimer.cancel();
                }
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
            if (query == null) {
                this.searchResult.clear();
                this.searchResultNames.clear();
                notifyDataSetChanged();
                return;
            }
            this.searchTimer = new Timer();
            this.searchTimer.schedule(new C14281(query), 200, 300);
        }

        private void processSearch(String query) {
            AndroidUtilities.runOnUIThread(new C14302(query));
        }

        private void updateSearchResults(ArrayList<ChatParticipant> users, ArrayList<CharSequence> names) {
            AndroidUtilities.runOnUIThread(new C14313(users, names));
        }

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int i) {
            return true;
        }

        public int getCount() {
            return this.searchResult.size();
        }

        public ChatParticipant getItem(int i) {
            return (ChatParticipant) this.searchResult.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return true;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            boolean z;
            boolean z2 = false;
            if (view == null) {
                view = new UserCell(this.mContext, 1, 2, false);
            }
            ChatParticipant participant = getItem(i);
            User user = MessagesController.getInstance().getUser(Integer.valueOf(participant.user_id));
            String un = user.username;
            CharSequence username = null;
            CharSequence name = null;
            if (i < this.searchResult.size()) {
                name = (CharSequence) this.searchResultNames.get(i);
                if (name != null && un != null && un.length() > 0 && name.toString().startsWith("@" + un)) {
                    username = name;
                    name = null;
                }
            }
            UserCell userCell = (UserCell) view;
            userCell.setData(user, name, username, 0);
            SetAdminsActivity.this.chat = MessagesController.getInstance().getChat(Integer.valueOf(SetAdminsActivity.this.chat_id));
            if ((participant instanceof TL_chatParticipant) && (SetAdminsActivity.this.chat == null || SetAdminsActivity.this.chat.admins_enabled)) {
                z = false;
            } else {
                z = true;
            }
            userCell.setChecked(z, false);
            if (SetAdminsActivity.this.chat == null || !SetAdminsActivity.this.chat.admins_enabled || participant.user_id == UserConfig.getClientUserId()) {
                z2 = true;
            }
            userCell.setCheckDisabled(z2);
            return view;
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return this.searchResult.isEmpty();
        }
    }

    public SetAdminsActivity(Bundle args) {
        super(args);
        this.participants = new ArrayList();
        this.chat_id = args.getInt("chat_id");
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.chatInfoDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
    }

    public View createView(Context context) {
        this.searching = false;
        this.searchWas = false;
        this.actionBar.setBackButtonImage(C0691R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("SetAdminsTitle", C0691R.string.SetAdminsTitle));
        this.actionBar.setActionBarMenuOnItemClick(new C19451());
        this.searchItem = this.actionBar.createMenu().addItem(0, (int) C0691R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new C19462());
        this.searchItem.getSearchField().setHint(LocaleController.getString("Search", C0691R.string.Search));
        this.listAdapter = new ListAdapter(context);
        this.searchAdapter = new SearchAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        this.fragmentView.setBackgroundColor(-986896);
        this.listView = new ListView(context);
        this.listView.setDivider(null);
        this.listView.setDividerHeight(0);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setDrawSelectorOnTop(true);
        frameLayout.addView(this.listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.listView.setAdapter(this.listAdapter);
        this.listView.setOnItemClickListener(new C14263());
        this.searchEmptyView = new EmptyTextProgressView(context);
        this.searchEmptyView.setVisibility(8);
        this.searchEmptyView.setShowAtCenter(true);
        this.searchEmptyView.setText(LocaleController.getString("NoResult", C0691R.string.NoResult));
        frameLayout.addView(this.searchEmptyView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        this.searchEmptyView.showTextView();
        updateRowsIds();
        return this.fragmentView;
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.chatInfoDidLoaded) {
            ChatFull chatFull = args[0];
            if (chatFull.id == this.chat_id) {
                this.info = chatFull;
                updateChatParticipants();
                updateRowsIds();
            }
        }
        if (id == NotificationCenter.updateInterfaces) {
            int mask = ((Integer) args[0]).intValue();
            if (((mask & 2) != 0 || (mask & 1) != 0 || (mask & 4) != 0) && this.listView != null) {
                int count = this.listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = this.listView.getChildAt(a);
                    if (child instanceof UserCell) {
                        ((UserCell) child).update(mask);
                    }
                }
            }
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void setChatInfo(ChatFull chatParticipants) {
        this.info = chatParticipants;
        updateChatParticipants();
    }

    private int getChatAdminParticipantType(ChatParticipant participant) {
        if (participant instanceof TL_chatParticipantCreator) {
            return 0;
        }
        if (participant instanceof TL_chatParticipantAdmin) {
            return 1;
        }
        return 2;
    }

    private void updateChatParticipants() {
        if (this.info != null && this.participants.size() != this.info.participants.participants.size()) {
            this.participants.clear();
            this.participants.addAll(this.info.participants.participants);
            try {
                Collections.sort(this.participants, new C14274());
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
        }
    }

    private void updateRowsIds() {
        this.rowCount = 0;
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.allAdminsRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.allAdminsInfoRow = i;
        if (this.info != null) {
            this.usersStartRow = this.rowCount;
            this.rowCount += this.participants.size();
            i = this.rowCount;
            this.rowCount = i + 1;
            this.usersEndRow = i;
            if (!(this.searchItem == null || this.searchWas)) {
                this.searchItem.setVisibility(0);
            }
        } else {
            this.usersStartRow = -1;
            this.usersEndRow = -1;
            if (this.searchItem != null) {
                this.searchItem.setVisibility(8);
            }
        }
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }
}
