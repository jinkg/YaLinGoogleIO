package com.yalin.googleio.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class SettingsUtils {
    private static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2015";
    private static final String PREF_USER_REFUSED_SIGN_IN = "pref_user_refused_sign_in" + CONFERENCE_YEAR_PREF_POSTFIX;

    public static boolean hasUserRefusedSignIn(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_USER_REFUSED_SIGN_IN, false);
    }
}
