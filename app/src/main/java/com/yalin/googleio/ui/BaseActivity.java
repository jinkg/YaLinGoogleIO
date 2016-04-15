package com.yalin.googleio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yalin.googleio.R;
import com.yalin.googleio.util.AccountUtils;
import com.yalin.googleio.util.LoginAndAuthHelper;
import com.yalin.googleio.util.RecentTasksStyler;
import com.yalin.googleio.welcome.WelcomeActivity;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class BaseActivity extends AppCompatActivity implements LoginAndAuthHelper.Callbacks {
    private static final String TAG = makeLogTag(BaseActivity.class);
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private int mThemedStatusBarColor;
    private int mNormalStatusBarColor;

    private LoginAndAuthHelper mLoginAndAuthHelper;

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
}
