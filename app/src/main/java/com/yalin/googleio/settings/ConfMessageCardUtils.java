package com.yalin.googleio.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.yalin.googleio.util.TimeUtils;
import com.yalin.googleio.util.UIUtils;

import java.util.HashMap;
import java.util.Random;

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

    private static final int WIFI_FEEDBACK_RANDOM_INT_UPPER_RANGE = 30;

    private static Random random = new Random();

    public enum ConfMessageCard {
        CONFERENCE_CREDENTIALS("2015-05-27T09:00:00-07:00", "2015-05-27T18:00:00-07:00"),

        KEYNOTE_ACCESS("2015-05-27T09:00:00-07:00", "2015-05-28T09:30:00-07:00"),

        AFTER_HOURS("2015-05-28T11:00:00-07:00", "2015-05-28T17:00:00-07:00"),

        WIFI_FEEDBACK("2015-05-28T09:30:00-07:00", "2015-05-29T17:30:00-07:00");


        long mStartTime;
        long mEndTime;

        ConfMessageCard(String startTime, String endTime) {
            mStartTime = TimeUtils.parseTimestamp(startTime).getTime();
            mEndTime = TimeUtils.parseTimestamp(endTime).getTime();
        }

        public boolean isActive(long millisSinceEpoch) {
            boolean returnVal = mStartTime <= millisSinceEpoch && mEndTime >= millisSinceEpoch;
            if (WIFI_FEEDBACK.equals(this)) {
                return random.nextInt(WIFI_FEEDBACK_RANDOM_INT_UPPER_RANGE) == 1;
            }
            return returnVal;
        }
    }

    private static final HashMap<ConfMessageCard, String> ConfMessageCardsShouldShowMap = new HashMap<>();

    public static void registerPreferencesChangeListener(final Context context, ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterPreferencesChangeListener(final Context context, ConferencePrefChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean isConfMessageCardsEnabled(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_CONF_MESSAGE_CARDS_ENABLED, false);
    }

    public static void setConfMessageCardsEnabled(final Context context, @Nullable Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(PREF_CONF_MESSAGE_CARDS_ENABLED).apply();
        } else {
            sp.edit().putBoolean(PREF_CONF_MESSAGE_CARDS_ENABLED, newValue).apply();
        }
    }

    public static boolean hasAnsweredConfMessageCardsPrompt(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, false);
    }

    public static void markAnsweredConfMessageCardsPrompt(final Context context, @Nullable Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT).apply();
        } else {
            sp.edit().putBoolean(PREF_ANSWERED_CONF_MESSAGE_CARDS_PROMPT, newValue).apply();
        }
    }

    public static void markShouldShowConfMessageCard(final Context context, ConfMessageCard card, Boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (newValue == null) {
            sp.edit().remove(ConfMessageCardsShouldShowMap.get(card)).apply();
        } else {
            sp.edit().putBoolean(ConfMessageCardsShouldShowMap.get(card), newValue).apply();
        }
    }

    public static void enableActiveCards(final Context context) {
        long currentTime = UIUtils.getCurrentTime(context);
        for (ConfMessageCard card : ConfMessageCard.values()) {
            if (card.isActive(currentTime)) {
                markShouldShowConfMessageCard(context, card, true);
            }
        }
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
