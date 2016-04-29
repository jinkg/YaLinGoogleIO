package com.yalin.googleio.provider;

import android.net.Uri;
import android.text.TextUtils;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class ScheduleContractHelper {

    private static final String QUERY_PARAMETER_DISTINCT = "distinct";

    private static final String QUERY_PARAMETER_OVERRIDE_ACCOUNT_NAME = "overrideAccountName";

    private static final String QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER = "callerIsSyncAdapter";

    public static boolean isUriCalledFromSyncAdapter(Uri uri) {
        return uri.getBooleanQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, false);
    }

    public static boolean isQueryDistinct(Uri uri) {
        return !TextUtils.isEmpty(uri.getQueryParameter(QUERY_PARAMETER_DISTINCT));
    }

    public static Uri setUriAsCalledFromSyncAdapter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(QUERY_PARAMETER_CALLER_IS_SYNC_ADAPTER, "true")
                .build();
    }

    public static String getOverrideAccountName(Uri uri) {
        return uri.getQueryParameter(QUERY_PARAMETER_OVERRIDE_ACCOUNT_NAME);
    }
}
