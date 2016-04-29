package com.yalin.googleio.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.googleio.Config;
import com.yalin.googleio.R;
import com.yalin.googleio.io.model.Session;
import com.yalin.googleio.io.model.Speaker;
import com.yalin.googleio.io.model.Tag;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.provider.ScheduleContractHelper;
import com.yalin.googleio.provider.ScheduleDatabase;
import com.yalin.googleio.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class SessionsHandler extends JSONHandler {
    private static final String TAG = makeLogTag(SessionsHandler.class);
    private HashMap<String, Session> mSessions = new HashMap<>();
    private HashMap<String, Tag> mTagMap = null;
    private HashMap<String, Speaker> mSpeakerMap = null;

    private int mDefaultSessionColor;

    public SessionsHandler(Context context) {
        super(context);
        mDefaultSessionColor = mContext.getResources().getColor(R.color.default_session_color);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.CONTENT_URI);

        HashMap<String, String> sessionHashCodes = loadSessionHashCodes();

        boolean incrementalUpdate = (sessionHashCodes != null) && (sessionHashCodes.size() > 0);

        HashSet<String> sessionsToKeep = new HashSet<>();

        if (incrementalUpdate) {
            LOGD(TAG, "Doing incremental update for sessions.");
        } else {
            LOGD(TAG, "Doing full (non-incremental) update for sessions.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedSessions = 0;
        for (Session session : mSessions.values()) {
            session.groupingOrder = computeTypeOrder(session);
            String hashCode = session.getImportHashCode();
            sessionsToKeep.add(session.id);

            if (!incrementalUpdate || !sessionHashCodes.containsKey(session.id) ||
                    !sessionHashCodes.get(session.id).equals(hashCode)) {
                ++updatedSessions;
                boolean isNew = !incrementalUpdate || !sessionHashCodes.containsKey(session.id);
                buildSession(isNew, session, list);

                buildSessionSpeakerMapping(session, list);
                buildTagsMapping(session, list);
            }
        }

        int deletedSession = 0;
        if (incrementalUpdate) {
            for (String sessionId : sessionHashCodes.keySet()) {
                if (!sessionsToKeep.contains(sessionId)) {
                    buildDeleteOperation(sessionId, list);
                    ++deletedSession;
                }
            }
        }

        LOGD(TAG, "Sessions: " + (incrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedSessions + " to update, " + deletedSession + " to delete. New total: " +
                mSessions.size());
    }

    @Override
    public void process(JsonElement element) {
        for (Session session : new Gson().fromJson(element, Session[].class)) {
            mSessions.put(session.id, session);
        }
    }

    private HashMap<String, String> loadSessionHashCodes() {
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(ScheduleContract.Sessions.CONTENT_URI);
        LOGD(TAG, "Loading session hashcodes for session import optimization.");
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, SessionHashcodeQuery.PROJECTION,
                    null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                LOGW(TAG, "Warning: failed to load session hashcodes. Not optimizing session import.");
                return null;
            }
            HashMap<String, String> hashcodeMap = new HashMap<>();
            if (cursor.moveToFirst()) {
                do {
                    String sessionId = cursor.getString(SessionHashcodeQuery.SESSION_ID);
                    String hashcode = cursor.getString(SessionHashcodeQuery.SESSION_IMPORT_HASHCODE);
                    hashcodeMap.put(sessionId, hashcode == null ? "" : hashcode);
                } while (cursor.moveToNext());
            }
            LOGD(TAG, "Session hashcodes loaded for " + hashcodeMap.size() + " sessions.");
            return hashcodeMap;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int computeTypeOrder(Session session) {
        int order = Integer.MAX_VALUE;
        int keynoteOrder = -1;
        if (mTagMap == null) {
            throw new IllegalStateException("Attempt to compute type order without tag map.");
        }
        for (String tagId : session.tags) {
            if (Config.Tags.SPECIAL_KEYNOTE.equals(tagId)) {
                return keynoteOrder;
            }
            Tag tag = mTagMap.get(tagId);
            if (tag != null && Config.Tags.SESSION_GROUPING_TAG_CATEGORY.equals(tag.category)) {
                if (tag.order_in_category < order) {
                    order = tag.order_in_category;
                }
            }
        }
        return order;
    }

    private void buildDeleteOperation(String sessionId, ArrayList<ContentProviderOperation> list) {
        Uri sessionUri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildSessionUri(sessionId));
        list.add(ContentProviderOperation.newDelete(sessionUri).build());
    }

    StringBuilder mStringBuilder = new StringBuilder();

    private void buildSession(boolean isInsert, Session session, ArrayList<ContentProviderOperation> list) {
        ContentProviderOperation.Builder builder;
        Uri allSessionsUri = ScheduleContractHelper
                .setUriAsCalledFromSyncAdapter(ScheduleContract.Sessions.CONTENT_URI);
        Uri thisSessionUri = ScheduleContractHelper
                .setUriAsCalledFromSyncAdapter(ScheduleContract.Sessions.buildSessionUri(
                        session.id));

        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allSessionsUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisSessionUri);
        }

        String speakerNames = "";
        if (mSpeakerMap != null) {
            mStringBuilder.setLength(0);
            for (int i = 0; i < session.speakers.length; ++i) {
                if (mSpeakerMap.containsKey(session.speakers[i])) {
                    mStringBuilder.append(i == 0 ? "" : i == session.speakers.length - 1 ? " and " : ", ")
                            .append(mSpeakerMap.get(session.speakers[i]).name.trim());
                } else {
                    LOGW(TAG, "Unknown speaker ID " + session.speakers[i] + " in session " + session.id);
                }
            }
            speakerNames = mStringBuilder.toString();
        } else {
            LOGE(TAG, "Can't build speaker name -- speaker map is null.");
        }

        int color = mDefaultSessionColor;
        try {
            if (!TextUtils.isEmpty(session.color)) {
                color = Color.parseColor(session.color);
            }
        } catch (IllegalArgumentException e) {
            LOGD(TAG, "Ignoring invalid formatted session color: " + session.color);
        }

        builder.withValue(ScheduleContract.SyncColumns.UPDATED, System.currentTimeMillis())
                .withValue(ScheduleContract.Sessions.SESSION_ID, session.id)
                .withValue(ScheduleContract.Sessions.SESSION_LEVEL, null)
                .withValue(ScheduleContract.Sessions.SESSION_TITLE, session.title)
                .withValue(ScheduleContract.Sessions.SESSION_ABSTRACT, session.description)
                .withValue(ScheduleContract.Sessions.SESSION_HASHTAG, session.hashtag)
                .withValue(ScheduleContract.Sessions.SESSION_START, TimeUtils.timestampToMillis(session.startTimestamp, 0))
                .withValue(ScheduleContract.Sessions.SESSION_END, TimeUtils.timestampToMillis(session.entTimestamp, 0))
                .withValue(ScheduleContract.Sessions.SESSION_TAGS, session.makeTagsList())
                .withValue(ScheduleContract.Sessions.SESSION_SPEAKER_NAMES, speakerNames)
                .withValue(ScheduleContract.Sessions.SESSION_KEYWORDS, null)
                .withValue(ScheduleContract.Sessions.SESSION_URL, session.url)
                .withValue(ScheduleContract.Sessions.SESSION_LIVESTREAM_URL, session.isLivestream ? session.youtubeUrl : null)
                .withValue(ScheduleContract.Sessions.SESSION_MODERATOR_URL, null)
                .withValue(ScheduleContract.Sessions.SESSION_REQUIREMENTS, null)
                .withValue(ScheduleContract.Sessions.SESSION_YOUTUBE_URL, session.isLivestream ? null : session.youtubeUrl)
                .withValue(ScheduleContract.Sessions.SESSION_PDF_URL, null)
                .withValue(ScheduleContract.Sessions.SESSION_NOTES_URL, null)
                .withValue(ScheduleContract.Sessions.ROOM_ID, session.room)
                .withValue(ScheduleContract.Sessions.SESSION_GROUPING_ORDER, session.groupingOrder)
                .withValue(ScheduleContract.Sessions.SESSION_IMPORT_HASHCODE, session.getImportHashCode())
                .withValue(ScheduleContract.Sessions.SESSION_MAIN_TAG, session.mainTag)
                .withValue(ScheduleContract.Sessions.SESSION_CAPTIONS_URL, session.captionsUrl)
                .withValue(ScheduleContract.Sessions.SESSION_PHOTO_URL, session.photoUrl)
                .withValue(ScheduleContract.Sessions.SESSION_COLOR, color);
        list.add(builder.build());
    }

    private void buildSessionSpeakerMapping(Session session, ArrayList<ContentProviderOperation> list) {
        final Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildSpeakersDirUri(session.id));

        list.add(ContentProviderOperation.newDelete(uri).build());
        if (session.speakers != null) {
            for (String speakerId : session.speakers) {
                list.add(ContentProviderOperation.newInsert(uri)
                        .withValue(ScheduleDatabase.SessionsSpeakers.SESSION_ID, session.id)
                        .withValue(ScheduleDatabase.SessionsSpeakers.SPEAKER_ID, speakerId)
                        .build());
            }
        }
    }

    private void buildTagsMapping(Session session, ArrayList<ContentProviderOperation> list) {
        final Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Sessions.buildTagsDirUri(session.id));
        list.add(ContentProviderOperation.newDelete(uri).build());

        for (String tag : session.tags) {
            list.add(ContentProviderOperation.newInsert(uri)
                    .withValue(ScheduleDatabase.SessionsTags.SESSION_ID, session.id)
                    .withValue(ScheduleDatabase.SessionsTags.TAG_ID, tag).build());
        }

    }

    public void setTagMap(HashMap<String, Tag> tagMap) {
        mTagMap = tagMap;
    }

    public void setSpeakerMap(HashMap<String, Speaker> speakerMap) {
        mSpeakerMap = speakerMap;
    }

    private interface SessionHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                ScheduleContract.Sessions.SESSION_ID,
                ScheduleContract.Sessions.SESSION_IMPORT_HASHCODE
        };
        int _ID = 0;
        int SESSION_ID = 1;
        int SESSION_IMPORT_HASHCODE = 2;
    }
}
