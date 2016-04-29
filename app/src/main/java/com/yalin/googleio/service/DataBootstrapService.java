package com.yalin.googleio.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yalin.googleio.BuildConfig;
import com.yalin.googleio.R;
import com.yalin.googleio.io.JSONHandler;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.sync.ConferenceDataHandler;
import com.yalin.googleio.util.SettingsUtils;

import java.io.IOException;

import static com.yalin.googleio.util.LogUtils.*;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class DataBootstrapService extends IntentService {
    private static final String TAG = makeLogTag(DataBootstrapService.class);

    public DataBootstrapService() {
        super(TAG);
    }

    public static void startDataBootstrapIfNecessary(Context context) {
        if (!SettingsUtils.isDataBootstrapDone(context)) {
            LOGW(TAG, "One-time data bootstrap not done yet, Doing now.");
            context.startService(new Intent(context, DataBootstrapService.class));
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context appContext = getApplicationContext();
        if (SettingsUtils.isDataBootstrapDone(appContext)) {
            LOGD(TAG, "Data bootstrap already done.");
            return;
        }
        LOGD(TAG, "Starting data bootstrap process.");
        try {
            String bootstrapJson = JSONHandler
                    .parseResource(appContext, R.raw.bootstrap_data);

            ConferenceDataHandler dataHandler = new ConferenceDataHandler(appContext);
            dataHandler.applyConferenceData(new String[]{bootstrapJson},
                    BuildConfig.BOOTSTRAP_DATA_TIMESTAMP, false);

            SettingsUtils.markDataBootstrapDone(appContext);

            getContentResolver().notifyChange(Uri.parse(ScheduleContract.CONTENT_AUTHORITY),
                    null, false);

        } catch (IOException e) {
            LOGE(TAG, "*** ERROR DURING BOOTSTRAP! Problem in bootstrap data?", e);
            LOGE(TAG, "Applying fallback -- marking bootstrap as done; sync might fix problem.");
            SettingsUtils.markDataBootstrapDone(appContext);
        } finally {

        }
    }
}
