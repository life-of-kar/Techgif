package org.telegram.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

public class ConvertGroupActivity extends BaseFragment implements NotificationCenterDelegate {
    private int chat_id;
    private int convertDetailRow;
    private int convertInfoRow;
    private int convertRow;
    private ListAdapter listAdapter;
    private int rowCount;

    /* renamed from: org.telegram.ui.ConvertGroupActivity.2 */
    class C12122 implements OnItemClickListener {

        /* renamed from: org.telegram.ui.ConvertGroupActivity.2.1 */
        class C12111 implements OnClickListener {
            C12111() {
            }

            public void onClick(DialogInterface dialogInterface, int i) {
                MessagesController.getInstance().convertToMegaGroup(ConvertGroupActivity.this.getParentActivity(), ConvertGroupActivity.this.chat_id);
            }
        }

        C12122() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == ConvertGroupActivity.this.convertRow) {
                Builder builder = new Builder(ConvertGroupActivity.this.getParentActivity());
                builder.setMessage(LocaleController.getString("ConvertGroupAlert", C0691R.string.ConvertGroupAlert));
                builder.setTitle(LocaleController.getString("ConvertGroupAlertWarning", C0691R.string.ConvertGroupAlertWarning));
                builder.setPositiveButton(LocaleController.getString("OK", C0691R.string.OK), new C12111());
                builder.setNegativeButton(LocaleController.getString("Cancel", C0691R.string.Cancel), null);
                ConvertGroupActivity.this.showDialog(builder.create());
            }
        }
    }

    /* renamed from: org.telegram.ui.ConvertGroupActivity.1 */
    class C18521 extends ActionBarMenuOnItemClick {
        C18521() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                ConvertGroupActivity.this.finishFragment();
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
            return i == ConvertGroupActivity.this.convertRow;
        }

        public int getCount() {
            return ConvertGroupActivity.this.rowCount;
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
                    view = new TextSettingsCell(this.mContext);
                    view.setBackgroundColor(-1);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == ConvertGroupActivity.this.convertRow) {
                    textCell.setText(LocaleController.getString("ConvertGroup", C0691R.string.ConvertGroup), false);
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new TextInfoPrivacyCell(this.mContext);
                }
                if (i == ConvertGroupActivity.this.convertInfoRow) {
                    ((TextInfoPrivacyCell) view).setText(AndroidUtilities.replaceTags(LocaleController.getString("ConvertGroupInfo2", C0691R.string.ConvertGroupInfo2)));
                    view.setBackgroundResource(C0691R.drawable.greydivider);
                } else if (i == ConvertGroupActivity.this.convertDetailRow) {
                    ((TextInfoPrivacyCell) view).setText(AndroidUtilities.replaceTags(LocaleController.getString("ConvertGroupInfo3", C0691R.string.ConvertGroupInfo3)));
                    view.setBackgroundResource(C0691R.drawable.greydivider_bottom);
                }
            }
            return view;
        }

        public int getItemViewType(int i) {
            if (i == ConvertGroupActivity.this.convertRow) {
                return 0;
            }
            if (i == ConvertGroupActivity.this.convertInfoRow || i == ConvertGroupActivity.this.convertDetailRow) {
                return 1;
            }
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    public ConvertGroupActivity(Bundle args) {
        super(args);
        this.chat_id = args.getInt("chat_id");
    }

    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        int i = this.rowCount;
        this.rowCount = i + 1;
        this.convertInfoRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.convertRow = i;
        i = this.rowCount;
        this.rowCount = i + 1;
        this.convertDetailRow = i;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        return true;
    }

    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
    }

    public View createView(Context context) {
        this.actionBar.setBackButtonImage(C0691R.drawable.ic_ab_back);
        this.actionBar.setAllowOverlayTitle(true);
        this.actionBar.setTitle(LocaleController.getString("ConvertGroup", C0691R.string.ConvertGroup));
        this.actionBar.setActionBarMenuOnItemClick(new C18521());
        this.listAdapter = new ListAdapter(context);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(-986896);
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        listView.setDrawSelectorOnTop(true);
        frameLayout.addView(listView, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        listView.setAdapter(this.listAdapter);
        listView.setOnItemClickListener(new C12122());
        return this.fragmentView;
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        }
    }
}
