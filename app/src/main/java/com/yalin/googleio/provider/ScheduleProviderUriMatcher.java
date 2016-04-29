package com.yalin.googleio.provider;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.SparseArray;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class ScheduleProviderUriMatcher {
    private UriMatcher mUriMatcher;

    public ScheduleProviderUriMatcher() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        buildUriMatcher();
    }

    private SparseArray<ScheduleUriEnum> mEnumsMap = new SparseArray<>();

    public ScheduleUriEnum matchUri(Uri uri) {
        final int code = mUriMatcher.match(uri);
        if (code == -1) {
            System.out.println(uri.toString());
        }
        return matchCode(code);
    }

    public ScheduleUriEnum matchCode(int code) {
        ScheduleUriEnum scheduleUriEnum = mEnumsMap.get(code);
        if (scheduleUriEnum != null) {
            return scheduleUriEnum;
        } else {
            throw new UnsupportedOperationException("Unknown uri with code " + code);
        }
    }

    private void buildUriMatcher() {
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        ScheduleUriEnum[] uris = ScheduleUriEnum.values();
        for (ScheduleUriEnum uriEnum : uris) {
            mUriMatcher.addURI(authority, uriEnum.path, uriEnum.code);
        }
        buildEnumsMap();
    }

    private void buildEnumsMap() {
        ScheduleUriEnum[] uris = ScheduleUriEnum.values();
        for (ScheduleUriEnum uriEnum : uris) {
            mEnumsMap.put(uriEnum.code, uriEnum);
        }
    }
}
