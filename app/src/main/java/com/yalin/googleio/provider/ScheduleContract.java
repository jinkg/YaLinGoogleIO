package com.yalin.googleio.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public class ScheduleContract {
    public static final String CONTENT_TYPE_APP_BASE = "yalinIO2015.";

    public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd."
            + CONTENT_TYPE_APP_BASE;

    public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd."
            + CONTENT_TYPE_APP_BASE;

    public interface SyncColumns {
        String UPDATED = "updated";
    }

    interface BlocksColumns {
        String BLOCK_ID = "block_id";
        String BLOCK_TITLE = "block_title";
        String BLOCK_START = "block_starts";
        String BLOCK_END = "block_end";
        String BLOCK_TYPE = "block_type";
        String BLOCK_SUBTITLE = "block_subtile";
    }

    interface SessionsColumns {
        String SESSION_ID = "session_id";
        String SESSION_LEVEL = "session_level";
        String SESSION_START = "session_start";
        String SESSION_END = "session_end";
        String SESSION_TITLE = "session_title";
        String SESSION_ABSTRACT = "session_abstract";
        String SESSION_REQUIREMENTS = "session_requirements";
        String SESSION_KEYWORDS = "session_keywords";
        String SESSION_HASHTAG = "session_hashtag";
        String SESSION_URL = "session_url";
        String SESSION_YOUTUBE_URL = "session_youtube_url";
        String SESSION_PDF_URL = "session_pdf_url";
        String SESSION_NOTES_URL = "session_notes_url";
        String SESSION_IN_MY_SCHEDULE = "session_in_my_schedule";
        String SESSION_CAL_EVENT_ID = "session_cal_event_id";
        String SESSION_LIVESTREAM_URL = "session_livestream_url";
        String SESSION_MODERATOR_URL = "session_moderator_url";
        String SESSION_TAGS = "session_tags";
        String SESSION_SPEAKER_NAMES = "session_speaker_names";
        String SESSION_GROUPING_ORDER = "session_grouping_order";
        String SESSION_IMPORT_HASHCODE = "session_import_hasecode";
        String SESSION_MAIN_TAG = "session_main_tag";
        String SESSION_COLOR = "session_color";
        String SESSION_CAPTIONS_URL = "session_captions_url";
        String SESSION_INTERVAL_COUNT = "session_interval_count";
        String SESSION_PHOTO_URL = "session_photo_url";
        String SESSION_RELATED_CONTENT = "session_related_content";
    }

    interface RoomsColumns {
        String ROOM_ID = "room_id";
        String ROOM_NAME = "room_name";
        String ROOM_FLOOR = "room_floor";
    }


    interface TagsColumns {
        String TAG_ID = "tag_id";
        String TAG_CATEGORY = "tag_category";
        String TAG_NAME = "tag_name";
        String TAG_ORDER_IN_CATEGORY = "tag_order_in_category";
        String TAG_COLOR = "tag_color";
        String TAG_ABSTRACT = "tag_abstract";
    }

    interface SpeakersColumns {
        String SPEAKER_ID = "speaker_id";
        String SPEAKER_NAME = "speaker_name";
        String SPEAKER_IMAGE_URL = "speaker_image_url";
        String SPEAKER_COMPANY = "speaker_company";
        String SPEAKER_ABSTRACT = "speaker_abstract";
        String SPEAKER_URL = "speaker_url";
        String SPEAKER_PLUSONE_URL = "plusone_url";
        String SPEAKER_TWITTER_URL = "twitter_url";
        String SPEAKER_IMPORT_HASHCODE = "speaker_import_hasecode";
    }

    interface MyScheduleColumns {
        String SESSION_ID = SessionsColumns.SESSION_ID;
        String MY_SCHEDULE_ACCOUNT_NAME = "account_name";
        String MY_SCHEDULE_IN_SCHEDULE = "in_schedule";
        String MY_SCHEDULE_DIRTY_FLAG = "dirty";
    }

    public static final String CONTENT_AUTHORITY = "com.yalin.googleio";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_BLOCKS = "blocks";

    private static final String PATH_AFTER = "after";

    private static final String PATH_TAGS = "tags";

    private static final String PATH_ROOM = "room";

    private static final String PATH_UNSCHEDULED = "unscheduled";

    private static final String PATH_ROOMS = "rooms";

    private static final String PATH_SESSIONS = "sessions";

    private static final String PATH_FEEDBACK = "feedback";

    private static final String PATH_MY_SCHEDULE = "my_schedule";

    private static final String PATH_MY_VIEWED_VIDEOS = "my_viewed_videos";

    private static final String PATH_MY_FEEDBACK_SUBMITTED = "my_feedback_submitted";

    private static final String PATH_SESSIONS_COUNTER = "counter";

    private static final String PATH_SPEAKERS = "speakers";

    private static final String PATH_ANNOUNCEMENTS = "announcements";

    private static final String PATH_MAP_MARKERS = "mapmarkers";

    private static final String PATH_MAP_FLOOR = "floor";

    private static final String PATH_MAP_TILES = "maptiles";

    private static final String PATH_HASHTAGS = "hashtags";

    private static final String PATH_VIDEOS = "videos";

    private static final String PATH_SEARCH = "search";

    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    private static final String PATH_SEARCH_INDEX = "search_index";

    private static final String PATH_PEOPLE_IVE_MET = "people_ive_met";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_BLOCKS,
            PATH_TAGS,
            PATH_ROOMS,
            PATH_SESSIONS,
            PATH_FEEDBACK,
            PATH_MY_SCHEDULE,
            PATH_SPEAKERS,
            PATH_ANNOUNCEMENTS,
            PATH_MAP_MARKERS,
            PATH_MAP_FLOOR,
            PATH_MAP_MARKERS,
            PATH_MAP_TILES,
            PATH_HASHTAGS,
            PATH_VIDEOS,
            PATH_PEOPLE_IVE_MET
    };

    public static String makeContentType(String id) {
        if (id != null) {
            return CONTENT_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static String makeContentItemType(String id) {
        if (id != null) {
            return CONTENT_ITEM_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static class Blocks implements BlocksColumns, BaseColumns {

        public static final String CONTENT_TYPE_ID = "block";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BLOCKS).build();

        public static Uri buildBlockUri(String blockId) {
            return CONTENT_URI.buildUpon().appendPath(blockId).build();
        }
    }

    public static class Sessions implements SessionsColumns, RoomsColumns, SyncColumns, BaseColumns {
        public static final String QUERY_PARAMETER_TAG_FILTER = "filter";

        public static final String QUERY_PARAMETER_CATEGORIES = "categories";

        public static final String CONTENT_TYPE_ID = "session";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS).build();

        public static final String SORT_BY_TYPE_THEN_TIME = SESSION_GROUPING_ORDER + " ASC,"
                + SESSION_START + " ASC," + SESSION_TITLE + " COLLATE NOCASE ASC";

        public static Uri buildSessionUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).build();
        }

        public static Uri buildTagsDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_TAGS).build();
        }

        public static Uri buildSpeakersDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_SPEAKERS).build();
        }

        public static Uri buildSessionsAfterUri(long time) {
            return CONTENT_URI.buildUpon().appendPath(PATH_AFTER)
                    .appendPath(String.valueOf(time)).build();
        }

        public static String getSessionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Tags implements TagsColumns, BaseColumns {
        public static final String CONTENT_TYPE_ID = "tag";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();

        public static final Uri buildTagUri(String tagId) {
            return CONTENT_URI.buildUpon().appendPath(tagId).build();
        }
    }

    public static class Rooms implements RoomsColumns, BaseColumns {

    }

    public static class Speakers implements SpeakersColumns, SyncColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPEAKERS).build();

        public static final String CONTENT_TYPE_ID = "speaker";

        public static Uri buildSpeakerUri(String speakerId) {
            return CONTENT_URI.buildUpon().appendPath(speakerId).build();
        }
    }

    public static class MySchedule implements MyScheduleColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MY_SCHEDULE).build();

        public static final String CONTENT_TYPE_ID = "myschedule";
    }
}
