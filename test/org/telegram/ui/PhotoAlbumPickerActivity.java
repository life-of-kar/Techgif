package org.telegram.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils.TruncateAt;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.C0691R;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MediaController.AlbumEntry;
import org.telegram.messenger.MediaController.PhotoEntry;
import org.telegram.messenger.MediaController.SearchImage;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationCenter.NotificationCenterDelegate;
import org.telegram.ui.ActionBar.ActionBar.ActionBarMenuOnItemClick;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.PhotoPickerAlbumsCell;
import org.telegram.ui.Cells.PhotoPickerAlbumsCell.PhotoPickerAlbumsCellDelegate;
import org.telegram.ui.Cells.PhotoPickerSearchCell;
import org.telegram.ui.Cells.PhotoPickerSearchCell.PhotoPickerSearchCellDelegate;
import org.telegram.ui.Components.PickerBottomLayout;
import org.telegram.ui.PhotoPickerActivity.PhotoPickerActivityDelegate;

public class PhotoAlbumPickerActivity extends BaseFragment implements NotificationCenterDelegate {
    private static final int item_photos = 2;
    private static final int item_video = 3;
    private ArrayList<AlbumEntry> albumsSorted;
    private boolean allowGifs;
    private ChatActivity chatActivity;
    private int columnsCount;
    private PhotoAlbumPickerActivityDelegate delegate;
    private TextView dropDown;
    private ActionBarMenuItem dropDownContainer;
    private TextView emptyView;
    private ListAdapter listAdapter;
    private ListView listView;
    private boolean loading;
    private PickerBottomLayout pickerBottomLayout;
    private FrameLayout progressView;
    private ArrayList<SearchImage> recentGifImages;
    private HashMap<String, SearchImage> recentImagesGifKeys;
    private HashMap<String, SearchImage> recentImagesWebKeys;
    private ArrayList<SearchImage> recentWebImages;
    private int selectedMode;
    private HashMap<Integer, PhotoEntry> selectedPhotos;
    private HashMap<String, SearchImage> selectedWebPhotos;
    private boolean sendPressed;
    private boolean singlePhoto;
    private ArrayList<AlbumEntry> videoAlbumsSorted;

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.2 */
    class C13512 implements OnClickListener {
        C13512() {
        }

        public void onClick(View view) {
            PhotoAlbumPickerActivity.this.dropDownContainer.toggleSubMenu();
        }
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.3 */
    class C13523 implements OnTouchListener {
        C13523() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            return true;
        }
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.4 */
    class C13534 implements OnClickListener {
        C13534() {
        }

        public void onClick(View view) {
            PhotoAlbumPickerActivity.this.finishFragment();
        }
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.5 */
    class C13545 implements OnClickListener {
        C13545() {
        }

        public void onClick(View view) {
            PhotoAlbumPickerActivity.this.sendSelectedPhotos();
            PhotoAlbumPickerActivity.this.finishFragment();
        }
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.6 */
    class C13556 implements OnPreDrawListener {
        C13556() {
        }

        public boolean onPreDraw() {
            PhotoAlbumPickerActivity.this.fixLayoutInternal();
            if (PhotoAlbumPickerActivity.this.listView != null) {
                PhotoAlbumPickerActivity.this.listView.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            return true;
        }
    }

    public interface PhotoAlbumPickerActivityDelegate {
        void didSelectPhotos(ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<SearchImage> arrayList3);

        boolean didSelectVideo(String str);

        void startPhotoSelectActivity();
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.1 */
    class C19001 extends ActionBarMenuOnItemClick {
        C19001() {
        }

