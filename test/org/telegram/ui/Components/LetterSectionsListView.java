package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Iterator;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.exoplayer.C0747C;
import org.telegram.ui.Adapters.BaseSectionsAdapter;
import org.telegram.ui.StickersActivity.TouchHelperCallback;

public class LetterSectionsListView extends ListView implements OnScrollListener {
    private int currentFirst;
    private int currentVisible;
    private ArrayList<View> headers;
    private ArrayList<View> headersCache;
    private BaseSectionsAdapter mAdapter;
    private OnScrollListener mOnScrollListener;
    private int sectionsCount;
    private int startSection;

    public LetterSectionsListView(Context context) {
        super(context);
        this.headers = new ArrayList();
        this.headersCache = new ArrayList();
        this.currentFirst = -1;
        this.currentVisible = -1;
        super.setOnScrollListener(this);
    }

    public LetterSectionsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.headers = new ArrayList();
        this.headersCache = new ArrayList();
        this.currentFirst = -1;
        this.currentVisible = -1;
        super.setOnScrollListener(this);
    }

    public LetterSectionsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.headers = new ArrayList();
        this.headersCache = new ArrayList();
        this.currentFirst = -1;
        this.currentVisible = -1;
        super.setOnScrollListener(this);
    }

    public void setAdapter(ListAdapter adapter) {
        if (this.mAdapter != adapter) {
            this.headers.clear();
            this.headersCache.clear();
            if (adapter instanceof BaseSectionsAdapter) {
                this.mAdapter = (BaseSectionsAdapter) adapter;
            } else {
                this.mAdapter = null;
            }
            super.setAdapter(adapter);
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        if (this.mAdapter != null) {
            this.headersCache.addAll(this.headers);
            this.headers.clear();
            if (this.mAdapter.getCount() != 0) {
                int itemNum;
                if (!(this.currentFirst == firstVisibleItem && this.currentVisible == visibleItemCount)) {
                    this.currentFirst = firstVisibleItem;
                    this.currentVisible = visibleItemCount;
                    this.sectionsCount = 1;
                    this.startSection = this.mAdapter.getSectionForPosition(firstVisibleItem);
                    itemNum = (this.mAdapter.getCountForSection(this.startSection) + firstVisibleItem) - this.mAdapter.getPositionInSectionForPosition(firstVisibleItem);
                    while (itemNum < firstVisibleItem + visibleItemCount) {
                        itemNum += this.mAdapter.getCountForSection(this.startSection + this.sectionsCount);
                        this.sectionsCount++;
                    }
                }
                itemNum = firstVisibleItem;
                for (int a = this.startSection; a < this.startSection + this.sectionsCount; a++) {
                    View header = null;
                    if (!this.headersCache.isEmpty()) {
                        header = (View) this.headersCache.get(0);
                        this.headersCache.remove(0);
                    }
                    header = getSectionHeaderView(a, header);
                    this.headers.add(header);
                    int count = this.mAdapter.getCountForSection(a);
                    View child;
                    if (a == this.startSection) {
                        int pos = this.mAdapter.getPositionInSectionForPosition(itemNum);
                        if (pos == count - 1) {
                            header.setTag(Integer.valueOf(-header.getHeight()));
                        } else if (pos == count - 2) {
                            int headerTop;
                            child = getChildAt(itemNum - firstVisibleItem);
                            if (child != null) {
                                headerTop = child.getTop();
                            } else {
                                headerTop = -AndroidUtilities.dp(100.0f);
                            }
                            if (headerTop < 0) {
                                header.setTag(Integer.valueOf(headerTop));
                            } else {
                                header.setTag(Integer.valueOf(0));
                            }
                        } else {
                            header.setTag(Integer.valueOf(0));
                        }
                        itemNum += count - this.mAdapter.getPositionInSectionForPosition(firstVisibleItem);
                    } else {
                        child = getChildAt(itemNum - firstVisibleItem);
                        if (child != null) {
                            header.setTag(Integer.valueOf(child.getTop()));
                        } else {
                            header.setTag(Integer.valueOf(-AndroidUtilities.dp(100.0f)));
                        }
                        itemNum += count;
                    }
                }
            }
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private View getSectionHeaderView(int section, View oldView) {
        boolean shouldLayout;
        if (oldView == null) {
            shouldLayout = true;
        } else {
            shouldLayout = false;
        }
        View view = this.mAdapter.getSectionHeaderView(section, oldView, this);
        if (shouldLayout) {
            ensurePinnedHeaderLayout(view, false);
        }
        return view;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mAdapter != null && !this.headers.isEmpty()) {
            Iterator i$ = this.headers.iterator();
            while (i$.hasNext()) {
                ensurePinnedHeaderLayout((View) i$.next(), true);
            }
        }
    }

    private void ensurePinnedHeaderLayout(View header, boolean forceLayout) {
        if (header.isLayoutRequested() || forceLayout) {
            LayoutParams layoutParams = header.getLayoutParams();
            try {
                header.measure(MeasureSpec.makeMeasureSpec(layoutParams.width, C0747C.ENCODING_PCM_32BIT), MeasureSpec.makeMeasureSpec(layoutParams.height, C0747C.ENCODING_PCM_32BIT));
            } catch (Throwable e) {
                FileLog.m13e("tmessages", e);
            }
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mAdapter != null && !this.headers.isEmpty()) {
            Iterator i$ = this.headers.iterator();
            while (i$.hasNext()) {
                View header = (View) i$.next();
                int saveCount = canvas.save();
                int top = ((Integer) header.getTag()).intValue();
                canvas.translate(LocaleController.isRTL ? (float) (getWidth() - header.getWidth()) : 0.0f, (float) top);
                canvas.clipRect(0, 0, getWidth(), header.getMeasuredHeight());
                if (top < 0) {
                    canvas.saveLayerAlpha(0.0f, (float) top, (float) header.getWidth(), (float) (canvas.getHeight() + top), (int) (255.0f * (TouchHelperCallback.ALPHA_FULL + (((float) top) / ((float) header.getMeasuredHeight())))), 4);
                }
                header.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.mOnScrollListener = l;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        super.setOnItemClickListener(listener);
    }
}
