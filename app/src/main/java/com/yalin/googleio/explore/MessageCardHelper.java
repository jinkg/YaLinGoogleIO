package com.yalin.googleio.explore;

import android.content.Context;
import android.view.View;

import com.yalin.googleio.R;
import com.yalin.googleio.explore.data.MessageData;
import com.yalin.googleio.settings.ConfMessageCardUtils;
import com.yalin.googleio.util.SettingsUtils;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/5/4.
 */
public class MessageCardHelper {
    private static final String TAG = makeLogTag(MessageCardHelper.class);

    public static MessageData getConferenceOptInMessageData(final Context context) {
        MessageData messageData = new MessageData();
        messageData.setStartButtonStringResourceId(R.string.explore_io_msg_cards_answer_no);
        messageData.setMessageStringResourceId(R.string.explore_io_msg_cards_ask_opt_in);
        messageData.setEndButtonStringResourceId(R.string.explore_io_msg_cards_answer_yes);

        messageData.setStartButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Marking conference messages question answered with decline.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, true);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, false);
            }
        });

        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Marking conference message question answered with affirmation.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(context, true);
                ConfMessageCardUtils.setConfMessageCardsEnabled(context, true);
            }
        });
        return messageData;
    }

    public static MessageData getWifiSetupMessageData(final Context context) {
        MessageData messageData = new MessageData();
        messageData.setStartButtonStringResourceId(R.string.explore_io_msg_cards_answer_no);
        messageData.setMessageStringResourceId(R.string.explore_io_msg_cards_ask_opt_in);
        messageData.setEndButtonStringResourceId(R.string.explore_io_msg_cards_answer_yes);
        messageData.setIconDrawableId(R.drawable.message_card_wifi);

        messageData.setStartButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Marking wifi setup declined.");
                SettingsUtils.markDeclinedWifiSetup(context, false);
                SettingsUtils.markDeclinedWifiSetup(context, true);
            }
        });

        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LOGD(TAG, "Installing conference wifi.");
                SettingsUtils.markDeclinedWifiSetup(context, true);
                SettingsUtils.markDeclinedWifiSetup(context, false);
            }
        });
        return messageData;
    }
}
