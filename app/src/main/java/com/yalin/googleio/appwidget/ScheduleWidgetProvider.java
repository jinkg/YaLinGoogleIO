package com.yalin.googleio.appwidget;

import android.accounts.Account;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.yalin.googleio.R;
import com.yalin.googleio.provider.ScheduleProvider;
import com.yalin.googleio.sync.SyncHelper;
import com.yalin.googleio.util.AccountUtils;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class ScheduleWidgetProvider extends AppWidgetProvider {
    private static final String TAG = makeLogTag(ScheduleWidgetProvider.class);

    private static final String REFRESH_ACTION =
            "com.yalin.googleio.appwidget.action.REFRESH";

    private static final String EXTRA_PERFORM_SYNC =
            "com.yalin.googleio.appwidget.extra.PERFORM_SYNC";

    public static Intent getRefreshBroadcastIntent(Context context, boolean performSync) {
        return new Intent(REFRESH_ACTION)
                .setComponent(new ComponentName(context, ScheduleProvider.class))
                .putExtra(EXTRA_PERFORM_SYNC, performSync);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (REFRESH_ACTION.equals(action)) {
            LOGD(TAG, "received REFRESH_ACTION_from widget.");
            final boolean shouldSync = intent.getBooleanExtra(EXTRA_PERFORM_SYNC, false);

            Account chosenAccount = AccountUtils.getActiveAccount(context);
            if (shouldSync && chosenAccount != null) {
                SyncHelper.requestManualSync(chosenAccount);
            }

            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, ScheduleWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn),
                    R.id.widget_schedule_list);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        LOGD(TAG, "updating app widget.");
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
