package com.yalin.googleio.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.provider.Settings;

import com.yalin.googleio.BuildConfig;
import com.yalin.googleio.R;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class UIUtils {

    private static final int[] RES_IDS_ACTION_BAR_SIZE = {R.attr.actionBarSize};

    public static final String MOCK_DATA_PREFERENCES = "mock_data";
    public static final String PREFS_MOCK_CURRENT_TIME = "mock_current_time";

    public static int calculateActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    private static final long sAppLoadTime = System.currentTimeMillis();

    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences(MOCK_DATA_PREFERENCES, Context.MODE_PRIVATE)
                    .getLong(PREFS_MOCK_CURRENT_TIME, System.currentTimeMillis())
                    + System.currentTimeMillis() - sAppLoadTime;
        } else {
            return System.currentTimeMillis();
        }
    }
}
