package com.yalin.googleio.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.yalin.googleio.R;
import com.yalin.googleio.framework.Model;
import com.yalin.googleio.framework.PresenterFragmentImpl;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.framework.UpdatableView;
import com.yalin.googleio.framework.UserActionEnum;
import com.yalin.googleio.service.DataBootstrapService;
import com.yalin.googleio.util.AccountUtils;
import com.yalin.googleio.util.LUtils;
import com.yalin.googleio.util.LoginAndAuthHelper;
import com.yalin.googleio.util.RecentTasksStyler;
import com.yalin.googleio.welcome.WelcomeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class BaseActivity extends AppCompatActivity implements LoginAndAuthHelper.Callbacks {
    private static final String TAG = makeLogTag(BaseActivity.class);

    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    private static final int ACCOUNT_BOX_EXPAND_ANIM_DURATION = 200;

    public static final String PRESENTER_TAG = "Presenter";

    private DrawerLayout mDrawerLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LUtils mLUtils;

    private int mThemedStatusBarColor;

    private int mNormalStatusBarColor;

    private int mProgressBarTopWhenActionBarShown;

    private LoginAndAuthHelper mLoginAndAuthHelper;

    private ArrayList<View> mHideableHeaderViews = new ArrayList<>();

    private boolean mActionBarAutoHideEnabled = false;

    private int mActionBarAuthHideSensitivity = 0;

    private int mActionBarAuthHideMinY = 0;

    private int mActionBarAuthHideSignal = 0;

    private boolean mActionBarShown = true;

    private ObjectAnimator mStatusBarColorAnimator;

    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);

        if (WelcomeActivity.shouldDisplay(this)) {
            Intent intent = WelcomeActivity.getOpenIntent(this);
            startActivity(intent);
            finish();
            return;
        }

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mThemedStatusBarColor = getResources().getColor(R.color.colorPrimaryDark);
        mNormalStatusBarColor = mThemedStatusBarColor;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolBar();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataBootstrapService.startDataBootstrapIfNecessary(this);

        startLoginProcess();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLoginAndAuthHelper != null) {
            mLoginAndAuthHelper.stop();
        }
    }

    private void getActionBarToolBar() {

    }

    private void startLoginProcess() {
        LOGD(TAG, "Starting login process.");
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "No active account, attempting to pick a default.");
            String defaultAccount = AccountUtils.getActiveAccountName(this);
            if (defaultAccount == null) {
                LOGD(TAG, "Failed to pick default account (no accounts). Failing.");
                return;
            }
            LOGD(TAG, "Default to: " + defaultAccount);
            AccountUtils.setActiveAccount(this, defaultAccount);
        }
        if (!AccountUtils.hasActiveAccount(this)) {
            LOGD(TAG, "Can't proceed with login -- no account chosen.");
            return;
        }
        String accountName = AccountUtils.getActiveAccountName(this);
        LOGD(TAG, "Chosen account: " + accountName);

        if (mLoginAndAuthHelper != null && mLoginAndAuthHelper.getAccountName()
                .equals(accountName)) {
            LOGD(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthHelper.start();
            return;
        }

        LOGD(TAG, "Starting login process with account " + accountName);
        if (mLoginAndAuthHelper != null) {
            LOGD(TAG, "Tearing down old Helper, was " + mLoginAndAuthHelper.getAccountName());
            if (mLoginAndAuthHelper.ismStarted()) {
                LOGD(TAG, "Stopping old Helper.");
                mLoginAndAuthHelper.stop();
            }
            mLoginAndAuthHelper = null;
        }

        LOGD(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthHelper = new LoginAndAuthHelper(this, this, accountName);
        mLoginAndAuthHelper.start();
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {

    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {

    }

    @Override
    public void onAuthFailure(String accountName) {

    }

    public PresenterFragmentImpl addPresenterFragment(int updatableViewResId, Model model, QueryEnum[] queries,
                                                      UserActionEnum[] actions) {
        FragmentManager fragmentManager = getFragmentManager();
        PresenterFragmentImpl presenter = (PresenterFragmentImpl) fragmentManager.findFragmentByTag(PRESENTER_TAG);
        if (presenter == null) {
            presenter = new PresenterFragmentImpl();
            setUpPresenter(presenter, fragmentManager, updatableViewResId, model, queries, actions);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(presenter, PRESENTER_TAG);
            fragmentTransaction.commit();
        } else {
            setUpPresenter(presenter, fragmentManager, updatableViewResId, model, queries, actions);
        }
        return presenter;
    }

    public void setUpPresenter(PresenterFragmentImpl presenter, FragmentManager fragmentManager,
                               int updatableViewResId, Model model, QueryEnum[] queries,
                               UserActionEnum[] actions) {
        UpdatableView ui = (UpdatableView) fragmentManager.findFragmentById(updatableViewResId);
        presenter.setModel(model);
        presenter.setUpdatableView(ui);
        presenter.setInitialQueriesToLoad(queries);
        presenter.setValidUserActions(actions);
    }

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    protected void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    protected void enableActionBarAuthHide(final ListView listView) {
        initActionBarAutoHide();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private Map<Integer, Integer> heights = new HashMap<>();
            private int lastCurrentScroolY = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View firstVisibleItemView = view.getChildAt(0);
                if (firstVisibleItemView == null) {
                    return;
                }
                heights.put(firstVisibleItem, firstVisibleItemView.getHeight());
                int previousItemsHeight = 0;
                for (int i = 0; i < firstVisibleItem; i++) {
                    previousItemsHeight += heights.get(i) != null ? heights.get(i) : 0;
                }
                int currentScrollY = previousItemsHeight - firstVisibleItemView.getTop()
                        + view.getPaddingTop();
                onMainContentScrolled(currentScrollY, currentScrollY - lastCurrentScroolY);

                lastCurrentScroolY = currentScrollY;
            }
        });
    }

    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAuthHideMinY = getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_min_y);
        mActionBarAuthHideSensitivity = getResources().getDimensionPixelSize(R.dimen.action_bar_auto_hide_sensitivity);
    }

    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAuthHideSensitivity) {
            deltaY = mActionBarAuthHideSensitivity;
        } else if (deltaY < -mActionBarAuthHideSensitivity) {
            deltaY = -mActionBarAuthHideSensitivity;
        }
        if (Math.signum(deltaY) * Math.signum(mActionBarAuthHideSignal) < 0) {
            mActionBarAuthHideSignal = deltaY;
        } else {
            mActionBarAuthHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAuthHideMinY || (mActionBarAuthHideSignal <= -mActionBarAuthHideSensitivity);
        autoShowOrHideActionBar(shouldShow);
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }
        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
        mStatusBarColorAnimator = ObjectAnimator.ofInt((mDrawerLayout != null) ? mDrawerLayout : mLUtils,
                (mDrawerLayout != null) ? "statusBarBackgroundColor" : "statusBarColor",
                shown ? Color.BLACK : mNormalStatusBarColor,
                shown ? mNormalStatusBarColor : Color.BLACK)
                .setDuration(250);
        if (mDrawerLayout != null) {
            mStatusBarColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewCompat.postInvalidateOnAnimation(mDrawerLayout);
                }
            });
        }

        mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
        mStatusBarColorAnimator.start();

        updateSwipeRefreshProgressBarTop();

        for (final View view : mHideableHeaderViews) {
            if (shown) {
                ViewCompat.animate(view)
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .withLayer();
            } else {
                ViewCompat.animate(view)
                        .translationY(-view.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator())
                        .withLayer();
            }
        }
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = mActionBarShown ? mProgressBarTopWhenActionBarShown : 0;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);
    }
}
