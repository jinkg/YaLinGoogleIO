package com.yalin.googleio.explore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.yalin.googleio.ui.BaseActivity;

/**
 * 作者：YaLin
 * 日期：2016/5/5.
 */
public class ExploreSessionsActivity extends BaseActivity {
    private static final String EXTRA_SHOW_LIVE_STREAM_SESSIONS =
            "com.yalin.googleio.explore.EXTRA_SHOW_LIVE_STREAM_SESSIONS";

    private static final String EXTRA_FILTER_TAG =
            "com.yalin.googleio.explore.EXTRA_FILTER_TAG";

    public static void openLiveStream(Activity activity, Uri sessionUri) {
        Intent intent = new Intent(activity, ExploreSessionsActivity.class);
        intent.setData(sessionUri);
        intent.putExtra(EXTRA_SHOW_LIVE_STREAM_SESSIONS, true);
        activity.startActivity(intent);
    }

    public static void openItemGroup(Activity activity, String groupId) {
        Intent intent = new Intent(activity, ExploreSessionsActivity.class);
        intent.putExtra(EXTRA_FILTER_TAG, groupId);
        activity.startActivity(intent);
    }
}
