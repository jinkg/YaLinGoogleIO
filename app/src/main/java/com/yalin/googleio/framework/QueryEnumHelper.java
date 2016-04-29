package com.yalin.googleio.framework;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class QueryEnumHelper {

    public static QueryEnum getQueryForId(int id, QueryEnum[] enums) {
        for (QueryEnum anEnum : enums) {
            if (id == anEnum.getId()) {
                return anEnum;
            }
        }
        return null;
    }
}
