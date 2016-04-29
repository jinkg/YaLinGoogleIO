package com.yalin.googleio.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ConfMessageCardUtils {
    public static final String PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT =
            "pref_answered_conf_message_cards_prompt";

    public static final String PREF_CONF_MESSAGE_CARDS_ENABLED = "pref_conf_message_cards_enabled";

    private static final String DISMISS_PREFIX = "pref_conf_message_cards_dismissed_";

    private static final String SHOULD_SHOW_PREFIX = "pref_conf_message_cards_should_show_";

    public static void registerPreferencesChangeListener(final Context context, ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterPreferencesChangeListener(final Context context, ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static class ConferencePrefChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT.equals(key)) {
                onPrefChanged(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT,
                        sp.getBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, true));
            } else if (PREF_CONF_MESSAGE_CARDS_ENABLED.equals(key)) {
                onPrefChanged(PREF_CONF_MESSAGE_CARDS_ENABLED,
                        sp.getBoolean(PREF_CONF_MESSAGE_CARDS_ENABLED, false));
            } else if (key != null && key.startsWith(DISMISS_PREFIX)) {
                onPrefChanged(key, sp.getBoolean(key, false));
            } else if (key != null && key.startsWith(SHOULD_SHOW_PREFIX)) {
                onPrefChanged(key, sp.getBoolean(key, false));
            }
        }

        protected void onPrefChanged(String key, boolean value) {
        }
    }
}
