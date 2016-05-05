package com.yalin.googleio.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.yalin.googleio.R;

import java.util.ArrayList;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class CollectionView extends ListView {
    private static final String TAG = makeLogTag(CollectionView.class);

    private static final int BUILTIN_VIEW_TYPE_HEADER = 0;
    private static final int BUILTIN_VIEW_TYPE_COUNT = 1;
    private static final int BUILTIN_VIEW_TYPE_GROUP = 0;

    private Inventory mInventory = new Inventory();
    private boolean mCustomGroupViewDisabled = false;
    private int mContentTopClearance = 0;
    private int mInternalPadding;

    private CollectionViewCallbacks mCallbacks = null;

    public CollectionView(Context context) {
        this(context, null);
    }

    public CollectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAdapter(new MyListAdapter());
        if (attrs != null) {
            final TypedArray xmlArgs = context.obtainStyledAttributes(attrs,
                    R.styleable.CollectionView, defStyleAttr, 0);
            mInternalPadding = xmlArgs.getDimensionPixelSize(R.styleable.CollectionView_internalPadding, 0);
            mContentTopClearance = xmlArgs.getDimensionPixelSize(R.styleable.CollectionView_contentTopClearance, 0);
            xmlArgs.recycle();
        }
    }

    public void setContentTopClearance(int clearance) {
        if (mContentTopClearance != clearance) {
            mContentTopClearance = clearance;
            setPadding(getPaddingLeft(), mContentTopClearance, getPaddingRight(), getPaddingBottom());
            notifyAdapterDataSetChanged();
        }
    }

    public void updateInventory(final Inventory inv, boolean animate) {
        if (animate) {
            LOGD(TAG, "CollectionView updating inventory with animation.");
            setAlpha(0);
            updateInventoryImmediate(inv, true);
            doFadeInAnimation();
        } else {
            LOGD(TAG, "CollectionView updating inventory without animation.");
            updateInventoryImmediate(inv, false);
        }
    }

    private void updateInventoryImmediate(Inventory inv, boolean animate) {
        mInventory = new Inventory(inv);
        notifyAdapterDataSetChanged();
        if (animate) {
            startLayoutAnimation();
        }
    }

    private void doFadeInAnimation() {
        setAlpha(0);
        ViewCompat.animate(this)
                .setDuration(250)
                .alpha(1)
                .withLayer();
    }

    public void setCollectionAdapter(CollectionViewCallbacks adapter) {
        mCallbacks = adapter;
    }

    private void notifyAdapterDataSetChanged() {
        setAdapter(new MyListAdapter());
    }

    private boolean hasCustomGroupView() {
        return !mCustomGroupViewDisabled
                && mCallbacks instanceof CollectionViewCallbacks.GroupCollectionViewCallbacks;
    }

    private void disableCustomGroupView() {
        mCustomGroupViewDisabled = true;
    }

    private void enableCustomGroupView() {
        mCustomGroupViewDisabled = false;
    }

    protected class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (hasCustomGroupView()) {
                return mInventory.mGroups.size();
            }
            int rowCount = 0;
            for (InventoryGroup group : mInventory.mGroups) {
                int thisGroupRowCount = group.getRowCount();
                rowCount += thisGroupRowCount;
            }
            return rowCount;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getRowView(position, convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            return getRowViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            if (hasCustomGroupView()) {
                return 1;
            } else {
                return BUILTIN_VIEW_TYPE_COUNT + mInventory.mGroups.size();
            }
        }
    }

    private View getRowView(int row, View convertView, ViewGroup parent) {
        RowComputeResult rowComputeResult = new RowComputeResult();
        if (computeRowContent(row, rowComputeResult)) {
            return makeRow(convertView, rowComputeResult, parent);
        } else {
            LOGE(TAG, "Invalid row passed to getView: " + row);
            return convertView != null ? convertView : new View(getContext());
        }
    }

    private int getRowViewType(int row) {
        RowComputeResult rowComputeResult = new RowComputeResult();
        if (computeRowContent(row, rowComputeResult)) {
            if (hasCustomGroupView()) {
                return BUILTIN_VIEW_TYPE_GROUP;
            } else if (rowComputeResult.isHeader) {
                return BUILTIN_VIEW_TYPE_HEADER;
            } else {
                return BUILTIN_VIEW_TYPE_COUNT + mInventory.getGroupIndex(rowComputeResult.groupId);
            }
        } else {
            LOGE(TAG, "Invalid row passed to getItemViewType: " + row);
            return 0;
        }
    }

    private class RowComputeResult {
        int row;
        boolean isHeader;
        int groupId;
        InventoryGroup group;
        int groupOffset;
    }

    private boolean computeRowContent(int row, RowComputeResult result) {
        int curRow = 0;
        int posInGroup;
        if (hasCustomGroupView()) {
            if (row >= mInventory.mGroups.size()) {
                return false;
            }
            InventoryGroup group = mInventory.mGroups.get(row);

            result.row = row;
            result.isHeader = false;
            result.groupId = group.mGroupId;
            result.group = group;
            result.groupOffset = 0;
            for (int i = 0; i < row; i++) {
                InventoryGroup previousGroup = mInventory.mGroups.get(i);
                result.groupOffset += previousGroup.getRowCount();
            }
            return true;
        }
        for (InventoryGroup group : mInventory.mGroups) {
            if (group.mShowHeader) {
                if (curRow == row) {
                    result.row = row;
                    result.isHeader = true;
                    result.groupId = group.mGroupId;
                    result.group = group;
                    result.groupOffset = -1;
                }
                curRow++;
            }
            posInGroup = 0;
            while (posInGroup < group.mItemCount) {
                if (curRow == row) {
                    result.row = row;
                    result.isHeader = false;
                    result.groupId = group.mGroupId;
                    result.group = group;
                    result.groupOffset = posInGroup;
                    return true;
                }
                posInGroup += group.mDisplayCols;
                curRow++;
            }
        }
        return false;
    }

    private View makeRow(View view, RowComputeResult rowInfo, ViewGroup parent) {
        if (mCallbacks == null) {
            LOGE(TAG, "Call to makeRow without an adapter installed.");
            return view != null ? view : new View(getContext());
        }

        String desiredViewType = mInventory.hashCode() + "." + getRowViewType(rowInfo.row);
        String actualViewType = (view != null && view.getTag() != null) ? view.getTag().toString() : "";
        if (!desiredViewType.equals(actualViewType)) {
            view = null;
        }
        if (rowInfo.isHeader) {
            if (view == null) {
                view = mCallbacks.newCollectionHeaderView(getContext(), rowInfo.groupId, parent);
            }
            mCallbacks.bindCollectionHeaderView(getContext(), view, rowInfo.groupId,
                    rowInfo.group.mHeaderLabel, rowInfo.group.mHeaderTag);
        } else {
            view = makeItemRow(view, rowInfo);
        }
        view.setTag(desiredViewType);
        return view;
    }

    private View makeItemRow(View convertView, RowComputeResult rowInfo) {
        if (convertView == null) {
            return makeNewItemRow(rowInfo);
        } else {
            return recyclerItemRow(convertView, rowInfo);
        }
    }

    private View makeNewItemRow(RowComputeResult rowInfo) {
        LinearLayout ll = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(params);

        int nbColumns = rowInfo.group.mDisplayCols;
        if (hasCustomGroupView()) {
            nbColumns = 1;
        }
        for (int i = 0; i < nbColumns; i++) {
            View view = getItemView(rowInfo, i, null, ll);
            setupLayoutParams(view);
            ll.addView(view);
        }
        return ll;
    }

    private View recyclerItemRow(View convertView, RowComputeResult rowInfo) {
        LinearLayout ll = (LinearLayout) convertView;
        int nbColumns = rowInfo.group.mDisplayCols;
        if (hasCustomGroupView()) {
            nbColumns = 1;
        }

        for (int i = 0; i < nbColumns; i++) {
            View view = ll.getChildAt(i);
            View newView = getItemView(rowInfo, i, view, ll);
            if (view != newView) {
                setupLayoutParams(newView);
                ll.removeViewAt(i);
                ll.addView(newView, i);
            }
        }
        return ll;
    }

    private View getItemView(RowComputeResult rowInfo, int column, View view, ViewGroup parent) {
        if (hasCustomGroupView()) {
            return createGroupView(rowInfo, view, parent);
        }

        int indexInGroup = rowInfo.groupOffset + column;
        if (indexInGroup >= rowInfo.group.mItemCount) {
            if (view != null && view instanceof EmptyView) {
                return view;
            }
            view = new EmptyView(getContext());
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return view;
        }
        if (view == null || view instanceof EmptyView) {
            view = mCallbacks.newCollectionItemView(getContext(), rowInfo.groupId, parent);
        }

        mCallbacks.bindCollectionItemView(getContext(), view, rowInfo.groupId,
                indexInGroup, rowInfo.group.getDataIndex(indexInGroup),
                rowInfo.group.getItemTag(rowInfo.groupOffset + column));
        return view;
    }

    private void setupLayoutParams(View view) {
        if (hasCustomGroupView()) {
            return;
        }

        LinearLayout.LayoutParams viewLayoutParams;
        if (view.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            viewLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        } else {
            LOGW(TAG, "Unexpected class for collection view item's layout params: " +
                    view.getLayoutParams().getClass().getName());
            viewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        viewLayoutParams.leftMargin = mInternalPadding / 2;
        viewLayoutParams.rightMargin = mInternalPadding / 2;
        viewLayoutParams.bottomMargin = mInternalPadding;
        viewLayoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        viewLayoutParams.weight = 1.0f;
        view.setLayoutParams(viewLayoutParams);
    }

    private View createGroupView(RowComputeResult rowInfo, View view, ViewGroup parent) {
        ViewGroup groupView;
        if (view != null && view instanceof ViewGroup) {
            groupView = (ViewGroup) view;
            if (groupView.getChildAt(0) instanceof LinearLayout) {
                LinearLayout groupViewContent = (LinearLayout) groupView.getChildAt(0);
                if (groupViewContent.getChildCount() > rowInfo.group.getRowCount()) {
                    groupViewContent.removeViews(rowInfo.group.getRowCount(),
                            groupViewContent.getChildCount() - rowInfo.group.getRowCount());
                }
            }
        } else if (mCallbacks instanceof CollectionViewCallbacks.GroupCollectionViewCallbacks) {
            groupView = ((CollectionViewCallbacks.GroupCollectionViewCallbacks) mCallbacks)
                    .newCollectionGroupView(getContext(), rowInfo.groupId, rowInfo.group, parent);
        } else {
            LOGE(TAG, "Tried to create a group view but the callback is not an instance of "
                    + "GroupCollectionViewCallbacks");
            return new EmptyView(getContext());
        }
        LinearLayout groupViewContent;
        if (groupView.getChildAt(0) instanceof LinearLayout) {
            groupViewContent = (LinearLayout) groupView.getChildAt(0);
        } else {
            groupViewContent = new LinearLayout(getContext());
            groupViewContent.setOrientation(LinearLayout.VERTICAL);
            LayoutParams llParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            groupViewContent.setLayoutParams(llParams);
            groupView.addView(groupViewContent);
        }
        disableCustomGroupView();
        for (int i = 0; i < rowInfo.group.getRowCount(); i++) {
            View itemView;
            try {
                itemView = getRowView(rowInfo.groupOffset + i, groupViewContent.getChildAt(i),
                        groupViewContent);
            } catch (Exception e) {
                itemView = getRowView(rowInfo.groupOffset + i, null, groupViewContent);
            }
            if (itemView != groupViewContent.getChildAt(i)) {
                if (groupViewContent.getChildCount() > i) {
                    groupViewContent.removeViewAt(i);
                }
                groupViewContent.addView(itemView, i);
            }
        }
        enableCustomGroupView();
        return groupView;
    }

    private static class EmptyView extends View {
        public EmptyView(Context context) {
            super(context);
        }
    }

    public static class Inventory {
        private ArrayList<InventoryGroup> mGroups = new ArrayList<>();

        public Inventory() {
        }

        public Inventory(Inventory copyFrom) {
            for (InventoryGroup group : copyFrom.mGroups) {
                mGroups.add(group);
            }
        }

        public void addGroup(InventoryGroup group) {
            if (group.mItemCount > 0) {
                mGroups.add(new InventoryGroup(group));
            }
        }

        public int getTotalItemCount() {
            int total = 0;
            for (InventoryGroup group : mGroups) {
                total += group.mItemCount;
            }
            return total;
        }

        public int getGroupCount() {
            return mGroups.size();
        }

        public int getGroupIndex(int groupId) {
            for (int i = 0; i < mGroups.size(); i++) {
                if (mGroups.get(i).mGroupId == groupId) {
                    return i;
                }
            }

            return -1;
        }
    }

    public static class InventoryGroup implements Cloneable {
        private int mGroupId = 0;
        private boolean mShowHeader = false;
        private String mHeaderLabel = "";
        private Object mHeaderTag;
        private int mDataIndexStart = 0;
        private int mDisplayCols = 1;
        private int mItemCount = 0;
        private SparseArray<Object> mItemTag = new SparseArray<>();
        private SparseArray<Integer> mItemCustomDataIndices = new SparseArray<>();

        public InventoryGroup(int groupId) {
            mGroupId = groupId;
        }

        public InventoryGroup(InventoryGroup copyFrom) {
            mGroupId = copyFrom.mGroupId;
            mShowHeader = copyFrom.mShowHeader;
            mDataIndexStart = copyFrom.mDataIndexStart;
            mDisplayCols = copyFrom.mDisplayCols;
            mItemCount = copyFrom.mItemCount;
            mHeaderLabel = copyFrom.mHeaderLabel;
            mHeaderTag = copyFrom.mHeaderTag;
            mItemTag = cloneSparseArray(copyFrom.mItemTag);
            mItemCustomDataIndices = cloneSparseArray(copyFrom.mItemCustomDataIndices);
        }

        public InventoryGroup setShowHeader(boolean showHeader) {
            mShowHeader = showHeader;
            return this;
        }

        public InventoryGroup setHeaderLabel(String label) {
            mHeaderLabel = label;
            return this;
        }

        public String getHeaderLabel() {
            return mHeaderLabel;
        }

        public InventoryGroup setHeaderTag(Object headerTag) {
            mHeaderTag = headerTag;
            return this;
        }

        public Object getHeaderTag() {
            return mHeaderTag;
        }

        public InventoryGroup setDataIndexStart(int dataIndexStart) {
            mDataIndexStart = dataIndexStart;
            return this;
        }

        public InventoryGroup setCustomDataIndex(int groupIndex, int customDataIndex) {
            mItemCustomDataIndices.put(groupIndex, customDataIndex);
            return this;
        }

        public int getDataIndex(int indexInGroup) {
            return mItemCustomDataIndices.get(indexInGroup, mDataIndexStart + indexInGroup);
        }

        public InventoryGroup setDisplayCols(int cols) {
            mDisplayCols = cols > 1 ? cols : 1;
            return this;
        }

        public InventoryGroup setItemCount(int count) {
            mItemCount = count;
            return this;
        }

        public InventoryGroup setItemTag(int index, Object tag) {
            mItemTag.put(index, tag);
            return this;
        }

        public InventoryGroup incrementItemCount() {
            mItemCount++;
            return this;
        }

        public InventoryGroup addItemWithTag(Object tag) {
            mItemCount++;
            setItemTag(mItemCount - 1, tag);
            return this;
        }

        public InventoryGroup addItemWithCustomDataIndex(int customDataIndex) {
            mItemCount++;
            setCustomDataIndex(mItemCount - 1, customDataIndex);
            return this;
        }

        public int getRowCount() {
            return (mShowHeader ? 1 : 0) + (mItemCount / mDisplayCols) + ((mItemCount % mDisplayCols > 0) ? 1 : 0);
        }

        public Object getItemTag(int i) {
            return mItemTag.get(i, null);
        }

        private static <E> SparseArray<E> cloneSparseArray(SparseArray<E> orig) {
            SparseArray<E> result = new SparseArray<>();
            for (int i = 0; i < orig.size(); i++) {
                result.put(orig.keyAt(i), orig.valueAt(i));
            }
            return result;
        }
    }
}
