package com.yalin.googleio.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class CollectionView extends ListView {
    private static final String TAG = makeLogTag(CollectionView.class);

    private int mContentTopClearance = 0;

    public CollectionView(Context context) {
        this(context, null);
    }

    public CollectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setAdapter(new MyListAdapter());
    }

    public void setContentTopClearance(int clearance) {
        if (mContentTopClearance != clearance) {
            mContentTopClearance = clearance;
            setPadding(getPaddingLeft(), mContentTopClearance, getPaddingRight(), getPaddingBottom());
            notifyAdapterDataSetChanged();
        }
    }

    private void notifyAdapterDataSetChanged() {
        setAdapter(new MyListAdapter());
    }

    protected class MyListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
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
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount();
        }
    }
}
