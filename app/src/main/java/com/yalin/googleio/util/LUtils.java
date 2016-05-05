package com.yalin.googleio.util;

import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class LUtils {
    protected AppCompatActivity mActivity;

    public static LUtils getInstance(AppCompatActivity activity) {
        return new LUtils(activity);
    }

    private static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private LUtils(AppCompatActivity activity) {
        mActivity = activity;
    }

    public int getStatusBarColor() {
        if (!hasL()) {
            return Color.BLACK;
        }
        return mActivity.getWindow().getStatusBarColor();
    }

    public void setStartusBarColor(int color) {
        if (!hasL()) {
            return;
        }
        mActivity.getWindow().setStatusBarColor(color);
    }
}
