package com.yalin.googleio.util;

import java.util.Locale;

/**
 * 作者：YaLin
 * 日期：2016/4/20.
 */
public class HashUtils {
    public static String computeWeakHash(String string) {
        return String.format(Locale.US, "%08x%08x", string.hashCode(), string.length());
    }
}
