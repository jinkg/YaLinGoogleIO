package com.yalin.googleio.welcome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.yalin.googleio.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/15.
 */
public class WelcomeActivity extends AppCompatActivity implements WelcomeFragment.WelcomeFragmentContainer {
    private static final String TAG = makeLogTag(WelcomeActivity.class);

    public static Intent getOpenIntent(Activity activity) {
        return new Intent(activity, WelcomeActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    private static WelcomeActivityContent getCurrentFragment(Context context) {
        List<WelcomeActivityContent> welcomeActivityContents = getWelcomeFragments();

        for (WelcomeActivityContent fragment : welcomeActivityContents) {
            if (fragment.shouldDisplay(context)) {
                return fragment;
            }
        }
        return null;
    }

    public static boolean shouldDisplay(Context context) {
        WelcomeActivityContent fragment = getCurrentFragment(context);
        return fragment != null;
    }

    private static List<WelcomeActivityContent> getWelcomeFragments() {
        return new ArrayList<WelcomeActivityContent>(Arrays.asList(new AccountFragment()));
    }

    @Override
    public Button getPositiveButton() {
        return (Button) findViewById(R.id.button_accept);
    }

    @Override
    public void setPositiveButtonEnabled(boolean enabled) {
        try {
            getPositiveButton().setEnabled(enabled);
        } catch (NullPointerException e) {
            LOGD(TAG, "Positive welcome button doesn't exist to set enabled.");
        }
    }

    @Override
    public Button getNegativeButton() {
        return (Button) findViewById(R.id.button_decline);
    }

    @Override
    public void setNegativeButtonEnabled(boolean enabled) {
        try {
            getNegativeButton().setEnabled(enabled);
        } catch (NullPointerException e) {
            LOGD(TAG, "Negative welcome button doesn't exist to set enabled.");
        }
    }

    interface WelcomeActivityContent {
        boolean shouldDisplay(Context context);
    }
}