        public void onItemClick(int id) {
            if (id == -1) {
                PhotoAlbumPickerActivity.this.finishFragment();
            } else if (id == 1) {
                if (PhotoAlbumPickerActivity.this.delegate != null) {
                    PhotoAlbumPickerActivity.this.finishFragment(false);
                    PhotoAlbumPickerActivity.this.delegate.startPhotoSelectActivity();
                }
            } else if (id == PhotoAlbumPickerActivity.item_photos) {
                if (PhotoAlbumPickerActivity.this.selectedMode != 0) {
                    PhotoAlbumPickerActivity.this.selectedMode = 0;
                    PhotoAlbumPickerActivity.this.dropDown.setText(LocaleController.getString("PickerPhotos", C0691R.string.PickerPhotos));
                    PhotoAlbumPickerActivity.this.emptyView.setText(LocaleController.getString("NoPhotos", C0691R.string.NoPhotos));
                    PhotoAlbumPickerActivity.this.listAdapter.notifyDataSetChanged();
                }
            } else if (id == PhotoAlbumPickerActivity.item_video && PhotoAlbumPickerActivity.this.selectedMode != 1) {
                PhotoAlbumPickerActivity.this.selectedMode = 1;
                PhotoAlbumPickerActivity.this.dropDown.setText(LocaleController.getString("PickerVideo", C0691R.string.PickerVideo));
                PhotoAlbumPickerActivity.this.emptyView.setText(LocaleController.getString("NoVideo", C0691R.string.NoVideo));
                PhotoAlbumPickerActivity.this.listAdapter.notifyDataSetChanged();
            }
        }
    }

    /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.7 */
    class C19017 implements PhotoPickerActivityDelegate {
        C19017() {
        }

        public void selectedPhotosChanged() {
            if (PhotoAlbumPickerActivity.this.pickerBottomLayout != null) {
                PhotoAlbumPickerActivity.this.pickerBottomLayout.updateSelectedCount(PhotoAlbumPickerActivity.this.selectedPhotos.size() + PhotoAlbumPickerActivity.this.selectedWebPhotos.size(), true);
            }
        }

        public void actionButtonPressed(boolean canceled) {
            PhotoAlbumPickerActivity.this.removeSelfFromStack();
            if (!canceled) {
                PhotoAlbumPickerActivity.this.sendSelectedPhotos();
            }
        }

        public boolean didSelectVideo(String path) {
            PhotoAlbumPickerActivity.this.removeSelfFromStack();
            return PhotoAlbumPickerActivity.this.delegate.didSelectVideo(path);
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.ListAdapter.1 */
        class C19021 implements PhotoPickerAlbumsCellDelegate {
            C19021() {
            }

            public void didSelectAlbum(AlbumEntry albumEntry) {
                PhotoAlbumPickerActivity.this.openPhotoPicker(albumEntry, 0);
            }
        }

        /* renamed from: org.telegram.ui.PhotoAlbumPickerActivity.ListAdapter.2 */
        class C19032 implements PhotoPickerSearchCellDelegate {
            C19032() {
            }

            public void didPressedSearchButton(int index) {
                PhotoAlbumPickerActivity.this.openPhotoPicker(null, index);
            }
        }

        public ListAdapter(Context context) {
            this.mContext = context;
        }

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int i) {
            return true;
        }

