package com.yalin.googleio.explore;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yalin.googleio.Config;
import com.yalin.googleio.R;
import com.yalin.googleio.explore.data.ItemGroup;
import com.yalin.googleio.explore.data.LiveStreamData;
import com.yalin.googleio.explore.data.SessionData;
import com.yalin.googleio.explore.data.ThemeGroup;
import com.yalin.googleio.explore.data.TopicGroup;
import com.yalin.googleio.framework.Model;
import com.yalin.googleio.framework.QueryEnum;
import com.yalin.googleio.framework.UserActionEnum;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.provider.ScheduleContract.Sessions;
import com.yalin.googleio.ui.widget.CollectionView;
import com.yalin.googleio.util.SettingsUtils;
import com.yalin.googleio.util.TimeUtils;
import com.yalin.googleio.util.UIUtils;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ExploreModel implements Model {

    private static final String TAG = makeLogTag(ExploreModel.class);

    private Context mContext;

    private SessionData mKeynoteData;

    private LiveStreamData mLiveStreamData;

    private Map<String, TopicGroup> mTopics = new HashMap<>();

    private Map<String, ThemeGroup> mThemes = new HashMap<>();

    private Map<String, String> mTagTitles = new HashMap<>();

    public ExploreModel(Context context) {
        mContext = context;
    }

    public SessionData getKeynoteData() {
        return mKeynoteData;
    }

    public LiveStreamData getLiveStreamData() {
        return mLiveStreamData;
    }

    public Collection<TopicGroup> getTopics() {
        return mTopics.values();
    }

    public Collection<ThemeGroup> getThemes() {
        return mThemes.values();
    }

    public Map<String, String> getTagTitles() {
        return mTagTitles;
    }

    @Override
    public QueryEnum[] getQueries() {
        return ExploreQueryEnum.values();
    }

    @Override
    public boolean readDataFromCursor(Cursor cursor, QueryEnum query) {
        LOGD(TAG, "readDataFromCursor.");
        if (query == ExploreQueryEnum.SESSIONS) {
            LOGD(TAG, "Reading session data from cursor.");

            boolean atVenue = SettingsUtils.isAttendeeAtVenue(mContext);
            int themeSessionLimit = getThemeSessionLimit(mContext);
            int topicSessionLimit = getTopicSessionLimit(mContext);

            LiveStreamData liveStreamData = new LiveStreamData();
            Map<String, TopicGroup> topicGroups = new HashMap<>();
            Map<String, ThemeGroup> themeGroups = new HashMap<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    SessionData session = new SessionData();
                    populateSessionFromCursorRow(session, cursor);

                    if (TextUtils.isEmpty(session.sessionName) ||
                            TextUtils.isEmpty(session.details) ||
                            TextUtils.isEmpty(session.sessionId) ||
                            TextUtils.isEmpty(session.imageUrl)) {
                        continue;
                    }

                    if (!atVenue &&
                            (!session.isLiveStreamAvailable()) && !session.isVideoAvailable()) {
                        continue;
                    }
                    String tags = session.tags;
                    if (Config.Tags.SPECIAL_KEYNOTE.equals(session.mainTag)) {
                        SessionData keynoteData = new SessionData();
                        populateSessionFromCursorRow(keynoteData, cursor);
                        rewriteKeynoteDetails(keynoteData);
                        mKeynoteData = keynoteData;
                    } else if (session.isLiveStreamNow(mContext)) {
                        liveStreamData.addSessionData(session);
                    }

                    if (!TextUtils.isEmpty(tags)) {
                        StringTokenizer tagsTokenizer = new StringTokenizer(tags, ",");
                        while (tagsTokenizer.hasMoreTokens()) {
                            String rawTag = tagsTokenizer.nextToken();
                            if (rawTag.startsWith("TOPIC_")) {
                                TopicGroup topicGroup = topicGroups.get(rawTag);
                                if (topicGroup == null) {
                                    topicGroup = new TopicGroup();
                                    topicGroup.setTitle(rawTag);
                                    topicGroup.setId(rawTag);
                                    topicGroups.put(rawTag, topicGroup);
                                }
                                topicGroup.addSessionData(session);
                            } else if (rawTag.startsWith("THEME_")) {
                                ThemeGroup themeGroup = themeGroups.get(rawTag);
                                if (themeGroup == null) {
                                    themeGroup = new ThemeGroup();
                                    themeGroup.setTitle(rawTag);
                                    themeGroup.setId(rawTag);
                                    themeGroups.put(rawTag, themeGroup);
                                }
                                themeGroup.addSessionData(session);
                            }
                        }
                    }
                } while (cursor.moveToNext());
            }

            for (ItemGroup group : themeGroups.values()) {
                group.trimSessionData(themeSessionLimit);
            }
            for (ItemGroup group : topicGroups.values()) {
                group.trimSessionData(topicSessionLimit);
            }
            if (liveStreamData.getSessions().size() > 0) {
                mLiveStreamData = liveStreamData;
            }

            mThemes = themeGroups;
            mTopics = topicGroups;
            return true;
        } else if (query == ExploreQueryEnum.TAGS) {
            LOGW(TAG, "TAGS query loaded.");
            return true;
        }
        return false;
    }

    @Override
    public Loader<Cursor> createCursorLoader(int loaderId, Uri uri, Bundle args) {
        CursorLoader loader = null;
        if (loaderId == ExploreQueryEnum.SESSIONS.getId()) {
            loader = getCursorLoaderInstance(mContext, uri, ExploreQueryEnum.SESSIONS.getProjection(), null, null,
                    Sessions.SORT_BY_TYPE_THEN_TIME);
        } else if (loaderId == ExploreQueryEnum.TAGS.getId()) {
            LOGW(TAG, "Starting sessions tag query.");
            loader = new CursorLoader(mContext, ScheduleContract.Tags.CONTENT_URI,
                    ExploreQueryEnum.TAGS.getProjection(), null, null, null);
        } else {
            LOGE(TAG, "Invalid query loaderId: " + loaderId);
        }
        return loader;
    }

    @Override
    public boolean requestModelUpdate(UserActionEnum action, @Nullable Bundle args) {
        return true;
    }

    public CursorLoader getCursorLoaderInstance(Context context, Uri uri, String[] projection,
                                                String selection, String[] selectionArgs, String sortOrder) {
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static int getTopicSessionLimit(Context context) {
        boolean atVenue = SettingsUtils.isAttendeeAtVenue(context);
        int topicSessionLimit;
        if (atVenue) {
            topicSessionLimit = context.getResources().getInteger(R.integer.explore_topic_theme_onsite_max_item_count);
        } else {
            topicSessionLimit = 0;
        }
        return topicSessionLimit;
    }

    public static int getThemeSessionLimit(Context context) {
        boolean atVenue = SettingsUtils.isAttendeeAtVenue(context);
        int themeSessionLimit;
        if (atVenue) {
            themeSessionLimit = context.getResources().getInteger(R.integer.explore_topic_theme_onsite_max_item_count);
        } else {
            themeSessionLimit = context.getResources().getInteger(R.integer.explore_theme_max_item_count_offsite);
        }
        return themeSessionLimit;
    }

    private void rewriteKeynoteDetails(SessionData keynoteData) {
        long startTime, endTime, currentTime;
        currentTime = UIUtils.getCurrentTime(mContext);
        if (keynoteData.startDate != null) {
            startTime = keynoteData.startDate.getTime();
        } else {
            LOGD(TAG, "Keynote start time wasn't set.");
            startTime = 0;
        }
        if (keynoteData.endDate != null) {
            endTime = keynoteData.endDate.getTime();
        } else {
            LOGD(TAG, "Keynote end time wasn't set.");
            endTime = Long.MAX_VALUE;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (currentTime >= startTime && currentTime < endTime) {
            stringBuilder.append(mContext.getString(R.string.live_now));
        } else {
            String shortDate = TimeUtils.formatShortDate(mContext, keynoteData.startDate);
            stringBuilder.append(shortDate);

            if (startTime > 0) {
                stringBuilder.append(" / ");
                stringBuilder.append(TimeUtils.formatShortTime(mContext,
                        new Date(startTime)));
            }
        }
        keynoteData.details = stringBuilder.toString();
    }

    private void populateSessionFromCursorRow(SessionData session, Cursor cursor) {
        session.update(cursor.getString(cursor.getColumnIndex(
                Sessions.SESSION_TITLE)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_ABSTRACT)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_ID)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_PHOTO_URL)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_MAIN_TAG)),
                cursor.getLong(cursor.getColumnIndex(
                        Sessions.SESSION_START)),
                cursor.getLong(cursor.getColumnIndex(
                        Sessions.SESSION_END)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_LIVESTREAM_URL)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_YOUTUBE_URL)),
                cursor.getString(cursor.getColumnIndex(
                        Sessions.SESSION_TAGS)),
                cursor.getLong(cursor.getColumnIndex(
                        Sessions.SESSION_IN_MY_SCHEDULE)) == 1L);
    }

    public enum ExploreQueryEnum implements QueryEnum {
        SESSIONS(0x1, new String[]{
                Sessions.SESSION_ID,
                Sessions.SESSION_TITLE,
                Sessions.SESSION_ABSTRACT,
                Sessions.SESSION_TAGS,
                Sessions.SESSION_MAIN_TAG,
                Sessions.SESSION_PHOTO_URL,
                Sessions.SESSION_START,
                Sessions.SESSION_END,
                Sessions.SESSION_LIVESTREAM_URL,
                Sessions.SESSION_YOUTUBE_URL,
                Sessions.SESSION_IN_MY_SCHEDULE,
                Sessions.SESSION_START,
        }),
        TAGS(0x2, new String[]{
                ScheduleContract.Tags.TAG_ID,
                ScheduleContract.Tags.TAG_NAME,
        });

        private int id;

        private String[] projection;

        ExploreQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }
    }

    public enum ExploreUserActionEnum implements UserActionEnum {
        RELOAD(2);

        private int id;

        ExploreUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
