package com.yalin.googleio;

import com.yalin.googleio.util.ParserUtils;
import com.yalin.googleio.util.TimeUtils;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class Config {
    public static final long[][] CONFERENCE_DAYS = new long[][]{
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_END)},
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_END)},
    };

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

    public static final long CONFERENCE_START_MILLIS = CONFERENCE_DAYS[0][0];

    public static final long CONFERENCE_END_MILLIS = CONFERENCE_DAYS[CONFERENCE_DAYS.length - 1][1];

    public static final long WIFI_SETUP_OFFER_START = (BuildConfig.DEBUG) ?
            System.currentTimeMillis() - 1000 :
            CONFERENCE_START_MILLIS - TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS);
}
