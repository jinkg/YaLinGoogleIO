package com.yalin.googleio.explore;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.googleio.R;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.framework.UpdatableView;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.settings.ConfMessageCardUtils;
import com.yalin.googleio.ui.widget.CollectionView;
import com.yalin.googleio.ui.widget.CollectionViewCallbacks;
import com.yalin.googleio.ui.widget.DrawShadowFrameLayout;
import com.yalin.googleio.util.ImageLoader;
import com.yalin.googleio.util.LogUtils;
import com.yalin.googleio.util.SettingsUtils;
import com.yalin.googleio.util.ThrottledContentObserver;
import com.yalin.googleio.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import static com.yalin.googleio.settings.ConfMessageCardUtils.ConferencePrefChangeListener;
import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ExploreIOFragment extends Fragment implements UpdatableView<ExploreModel>, CollectionViewCallbacks {
    private static final String TAG = makeLogTag(ExploreIOFragment.class);

    private CollectionView mCollectionView;
    private View mEmptyView;

    private ImageLoader mImageLoader;

    private List<UserActionListener> mListeners = new ArrayList<>();

    private ThrottledContentObserver mSessionsObserver, mTagsObserver;

    private ConferencePrefChangeListener mConfMessageAnswerChangeListener = new ConferencePrefChangeListener() {
        @Override
        protected void onPrefChanged(String key, boolean value) {
            fireReloadEvent();
        }
    };

    private OnSharedPreferenceChangeListener mSettingsChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (SettingsUtils.PREF_DECLINED_WIFI_SETUP.equals(key)) {
                fireReloadEvent();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_explore_io, container, false);
        findView(root);
        getActivity().overridePendingTransition(0, 0);
        return root;
    }

    private void findView(View root) {
        mCollectionView = (CollectionView) root.findViewById(R.id.explore_collection_view);
        mEmptyView = root.findViewById(android.R.id.empty);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageLoader = new ImageLoader(getActivity(), R.drawable.io_logo);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        int actionBarSize = UIUtils.calculateActionBarSize(getActivity());
        DrawShadowFrameLayout drawShadowFrameLayout = (DrawShadowFrameLayout) getActivity().findViewById(R.id.main_content);
        if (drawShadowFrameLayout != null) {
            drawShadowFrameLayout.setShadowTopOffset(actionBarSize);
        }
        setContentTopClearance(actionBarSize);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ConfMessageCardUtils.registerPreferencesChangeListener(getContext(),
                mConfMessageAnswerChangeListener);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        sp.registerOnSharedPreferenceChangeListener(mSettingsChangeListener);

        mSessionsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                fireReloadEvent();
                fireReloadTagsEvent();
            }
        });

        mTagsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                fireReloadTagsEvent();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mConfMessageAnswerChangeListener != null) {
            ConfMessageCardUtils.unregisterPreferencesChangeListener(getContext(),
                    mConfMessageAnswerChangeListener);
        }

        if (mSettingsChangeListener != null) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            sp.unregisterOnSharedPreferenceChangeListener(mSettingsChangeListener);
        }
        getActivity().getContentResolver().unregisterContentObserver(mSessionsObserver);
        getActivity().getContentResolver().unregisterContentObserver(mTagsObserver);
    }

    private void setContentTopClearance(int clearance) {
        if (mCollectionView != null) {
            mCollectionView.setContentTopClearance(clearance);
        }
    }

    private void fireReloadEvent() {

    }

    private void fireReloadTagsEvent() {

    }

    @Override
    public void displayData(ExploreModel model, QueryEnum query) {
        updateCollectionView(model);
    }

    @Override
    public void displayErrorMessage(QueryEnum query) {

    }

    @Override
    public Uri getDataUri(QueryEnum query) {
        if (query == ExploreModel.ExploreQueryEnum.SESSIONS) {
            return ScheduleContract.Sessions.CONTENT_URI;
        }
        return Uri.EMPTY;
    }

    @Override
    public void addListener(UserActionListener listener) {
        mListeners.add(listener);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public View newCollectionHeaderView(Context context, int groupId, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindCollectionHeaderView(Context context, View view, int groupId, String headerLabel, Object headerTag) {

    }

    @Override
    public View newCollectionItemView(Context context, int groupId, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindCollectionItemView(Context context, View view, int groupId, int indexInGroup, int dataIndex, Object tag) {

    }

    private void updateCollectionView(ExploreModel model) {
        LOGD(TAG, "Updating collection view.");

    }
}
