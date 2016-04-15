package com.yalin.googleio.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class LoginAndAuthHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<People.LoadPeopleResult> {
    private static final String TAG = makeLogTag(LoginAndAuthHelper.class);
    private static final int REQUEST_AUTHENTICATE = 100;
    private static final int REQUEST_RECOVER_FROM_AUTH_ERROR = 101;
    private static final int REQUEST_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;
    private static final int REQUEST_PLAY_SERVICES_ERROR_DIALOG = 103;

    private static final List<String> AUTH_SCOPES = new ArrayList<>(Arrays.asList(
            Scopes.PLUS_LOGIN,
            Scopes.DRIVE_APPFOLDER,
            "https://www.googleapis.com/auth/plus.profile.emails.read"));

    public static final String AUTH_TOKEN_TYPE;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("oauth2:");
        for (String scope : AUTH_SCOPES) {
            sb.append(scope);
            sb.append(" ");
        }
        AUTH_TOKEN_TYPE = sb.toString().trim();
    }

    WeakReference<Activity> mActivityRef;

    WeakReference<Callbacks> mCallbacksRef;

    Context mAppContext;

    private static boolean sCanShowSignInUi = true;
    private static boolean sCanShowAuthUi = true;

    String mAccountName;

    GetTokenTask mTokenTask = null;

    boolean mStarted = false;

    boolean mResolving = false;

    private GoogleApiClient mGoogleApiClient;

    public interface Callbacks {
        void onPlusInfoLoaded(String accountName);

        void onAuthSuccess(String accountName, boolean newlyAuthenticated);

        void onAuthFailure(String accountName);
    }

    public LoginAndAuthHelper(Activity activity, Callbacks callbacks, String accountName) {
        LOGD(TAG, "Helper created. Account: " + accountName);
        mActivityRef = new WeakReference<>(activity);
        mCallbacksRef = new WeakReference<>(callbacks);
        mAppContext = activity.getApplicationContext();
        mAccountName = accountName;
        if (SettingsUtils.hasUserRefusedSignIn(activity)) {
            sCanShowSignInUi = sCanShowAuthUi = false;
        }
    }

    public void start() {
        Activity activity = getActivity("start()");
        if (activity == null) {
            return;
        }
        if (mStarted) {
            LOGW(TAG, "Helper already started. Ignoring redundant call.");
            return;
        }

        mStarted = true;
        if (mResolving) {
            LOGD(TAG, "Helper ignoring signal to start because we're resolving a failure.");
            return;
        }
        LOGD(TAG, "Helper starting. Connecting " + mAccountName);
        if (mGoogleApiClient == null) {
            LOGD(TAG, "Creating client.");

            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity);
            for (String scope : AUTH_SCOPES) {
                builder.addScope(new Scope(scope));
            }
            mGoogleApiClient = builder.addApi(Plus.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .setAccountName(mAccountName)
                    .build();
        }
        LOGD(TAG, "Connecting client.");
        mGoogleApiClient.connect();
    }

    public void stop() {
        if (!mStarted) {
            LOGW(TAG, "Helper already stopped, Ignoring redundant call.");
            return;
        }
        LOGD(TAG, "Helper stopping.");
        if (mTokenTask != null) {
            LOGD(TAG, "Helper cancelling token task.");
            mTokenTask.cancel(false);
        }
        mStarted = false;
        if (mGoogleApiClient.isConnected()) {
            LOGD(TAG, "Helper disconnecting client.");
            mGoogleApiClient.disconnect();
        }
        mResolving = false;
    }

    public boolean ismStarted() {
        return mStarted;
    }

    public String getAccountName() {
        return mAccountName;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Activity activity = getActivity("onConnected()");
        if (activity == null) {
            return;
        }
        LOGD(TAG, "Helper connected, account " + mAccountName);

        if (!AccountUtils.hasPlusInfo(activity, mAccountName)) {
            LOGD(TAG, "We don't have Google+ info for " + mAccountName + " yet, so loading.");
            PendingResult<People.LoadPeopleResult> result = Plus.PeopleApi.load(mGoogleApiClient, "me");
            result.setResultCallback(this);
        } else {
            LOGD(TAG, "No need for Google+ info, we already have it.");
        }
        if (!AccountUtils.hasToken(activity, mAccountName)) {
            LOGD(TAG, "We don't have auth token for " + mAccountName + " yet, so getting it.");
            mTokenTask = new GetTokenTask();
            mTokenTask.execute();
        } else {
            LOGD(TAG, "No need for auth token, we already have it.");
            reportAuthSuccess(false);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        LOGD(TAG, "onConnectionSuspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Activity activity = getActivity("onConnectionFailed()");
        if (activity == null) {
            return;
        }

        if (connectionResult.hasResolution()) {

            return;
        }
        LOGD(TAG, "onConnectionFailed, no resolution.");
        final int errorCode = connectionResult.getErrorCode();
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode) && sCanShowSignInUi) {
            sCanShowSignInUi = false;
            GooglePlayServicesUtil.getErrorDialog(errorCode, activity,
                    REQUEST_PLAY_SERVICES_ERROR_DIALOG).show();
        } else {
            reportAuthFailure();
        }
    }

    @Override
    public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
        LOGD(TAG, "onPeopleLoaded, status=" + loadPeopleResult.getStatus().toString());
    }

    private Activity getActivity(String methodName) {
        Activity activity = mActivityRef.get();
        if (activity == null) {
            LOGD(TAG, "Helper lost Activity reference, ignoring (" + methodName + ")");
        }
        return activity;
    }

    private void reportAuthSuccess(boolean newlyAuthenticated) {
        LOGD(TAG, "reportAuthSuccess.");
    }

    private void reportAuthFailure() {
        LOGD(TAG, "reportAuthFailure.");
    }

    private class GetTokenTask extends AsyncTask<Void, Void, String> {
        public GetTokenTask() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (isCancelled()) {
                    LOGD(TAG, "doInBackground: task cancelled, so giving up on auth.");
                    return null;
                }

                LOGD(TAG, "Starting background auth for " + mAccountName);
                final String token = GoogleAuthUtil
                        .getToken(mAppContext, mAccountName, AUTH_TOKEN_TYPE);
                LOGD(TAG, "Saving token: " + (token == null ? "(null)" : "(length " +
                        token.length() + ")") + " for account " + mAccountName);
                AccountUtils.setAuthToken(mAppContext, mAccountName, token);
            } catch (GoogleAuthException e) {
                LOGE(TAG, "GoogleAuthException encountered: " + e.getMessage());
            } catch (IOException e) {
                LOGE(TAG, "IOException encountered: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (isCancelled()) {
                LOGD(TAG, "Task cancelled, so not reporting auth success.");
            } else if (!mStarted) {
                LOGD(TAG, "Activity not started, so not reporting auth success.");
            } else {
                LOGD(TAG, "GetTokenTask reporting auth success.");
                reportAuthSuccess(true);
            }
        }
    }
}