        public int getCount() {
            int i = 0;
            if (PhotoAlbumPickerActivity.this.singlePhoto || PhotoAlbumPickerActivity.this.selectedMode == 0) {
                if (!PhotoAlbumPickerActivity.this.singlePhoto) {
                    if (PhotoAlbumPickerActivity.this.albumsSorted != null) {
                        i = (int) Math.ceil((double) (((float) PhotoAlbumPickerActivity.this.albumsSorted.size()) / ((float) PhotoAlbumPickerActivity.this.columnsCount)));
                    }
                    return i + 1;
                } else if (PhotoAlbumPickerActivity.this.albumsSorted != null) {
                    return (int) Math.ceil((double) (((float) PhotoAlbumPickerActivity.this.albumsSorted.size()) / ((float) PhotoAlbumPickerActivity.this.columnsCount)));
                } else {
                    return 0;
                }
            } else if (PhotoAlbumPickerActivity.this.videoAlbumsSorted != null) {
                return (int) Math.ceil((double) (((float) PhotoAlbumPickerActivity.this.videoAlbumsSorted.size()) / ((float) PhotoAlbumPickerActivity.this.columnsCount)));
            } else {
                return 0;
            }
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return true;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);
            if (type == 0) {
                PhotoPickerAlbumsCell photoPickerAlbumsCell;
                if (view == null) {
                    view = new PhotoPickerAlbumsCell(this.mContext);
                    photoPickerAlbumsCell = (PhotoPickerAlbumsCell) view;
                    photoPickerAlbumsCell.setDelegate(new C19021());
                } else {
                    photoPickerAlbumsCell = (PhotoPickerAlbumsCell) view;
                }
                photoPickerAlbumsCell.setAlbumsCount(PhotoAlbumPickerActivity.this.columnsCount);
                for (int a = 0; a < PhotoAlbumPickerActivity.this.columnsCount; a++) {
                    int index;
                    if (PhotoAlbumPickerActivity.this.singlePhoto || PhotoAlbumPickerActivity.this.selectedMode == 1) {
                        index = (PhotoAlbumPickerActivity.this.columnsCount * i) + a;
                    } else {
                        index = ((i - 1) * PhotoAlbumPickerActivity.this.columnsCount) + a;
                    }
                    if (PhotoAlbumPickerActivity.this.singlePhoto || PhotoAlbumPickerActivity.this.selectedMode == 0) {
                        if (index < PhotoAlbumPickerActivity.this.albumsSorted.size()) {
                            photoPickerAlbumsCell.setAlbum(a, (AlbumEntry) PhotoAlbumPickerActivity.this.albumsSorted.get(index));
                        } else {
                            photoPickerAlbumsCell.setAlbum(a, null);
                        }
                    } else if (index < PhotoAlbumPickerActivity.this.videoAlbumsSorted.size()) {
                        photoPickerAlbumsCell.setAlbum(a, (AlbumEntry) PhotoAlbumPickerActivity.this.videoAlbumsSorted.get(index));
                    } else {
                        photoPickerAlbumsCell.setAlbum(a, null);
                    }
                }
                photoPickerAlbumsCell.requestLayout();
                return view;
            } else if (type != 1 || view != null) {
                return view;
            } else {
                view = new PhotoPickerSearchCell(this.mContext, PhotoAlbumPickerActivity.this.allowGifs);
                ((PhotoPickerSearchCell) view).setDelegate(new C19032());
                return view;
            }
        }

        public int getItemViewType(int i) {
            if (PhotoAlbumPickerActivity.this.singlePhoto || PhotoAlbumPickerActivity.this.selectedMode == 1) {
                return 0;
            }
            if (i != 0) {
                return 0;
            }
            return 1;
        }

        public int getViewTypeCount() {
            if (PhotoAlbumPickerActivity.this.singlePhoto || PhotoAlbumPickerActivity.this.selectedMode == 1) {
                return 1;
            }
            return PhotoAlbumPickerActivity.item_photos;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    public PhotoAlbumPickerActivity(boolean singlePhoto, boolean allowGifs, ChatActivity chatActivity) {
        this.albumsSorted = null;
        this.videoAlbumsSorted = null;
        this.selectedPhotos = new HashMap();
        this.selectedWebPhotos = new HashMap();
        this.recentImagesWebKeys = new HashMap();
        this.recentImagesGifKeys = new HashMap();
        this.recentWebImages = new ArrayList();
        this.recentGifImages = new ArrayList();
        this.loading = false;
        this.columnsCount = item_photos;
        this.chatActivity = chatActivity;
        this.singlePhoto = singlePhoto;
        this.allowGifs = allowGifs;
    }

    public boolean onFragmentCreate() {
        this.loading = true;
        MediaController.loadGalleryPhotosAlbums(this.classGuid);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.albumsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.recentImagesDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        return super.onFragmentCreate();
    }

    public void onFragmentDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.albumsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.recentImagesDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        super.onFragmentDestroy();
    }

