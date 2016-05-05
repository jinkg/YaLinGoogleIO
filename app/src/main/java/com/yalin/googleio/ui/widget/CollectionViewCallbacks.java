package com.yalin.googleio.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public interface CollectionViewCallbacks {
    View newCollectionHeaderView(Context context, int groupId, ViewGroup parent);

    void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel, Object headerTag);

    View newCollectionItemView(Context context, int groupId, ViewGroup parent);

    void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag);

    interface GroupCollectionViewCallbacks extends CollectionViewCallbacks {
        ViewGroup newCollectionGroupView(Context context, int groupId, CollectionView.InventoryGroup group, ViewGroup parent);
    }
}
