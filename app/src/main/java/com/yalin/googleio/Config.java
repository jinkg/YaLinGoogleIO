package com.yalin.googleio;

import java.util.TimeZone;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class Config {
    public interface Tags {
        String SESSIONS = "TYPE_SESSIONS";
        String SESSION_GROUPING_TAG_CATEGORY = "TYPE";

        String CATEGORY_THEME = "THEME";
        String CATEGORY_TOPIC = "TOPIC";
        String CATEGORY_TYPE = "TYPE";

        String SPECIAL_KEYNOTE = "FLAG_KEYNOTE";
    }

    public static final TimeZone CONFERENCE_TIMEZONE =
            TimeZone.getTimeZone(BuildConfig.INPERSON_TIMEZONE);
}
