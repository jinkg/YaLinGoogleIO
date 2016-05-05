package com.yalin.googleio.session;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.yalin.googleio.ui.BaseActivity;

/**
 * 作者：YaLin
 * 日期：2016/5/5.
 */
public class SessionDetailActivity extends BaseActivity {
    public static void open(Activity activity, Uri sessionUri) {
        Intent intent = new Intent(activity, SessionDetailActivity.class);
        intent.setData(sessionUri);
        activity.startActivity(intent);
    }
}
