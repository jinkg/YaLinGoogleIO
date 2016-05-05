package com.yalin.googleio.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yalin.googleio.Config;

import java.util.TimeZone;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class SettingsUtils {
    private static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2015";
    private static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in" + CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_DECLINED_WIFI_SETUP = "pref_declined_wifi_setup" + CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_DATA_BOOTSTRAP_DONE = "pref_data_bootstrap_done";

    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    public static final String PREF_SUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    public static final String PREF_ATTENDEE_AT_VENUE = "pref_attendee_at_venue" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }

    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DATA_BOOTSTRAP_DONE, false);
    }

    public static boolean isAttendeeAtVenue(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ATTENDEE_AT_VENUE, true);
    }

    public static long getLastSyncAttemptTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_SUR_SYNC_INTERVAL, 0L);
    }

    public static TimeZone getDisplayTimeZone(final Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null) ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    public static boolean isUsingLocalTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    public static boolean hasDeclinedWifiSetup(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DECLINED_WIFI_SETUP, false);
    }

    public static void markDeclinedWifiSetup(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DECLINED_WIFI_SETUP, newValue).apply();
    }
}
