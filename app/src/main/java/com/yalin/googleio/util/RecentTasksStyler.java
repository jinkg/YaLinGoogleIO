package com.yalin.googleio.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.yalin.googleio.R;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class RecentTasksStyler {
    private static Bitmap sIcon = null;

    private RecentTasksStyler() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void styleRecentTasksEntry(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        Resources resources = activity.getResources();
        String label = resources.getString(activity.getApplicationInfo().labelRes);
        int colorPrimary = resources.getColor(R.color.colorPrimary);
        if (sIcon == null) {
            sIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_stat_notification);
        }
        activity.setTaskDescription(new ActivityManager.TaskDescription(label, sIcon, colorPrimary));
    }
}
