package com.yalin.googleio.welcome;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yalin.googleio.explore.ExploreIOActivity;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public abstract class WelcomeFragment extends Fragment {
    private static final String TAG = makeLogTag(WelcomeFragment.class);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        LOGD(TAG, "Creating View.");
        if (getActivity() instanceof WelcomeFragmentContainer) {
            WelcomeFragmentContainer activity = (WelcomeFragmentContainer) getActivity();
            attachToPositiveButton(activity.getPositiveButton());
            attachToNegativeButton(activity.getNegativeButton());
        }
        return view;
    }

    protected void attachToPositiveButton(Button button) {
        button.setText(getPositiveText());
        button.setOnClickListener(getPositiveListener());
    }

    protected void attachToNegativeButton(Button button) {
        button.setText(getNegativeText());
        button.setOnClickListener(getNegativeListener());
    }

    protected String getResourceString(int id) {
        if (getActivity() != null) {
            return getActivity().getResources().getString(id);
        }
        return null;
    }

    protected abstract String getPositiveText();

    protected abstract String getNegativeText();

    protected abstract View.OnClickListener getPositiveListener();

    protected abstract View.OnClickListener getNegativeListener();

    protected abstract class WelcomeFragmentOnClickListener implements View.OnClickListener {
        Activity mActivity;

        public WelcomeFragmentOnClickListener(Activity activity) {
            mActivity = activity;
        }

        void doNext() {
            LOGD(TAG, "Proceeding to next activity.");
            Intent intent = ExploreIOActivity.getOpenIntent(mActivity);
            startActivity(intent);
            mActivity.finish();
        }

        void doFinish() {
            LOGD(TAG, "Closing app.");
            mActivity.finish();
        }
    }

    interface WelcomeFragmentContainer {
        Button getPositiveButton();

        void setPositiveButtonEnabled(boolean enabled);

        Button getNegativeButton();

        void setNegativeButtonEnabled(boolean enabled);
    }


}