    public View createView(Context context) {
        LayoutParams layoutParams;
        this.actionBar.setBackgroundColor(Theme.ACTION_BAR_MEDIA_PICKER_COLOR);
        this.actionBar.setItemsBackgroundColor(Theme.ACTION_BAR_PICKER_SELECTOR_COLOR);
        this.actionBar.setBackButtonImage(C0691R.drawable.ic_ab_back);
        this.actionBar.setActionBarMenuOnItemClick(new C19001());
        ActionBarMenu menu = this.actionBar.createMenu();
        menu.addItem(1, (int) C0691R.drawable.ic_ab_other);
        this.fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = this.fragmentView;
        frameLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        if (this.singlePhoto) {
            this.actionBar.setTitle(LocaleController.getString("Gallery", C0691R.string.Gallery));
        } else {
            int dp;
            this.selectedMode = 0;
            this.dropDownContainer = new ActionBarMenuItem(context, menu, 0);
            this.dropDownContainer.setSubMenuOpenSide(1);
            this.dropDownContainer.addSubItem(item_photos, LocaleController.getString("PickerPhotos", C0691R.string.PickerPhotos), 0);
            this.dropDownContainer.addSubItem(item_video, LocaleController.getString("PickerVideo", C0691R.string.PickerVideo), 0);
            this.actionBar.addView(this.dropDownContainer);
            layoutParams = (LayoutParams) this.dropDownContainer.getLayoutParams();
            layoutParams.height = -1;
            layoutParams.width = -2;
            layoutParams.rightMargin = AndroidUtilities.dp(40.0f);
            if (AndroidUtilities.isTablet()) {
                dp = AndroidUtilities.dp(64.0f);
            } else {
                dp = AndroidUtilities.dp(56.0f);
            }
            layoutParams.leftMargin = dp;
            layoutParams.gravity = 51;
            this.dropDownContainer.setLayoutParams(layoutParams);
            this.dropDownContainer.setOnClickListener(new C13512());
            this.dropDown = new TextView(context);
            this.dropDown.setGravity(item_video);
            this.dropDown.setSingleLine(true);
            this.dropDown.setLines(1);
            this.dropDown.setMaxLines(1);
            this.dropDown.setEllipsize(TruncateAt.END);
            this.dropDown.setTextColor(-1);
            this.dropDown.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            this.dropDown.setCompoundDrawablesWithIntrinsicBounds(0, 0, C0691R.drawable.ic_arrow_drop_down, 0);
            this.dropDown.setCompoundDrawablePadding(AndroidUtilities.dp(4.0f));
            this.dropDown.setPadding(0, 0, AndroidUtilities.dp(10.0f), 0);
            this.dropDown.setText(LocaleController.getString("PickerPhotos", C0691R.string.PickerPhotos));
            this.dropDownContainer.addView(this.dropDown);
            layoutParams = (LayoutParams) this.dropDown.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -2;
            layoutParams.leftMargin = AndroidUtilities.dp(16.0f);
            layoutParams.gravity = 16;
            this.dropDown.setLayoutParams(layoutParams);
        }
        this.listView = new ListView(context);
        this.listView.setPadding(AndroidUtilities.dp(4.0f), 0, AndroidUtilities.dp(4.0f), AndroidUtilities.dp(4.0f));
        this.listView.setClipToPadding(false);
        this.listView.setHorizontalScrollBarEnabled(false);
        this.listView.setVerticalScrollBarEnabled(false);
        this.listView.setSelector(new ColorDrawable(0));
        this.listView.setDividerHeight(0);
        this.listView.setDivider(null);
        this.listView.setDrawingCacheEnabled(false);
        this.listView.setScrollingCacheEnabled(false);
        frameLayout.addView(this.listView);
        layoutParams = (LayoutParams) this.listView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.bottomMargin = AndroidUtilities.dp(48.0f);
        this.listView.setLayoutParams(layoutParams);
        ListView listView = this.listView;
        android.widget.ListAdapter listAdapter = new ListAdapter(context);
        this.listAdapter = listAdapter;
        listView.setAdapter(listAdapter);
        AndroidUtilities.setListViewEdgeEffectColor(this.listView, -13421773);
        this.emptyView = new TextView(context);
        this.emptyView.setTextColor(-8355712);
        this.emptyView.setTextSize(20.0f);
        this.emptyView.setGravity(17);
        this.emptyView.setVisibility(8);
        this.emptyView.setText(LocaleController.getString("NoPhotos", C0691R.string.NoPhotos));
        frameLayout.addView(this.emptyView);
        layoutParams = (LayoutParams) this.emptyView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.bottomMargin = AndroidUtilities.dp(48.0f);
        this.emptyView.setLayoutParams(layoutParams);
        this.emptyView.setOnTouchListener(new C13523());
        this.progressView = new FrameLayout(context);
        this.progressView.setVisibility(8);
        frameLayout.addView(this.progressView);
        layoutParams = (LayoutParams) this.progressView.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = -1;
        layoutParams.bottomMargin = AndroidUtilities.dp(48.0f);
        this.progressView.setLayoutParams(layoutParams);
        this.progressView.addView(new ProgressBar(context));
        layoutParams = (LayoutParams) this.progressView.getLayoutParams();
        layoutParams.width = -2;
        layoutParams.height = -2;
        layoutParams.gravity = 17;
        this.progressView.setLayoutParams(layoutParams);
        this.pickerBottomLayout = new PickerBottomLayout(context);
        frameLayout.addView(this.pickerBottomLayout);
        layoutParams = (LayoutParams) this.pickerBottomLayout.getLayoutParams();
        layoutParams.width = -1;
        layoutParams.height = AndroidUtilities.dp(48.0f);
        layoutParams.gravity = 80;
        this.pickerBottomLayout.setLayoutParams(layoutParams);
        this.pickerBottomLayout.cancelButton.setOnClickListener(new C13534());
        this.pickerBottomLayout.doneButton.setOnClickListener(new C13545());
        if (!this.loading || (this.albumsSorted != null && (this.albumsSorted == null || !this.albumsSorted.isEmpty()))) {
            this.progressView.setVisibility(8);
            this.listView.setEmptyView(this.emptyView);
        } else {
            this.progressView.setVisibility(0);
            this.listView.setEmptyView(null);
        }
        this.pickerBottomLayout.updateSelectedCount(this.selectedPhotos.size() + this.selectedWebPhotos.size(), true);
        return this.fragmentView;
    }

