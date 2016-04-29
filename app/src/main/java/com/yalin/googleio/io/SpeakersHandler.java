package com.yalin.googleio.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.android.gms.playlog.internal.LogEvent;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.googleio.io.model.Speaker;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.provider.ScheduleContractHelper;
import com.yalin.googleio.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class SpeakersHandler extends JSONHandler {
    private static final String TAG = makeLogTag(SpeakersHandler.class);
    private HashMap<String, Speaker> mSpeakers = new HashMap<>();

    public SpeakersHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Speakers.CONTENT_URI);
        HashMap<String, String> speakerHashcodes = loadSpeakerHashcodes();
        HashSet<String> speakersToKeep = new HashSet<>();
        boolean isIncrementalUpdate = speakerHashcodes != null && speakerHashcodes.size() > 0;

        if (isIncrementalUpdate) {
            LOGD(TAG, "Doing incremental update for speakers.");
        } else {
            LOGD(TAG, "Doing FULL (non incremental) update for speakers.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updateSpeakers = 0;
        for (Speaker speaker : mSpeakers.values()) {
            String hashCode = speaker.getImportHashcode();
            speakersToKeep.add(speaker.id);

            if (!isIncrementalUpdate || !speakerHashcodes.containsKey(speaker.id) ||
                    !speakerHashcodes.get(speaker.id).equals(hashCode)) {
                ++updateSpeakers;
                boolean isNew = !isIncrementalUpdate || !speakerHashcodes.containsKey(speaker.id);
                buildSpeaker(isNew, speaker, list);
            }
        }

        int deletedSpeakers = 0;
        if (isIncrementalUpdate) {
            for (String speakerId : speakerHashcodes.keySet()) {
                if (!speakersToKeep.contains(speakerId)) {
                    buildDeleteOperation(speakerId, list);
                    ++deletedSpeakers;
                }
            }
        }

        LOGD(TAG, "Speakers: " + (isIncrementalUpdate ? "INCREMENTAL" : "FULL") + " update." +
                updateSpeakers + " to update, " + deletedSpeakers + " to delete. New total: " +
                mSpeakers.size());
    }

    @Override
    public void process(JsonElement element) {
        for (Speaker speaker : new Gson().fromJson(element, Speaker[].class)) {
            mSpeakers.put(speaker.id, speaker);
        }
    }

    public HashMap<String, Speaker> getSpeakerMap() {
        return mSpeakers;
    }

    private HashMap<String, String> loadSpeakerHashcodes() {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Speakers.CONTENT_URI);

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, SpeakerHashcodeQuery.PROJECTION,
                    null, null, null);
            if (cursor == null) {
                LOGE(TAG, "Error querying speaker hashcodes (got null cursor)");
                return null;
            }
            if (cursor.getCount() < 1) {
                LOGE(TAG, "Error querying speaker hashcodes (no records returned)");
                return null;
            }
            HashMap<String, String> result = new HashMap<>();
            if (cursor.moveToFirst()) {
                do {
                    String speakerId = cursor.getString(SpeakerHashcodeQuery.SPEAKER_ID);
                    String hashcode = cursor.getString(SpeakerHashcodeQuery.SPEAKER_IMPORT_HASHCODE);
                    result.put(speakerId, hashcode == null ? "" : hashcode);
                } while (cursor.moveToNext());
            }
            return result;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void buildSpeaker(boolean isInsert, Speaker speaker,
                              ArrayList<ContentProviderOperation> list) {
        Uri allSpeakersUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Speakers.CONTENT_URI);
        Uri thisSpeakersUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Speakers.buildSpeakerUri(speaker.id));

        ContentProviderOperation.Builder builder;
        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allSpeakersUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisSpeakersUri);
        }

        list.add(builder.withValue(ScheduleContract.SyncColumns.UPDATED, System.currentTimeMillis())
                .withValue(ScheduleContract.Speakers.SPEAKER_ID, speaker.id)
                .withValue(ScheduleContract.Speakers.SPEAKER_NAME, speaker.name)
                .withValue(ScheduleContract.Speakers.SPEAKER_ABSTRACT, speaker.bio)
                .withValue(ScheduleContract.Speakers.SPEAKER_COMPANY, speaker.company)
                .withValue(ScheduleContract.Speakers.SPEAKER_IMAGE_URL, speaker.thumbnailUrl)
                .withValue(ScheduleContract.Speakers.SPEAKER_PLUSONE_URL, speaker.plusoneUrl)
                .withValue(ScheduleContract.Speakers.SPEAKER_TWITTER_URL, speaker.twitterUrl)
                .withValue(ScheduleContract.Speakers.SPEAKER_IMPORT_HASHCODE, speaker.getImportHashcode())
                .build());
    }

    private void buildDeleteOperation(String speakerId, ArrayList<ContentProviderOperation> list) {
        Uri speakerUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Speakers.buildSpeakerUri(speakerId));

        list.add(ContentProviderOperation.newDelete(speakerUri).build());
    }

    private interface SpeakerHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                ScheduleContract.Speakers.SPEAKER_ID,
                ScheduleContract.Speakers.SPEAKER_IMPORT_HASHCODE
        };

        int _ID = 0;
        int SPEAKER_ID = 1;
        int SPEAKER_IMPORT_HASHCODE = 2;
    }
}
