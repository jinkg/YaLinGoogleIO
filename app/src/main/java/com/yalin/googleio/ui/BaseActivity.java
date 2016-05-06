package com.yalin.googleio.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.yalin.googleio.BuildConfig;
import com.yalin.googleio.R;
import com.yalin.googleio.explore.ExploreIOActivity;
import com.yalin.googleio.explore.ExploreSessionsActivity;
import com.yalin.googleio.framework.Model;
import com.yalin.googleio.framework.PresenterFragmentImpl;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.framework.UpdatableView;
import com.yalin.googleio.framework.UserActionEnum;
import com.yalin.googleio.myschedule.MyScheduleActivity;
import com.yalin.googleio.service.DataBootstrapService;
import com.yalin.googleio.ui.widget.NavDrawerItemView;
import com.yalin.googleio.ui.widget.ScrimInsetsScrollView;
import com.yalin.googleio.util.AccountUtils;
import com.yalin.googleio.util.LUtils;
import com.yalin.googleio.util.LoginAndAuthHelper;
import com.yalin.googleio.util.RecentTasksStyler;
import com.yalin.googleio.util.SettingsUtils;
import com.yalin.googleio.util.UIUtils;
import com.yalin.googleio.welcome.WelcomeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGW;
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

    private static final int NAV_DRAWER_LAUNCH_DELAY = 250;

    protected static final int NAV_DRAWER_ITEM_MY_SCHEDULE = 0;

    protected static final int NAV_DRAWER_ITEM_EXPLORE = 2;

    protected static final int NAV_DRAWER_ITEM_MAP = 3;

    protected static final int NAV_DRAWER_ITEM_SOCIAL = 4;

    protected static final int NAV_DRAWER_ITEM_VIDEO_LIBRARY = 5;

    protected static final int NAV_DRAWER_ITEM_SIGN_IN = 6;

    protected static final int NAV_DRAWER_ITEM_SETTINGS = 7;

    protected static final int NAV_DRAWER_ITEM_ABOUT = 8;

    protected static final int NAV_DRAWER_ITEM_DEBUG = 9;

    protected static final int NAV_DRAWER_ITEM_INVALID = -1;

    protected static final int NAV_DRAWER_ITEM_SEPARATOR = -2;

    protected static final int NAV_DRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    public static final String PRESENTER_TAG = "Presenter";

    private DrawerLayout mDrawerLayout;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Toolbar mActionBarToolbar;

    private LUtils mLUtils;

    private Handler mHandler;

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

    private Runnable mDeferredOnDrawerClosedRunnable;

    private boolean mAccountBoxExpanded = false;

    private View[] mNavDrawerItemViews = null;

    private ArrayList<Integer> mNavDrawerItems = new ArrayList<>();

    private ViewGroup mDrawerItemsListContainer;

    private static final int[] NAV_DRAWER_TITLE_RES_ID = new int[]{
            R.string.nav_drawer_item_my_schedule,
            R.string.nav_drawer_item_io_live,
            R.string.nav_drawer_item_explore,
            R.string.nav_drawer_item_map,
            R.string.nav_drawer_item_social,
            R.string.nav_drawer_item_video_library,
            R.string.nav_drawer_item_sign_in,
            R.string.nav_drawer_item_settings,
            R.string.nav_drawer_item_about,
            R.string.nav_drawer_item_debug
    };

    private static final int[] NAV_DRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_nav_view_my_schedule,
            R.drawable.ic_nav_view_play_circle_fill,
            R.drawable.ic_nav_view_explore,
            R.drawable.ic_nav_view_map,
            R.drawable.ic_nav_view_social,
            R.drawable.ic_nav_view_video_library,
            0,
            R.drawable.ic_nav_view_settings,
            R.drawable.ic_info_outline,
            R.drawable.ic_nav_view_settings
    };

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

        mHandler = new Handler();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mLUtils = LUtils.getInstance(this);
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
        setupNavDrawer();
        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            LOGW(TAG, "No view with ID main_content to fade in.");
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

    private void setupNavDrawer() {
        int selfItem = getSelfNavDrawerItem();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView) mDrawerLayout.findViewById(R.id.nav_drawer);
        if (selfItem == NAV_DRAWER_ITEM_INVALID) {
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        if (navDrawer != null) {
            final View chosenAccountContentView = findViewById(R.id.chosen_account_content_view);
            final View chosenAccountView = findViewById(R.id.chosen_account_view);
            final int navDrawerChosenAccountHeight = getResources().getDimensionPixelSize(
                    R.dimen.nav_drawer_chosen_account_height);
            navDrawer.setOnInsetsCallback(new ScrimInsetsScrollView.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) chosenAccountContentView.getLayoutParams();
                    lp.topMargin = insets.top;
                    chosenAccountContentView.setLayoutParams(lp);

                    ViewGroup.LayoutParams lp2 = chosenAccountView.getLayoutParams();
                    lp2.height = navDrawerChosenAccountHeight + insets.top;
                    chosenAccountView.setLayoutParams(lp2);
                }
            });
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_ab_drawer);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                onNavDrawerStateChanged(true, false);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                if (mAccountBoxExpanded) {
                    mAccountBoxExpanded = false;
                    setupAccountBoxToggle();
                }
                onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                onNavDrawerStateChanged(isNavDrawerOpen(), newState != DrawerLayout.STATE_IDLE);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        populateNavDrawer();

        if (!SettingsUtils.isFirstRunProcessComplete(this)) {
            SettingsUtils.markFirstRunProcessesDone(this, true);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    protected int getSelfNavDrawerItem() {
        return NAV_DRAWER_ITEM_INVALID;
    }

    protected LUtils getLUtils() {
        return mLUtils;
    }

    private void setupAccountBoxToggle() {

    }

    protected void onNavDrawerStateChanged(boolean isOpen, boolean inAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    protected void onNavDrawerSlide(float offset) {

    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void populateNavDrawer() {
        boolean attendeeAtVenue = SettingsUtils.isAttendeeAtVenue(this);

        mNavDrawerItems.clear();
        if (AccountUtils.hasActiveAccount(this)) {
            mNavDrawerItems.add(NAV_DRAWER_ITEM_MY_SCHEDULE);
        } else {
            mNavDrawerItems.add(NAV_DRAWER_ITEM_SIGN_IN);
        }

        mNavDrawerItems.add(NAV_DRAWER_ITEM_EXPLORE);

        if (attendeeAtVenue) {
            mNavDrawerItems.add(NAV_DRAWER_ITEM_MAP);
        }
        mNavDrawerItems.add(NAV_DRAWER_ITEM_SEPARATOR);

        mNavDrawerItems.add(NAV_DRAWER_ITEM_SOCIAL);
        mNavDrawerItems.add(NAV_DRAWER_ITEM_VIDEO_LIBRARY);
        mNavDrawerItems.add(NAV_DRAWER_ITEM_SEPARATOR_SPECIAL);
        mNavDrawerItems.add(NAV_DRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAV_DRAWER_ITEM_ABOUT);

        if (BuildConfig.DEBUG) {
            mNavDrawerItems.add(NAV_DRAWER_ITEM_DEBUG);
        }

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.nav_drawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        if (isSeparator(itemId)) {
            View separator =
                    getLayoutInflater().inflate(R.layout.nav_drawer_separator, container, false);
            UIUtils.setAccessibilityIgnore(separator);
            return separator;
        }

        NavDrawerItemView item = (NavDrawerItemView) getLayoutInflater().inflate(R.layout.nav_drawer_item, container, false);
        item.setContent(NAV_DRAWER_ICON_RES_ID[itemId], NAV_DRAWER_TITLE_RES_ID[itemId]);
        item.setActivated(getSelfNavDrawerItem() == itemId);
        if (item.isActivated()) {
            item.setContentDescription(getString(R.string.nav_drawer_selected_menu_item_ally_wrapper,
                    getString(NAV_DRAWER_TITLE_RES_ID[itemId])));
        } else {
            item.setContentDescription(getString(R.string.nav_drawer_menu_item_ally_wrapper,
                    getString(NAV_DRAWER_TITLE_RES_ID[itemId])));
        }
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return item;
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAV_DRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAV_DRAWER_ITEM_SEPARATOR || itemId == NAV_DRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    protected Toolbar getActionBarToolBar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                mActionBarToolbar.setNavigationContentDescription(R.string.nav_drawer_description_a11y);
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            closeNavDrawer();
            return;
        }
        if (isSpecialItem(itemId)) {
            gotoNavDrawerItem(itemId);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gotoNavDrawerItem(itemId);
                }
            }, NAV_DRAWER_LAUNCH_DELAY);

            setSelectedNavDrawerItem(itemId);

            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEIN_DURATION);
            }
        }
        closeNavDrawer();
    }

    private void gotoNavDrawerItem(int item) {
        switch (item) {
            case NAV_DRAWER_ITEM_MY_SCHEDULE:
                createBackStack(new Intent(this, MyScheduleActivity.class));
                break;
            case NAV_DRAWER_ITEM_EXPLORE:
                startActivity(new Intent(this, ExploreIOActivity.class));
                finish();
                break;
            case NAV_DRAWER_ITEM_MAP:
                createBackStack(new Intent(this, ExploreIOActivity.class));
                break;
            case NAV_DRAWER_ITEM_SOCIAL:
                createBackStack(new Intent(this, ExploreIOActivity.class));
                break;
            case NAV_DRAWER_ITEM_VIDEO_LIBRARY:
                createBackStack(new Intent(this, ExploreSessionsActivity.class));
                break;
            case NAV_DRAWER_ITEM_SIGN_IN:
                signInOrCreateAnAccount();
                break;
            case NAV_DRAWER_ITEM_SETTINGS:
                createBackStack(new Intent(this, ExploreSessionsActivity.class));
                break;
            case NAV_DRAWER_ITEM_ABOUT:
                createBackStack(new Intent(this, ExploreSessionsActivity.class));
                break;
            case NAV_DRAWER_ITEM_DEBUG:
                createBackStack(new Intent(this, ExploreSessionsActivity.class));
                break;
        }
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    mNavDrawerItemViews[i].setActivated(itemId == thisItemId);
                }
            }
        }
    }

    private void createBackStack(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntentWithParentStack(intent);
            builder.startActivities();
        } else {
            startActivity(intent);
            finish();
        }
    }

    private void signInOrCreateAnAccount() {
        AccountManager am = AccountManager.get(BaseActivity.this);
        Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accounts.length == 0) {
            Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
            intent.putExtra(Settings.EXTRA_ACCOUNT_TYPES,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE});
            startActivity(intent);
        } else {
            startLoginProcess();
            closeNavDrawer();
        }
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
            private int lastCurrentScrollY = 0;

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
                onMainContentScrolled(currentScrollY, currentScrollY - lastCurrentScrollY);

                lastCurrentScrollY = currentScrollY;
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
