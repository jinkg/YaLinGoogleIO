package com.yalin.googleio.util;

import android.text.format.Time;

/**
 * 作者：YaLin
 * 日期：2016/5/4.
 */
public class ParserUtils {
    public static long parseTime(String timestamp) {
        final Time time = new Time();
        time.parse3339(timestamp);
        return time.toMillis(false);
    }
}