    public void onPause() {
        super.onPause();
        if (this.dropDownContainer != null) {
            this.dropDownContainer.closeSubMenu();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
        }
        fixLayout();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.albumsDidLoaded) {
            if (this.classGuid == ((Integer) args[0]).intValue()) {
                this.albumsSorted = (ArrayList) args[1];
                this.videoAlbumsSorted = (ArrayList) args[item_video];
                if (this.progressView != null) {
                    this.progressView.setVisibility(8);
                }
                if (this.listView != null && this.listView.getEmptyView() == null) {
                    this.listView.setEmptyView(this.emptyView);
                }
                if (this.listAdapter != null) {
                    this.listAdapter.notifyDataSetChanged();
                }
                this.loading = false;
            }
        } else if (id == NotificationCenter.closeChats) {
            removeSelfFromStack();
        } else if (id == NotificationCenter.recentImagesDidLoaded) {
            int type = ((Integer) args[0]).intValue();
            Iterator i$;
            SearchImage searchImage;
            if (type == 0) {
                this.recentWebImages = (ArrayList) args[1];
                this.recentImagesWebKeys.clear();
                i$ = this.recentWebImages.iterator();
                while (i$.hasNext()) {
                    searchImage = (SearchImage) i$.next();
                    this.recentImagesWebKeys.put(searchImage.id, searchImage);
                }
            } else if (type == 1) {
                this.recentGifImages = (ArrayList) args[1];
                this.recentImagesGifKeys.clear();
                i$ = this.recentGifImages.iterator();
                while (i$.hasNext()) {
                    searchImage = (SearchImage) i$.next();
                    this.recentImagesGifKeys.put(searchImage.id, searchImage);
                }
            }
        }
    }

    public void setDelegate(PhotoAlbumPickerActivityDelegate delegate) {
        this.delegate = delegate;
    }

