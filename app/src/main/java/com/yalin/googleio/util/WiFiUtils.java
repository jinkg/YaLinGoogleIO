package com.yalin.googleio.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.yalin.googleio.BuildConfig;
import com.yalin.googleio.Config;

import java.util.List;

import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/5/4.
 */
public class WiFiUtils {
    private static final String TAG = makeLogTag(WiFiUtils.class);

    public static boolean shouldOfferToSetupWifi(final Context context, boolean actively) {
        long now = UIUtils.getCurrentTime(context);
        if (now < Config.WIFI_SETUP_OFFER_START) {
            LOGW(TAG, "Too early to offer wifi");
            return false;
        }
        if (now > Config.CONFERENCE_END_MILLIS) {
            LOGW(TAG, "Too late to offer wifi");
            return false;
        }
        if (!isWiFiEnabled(context)) {
            LOGW(TAG, "Wifi isn't enabled");
            return false;
        }
        if (!SettingsUtils.isAttendeeAtVenue(context)) {
            LOGW(TAG, "Attendee isn't on site so wifi wouldn't matter");
            return false;
        }

        if (isWiFiApConfigured(context)) {
            LOGW(TAG, "Attendee is already setup for wifi.");
            return false;
        }
        if (actively && SettingsUtils.hasDeclinedWifiSetup(context)) {
            LOGW(TAG, "Attendee opted out of wifi.");
            return false;
        }
        return true;

    }

    public static boolean isWiFiEnabled(final Context context) {
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public static boolean isWiFiApConfigured(final Context context) {
        final WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs == null) return false;

        final String conferenceSSID = getConferenceWifiConfig().SSID;
        for (WifiConfiguration config : configs) {
            if (conferenceSSID.equalsIgnoreCase(config.SSID)) {
                return true;
            }
        }
        return false;
    }

    private static WifiConfiguration getConferenceWifiConfig() {
        WifiConfiguration conferenceConfig = new WifiConfiguration();

        conferenceConfig.SSID = String.format("\"%s\"", BuildConfig.WIFI_SSID);
        conferenceConfig.preSharedKey = String.format("\"%s\"", BuildConfig.WIFI_PASSPHRASE);

        return conferenceConfig;
    }
}
