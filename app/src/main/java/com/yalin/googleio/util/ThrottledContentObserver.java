package com.yalin.googleio.util;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ThrottledContentObserver extends ContentObserver {
    private static final int THROTTLE_DELAY = 1000;
    private Handler mHandler;
    private Callbacks mCallback;
    private Runnable mScheduleRun;

    public interface Callbacks {
        void onThrottledContentObserverFired();
    }

    public ThrottledContentObserver(Callbacks callbacks) {
        super(null);
        mHandler = new Handler();
        mCallback = callbacks;
    }

    public void cancelPendingCallback() {
        if (mScheduleRun != null) {
            mHandler.removeCallbacks(mScheduleRun);
        }
    }

    @Override
    public void onChange(boolean selfChange) {
        if (mScheduleRun != null) {
            mHandler.removeCallbacks(mScheduleRun);
        } else {
            mScheduleRun = new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onThrottledContentObserverFired();
                    }
                }
            };
        }
        mHandler.postDelayed(mScheduleRun, THROTTLE_DELAY);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(selfChange);
    }
}