    private void sendSelectedPhotos() {
        if ((!this.selectedPhotos.isEmpty() || !this.selectedWebPhotos.isEmpty()) && this.delegate != null && !this.sendPressed) {
            this.sendPressed = true;
            ArrayList<String> photos = new ArrayList();
            ArrayList<String> captions = new ArrayList();
            for (Entry<Integer, PhotoEntry> entry : this.selectedPhotos.entrySet()) {
                PhotoEntry photoEntry = (PhotoEntry) entry.getValue();
                if (photoEntry.imagePath != null) {
                    photos.add(photoEntry.imagePath);
                    captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                } else if (photoEntry.path != null) {
                    photos.add(photoEntry.path);
                    captions.add(photoEntry.caption != null ? photoEntry.caption.toString() : null);
                }
            }
            ArrayList<SearchImage> webPhotos = new ArrayList();
            boolean gifChanged = false;
            boolean webChange = false;
            for (Entry<String, SearchImage> entry2 : this.selectedWebPhotos.entrySet()) {
                SearchImage searchImage = (SearchImage) entry2.getValue();
                if (searchImage.imagePath != null) {
                    photos.add(searchImage.imagePath);
                    captions.add(searchImage.caption != null ? searchImage.caption.toString() : null);
                } else {
                    webPhotos.add(searchImage);
                }
                searchImage.date = (int) (System.currentTimeMillis() / 1000);
                SearchImage recentImage;
                if (searchImage.type == 0) {
                    webChange = true;
                    recentImage = (SearchImage) this.recentImagesWebKeys.get(searchImage.id);
                    if (recentImage != null) {
                        this.recentWebImages.remove(recentImage);
                        this.recentWebImages.add(0, recentImage);
                    } else {
                        this.recentWebImages.add(0, searchImage);
                    }
                } else if (searchImage.type == 1) {
                    gifChanged = true;
                    recentImage = (SearchImage) this.recentImagesGifKeys.get(searchImage.id);
                    if (recentImage != null) {
                        this.recentGifImages.remove(recentImage);
                        this.recentGifImages.add(0, recentImage);
                    } else {
                        this.recentGifImages.add(0, searchImage);
                    }
                }
            }
            if (webChange) {
                MessagesStorage.getInstance().putWebRecent(this.recentWebImages);
            }
            if (gifChanged) {
                MessagesStorage.getInstance().putWebRecent(this.recentGifImages);
            }
            this.delegate.didSelectPhotos(photos, captions, webPhotos);
        }
    }

    private void fixLayout() {
        if (this.listView != null) {
            this.listView.getViewTreeObserver().addOnPreDrawListener(new C13556());
        }
    }

    private void fixLayoutInternal() {
        if (getParentActivity() != null) {
            int rotation = ((WindowManager) ApplicationLoader.applicationContext.getSystemService("window")).getDefaultDisplay().getRotation();
            this.columnsCount = item_photos;
            if (!AndroidUtilities.isTablet() && (rotation == item_video || rotation == 1)) {
                this.columnsCount = 4;
            }
            this.listAdapter.notifyDataSetChanged();
            if (this.dropDownContainer != null) {
                if (!AndroidUtilities.isTablet()) {
                    LayoutParams layoutParams = (LayoutParams) this.dropDownContainer.getLayoutParams();
                    layoutParams.topMargin = VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0;
                    this.dropDownContainer.setLayoutParams(layoutParams);
                }
                if (AndroidUtilities.isTablet() || ApplicationLoader.applicationContext.getResources().getConfiguration().orientation != item_photos) {
                    this.dropDown.setTextSize(20.0f);
                } else {
                    this.dropDown.setTextSize(18.0f);
                }
            }
        }
    }

    private void openPhotoPicker(AlbumEntry albumEntry, int type) {
        ArrayList<SearchImage> recentImages = null;
        if (albumEntry == null) {
            if (type == 0) {
                recentImages = this.recentWebImages;
            } else if (type == 1) {
                recentImages = this.recentGifImages;
            }
        }
        PhotoPickerActivity fragment = new PhotoPickerActivity(type, albumEntry, this.selectedPhotos, this.selectedWebPhotos, recentImages, this.singlePhoto, this.chatActivity);
        fragment.setDelegate(new C19017());
        presentFragment(fragment);
    }
}
