package com.yalin.googleio.provider;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.google.common.collect.Table;
import com.yalin.googleio.provider.ScheduleContract.Blocks;
import com.yalin.googleio.provider.ScheduleContract.BlocksColumns;
import com.yalin.googleio.provider.ScheduleContract.MySchedule;
import com.yalin.googleio.provider.ScheduleContract.Rooms;
import com.yalin.googleio.provider.ScheduleContract.RoomsColumns;
import com.yalin.googleio.provider.ScheduleContract.Sessions;
import com.yalin.googleio.provider.ScheduleContract.SessionsColumns;
import com.yalin.googleio.provider.ScheduleContract.Speakers;
import com.yalin.googleio.provider.ScheduleContract.SpeakersColumns;
import com.yalin.googleio.provider.ScheduleContract.SyncColumns;
import com.yalin.googleio.provider.ScheduleContract.Tags;
import com.yalin.googleio.provider.ScheduleContract.TagsColumns;
import com.yalin.googleio.util.AccountUtils;
import com.yalin.googleio.util.LogUtils;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGI;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class ScheduleDatabase extends SQLiteOpenHelper {
    private static final String TAG = makeLogTag(ScheduleDatabase.class);

    private static final String DATABASE_NAME = "schedule.db";

    private static final int VER_2014_RELEASE_A = 122;
    private static final int VER_2014_RELEASE_C = 207;
    private static final int VER_2015_RELEASE_A = 208;
    private static final int VER_2015_RELEASE_B = 210;
    private static final int CUR_DATABASE_VERSION = VER_2015_RELEASE_B;

    private Context mContext;

    interface Tables {
        String BLOCKS = "blocks";
        String TAGS = "tags";
        String ROOMS = "rooms";
        String SESSIONS = "sessions";
        String MY_SCHEDULE = "myschedule";
        String MY_VIEWED_VIDEO = "myviewedvideos";
        String MY_FEEDBACK_SUBMITTED = "myfeedbacksubmitted";
        String SPEAKERS = "speakers";
        String SESSIONS_TAGS = "sessions_tags";
        String SESSIONS_SPEAKERS = "sessions_speakers";
        String ANNOUNCEMENTS = "announcements";
        String MAPMARKERS = "mapmarkers";
        String MAPTILES = "mapoverlays";
        String HASHTAGS = "hashtags";
        String FEEDBACK = "feedback";
        String VIDEOS = "videos";

        String SESSIONS_JOIN_ROOMS_TAGS = "sessions "
                + "LEFT OUTER JOIN myschedule ON sessions.session_id=myschedule.session_id "
                + "AND myschedule.account_name=? "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id "
                + "LEFT OUTER JOIN sessions_tags ON sessions.session_id=sessions_tags.session_id";
    }

    public interface SessionsSpeakers {
        String SESSION_ID = "session_id";
        String SPEAKER_ID = "speaker_id";
    }

    public interface SessionsTags {
        String SESSION_ID = "session_id";
        String TAG_ID = "tag_id";
    }

    private interface References {
        String BLOCK_ID = "REFERENCES " + Tables.BLOCKS + "(" + Blocks.BLOCK_ID + ")";
        String TAG_ID = "REFERENCES " + Tables.TAGS + "(" + Tags.TAG_ID + ")";
        String ROOM_ID = "REFERENCES " + Tables.ROOMS + "(" + Rooms.ROOM_ID + ")";
        String SESSION_ID = "REFERENCES " + Tables.SESSIONS + "(" + Sessions.SESSION_ID + ")";
        String SPEAKER_ID = "REFERENCES " + Tables.SPEAKERS + "(" + Speakers.SPEAKER_ID + ")";
    }

    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.BLOCKS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BlocksColumns.BLOCK_ID + " TEXT NOT NULL,"
                + BlocksColumns.BLOCK_TITLE + " TEXT NOT NULL,"
                + BlocksColumns.BLOCK_START + " INTEGER NOT NULL,"
                + BlocksColumns.BLOCK_END + " INTEGER NOT NULL,"
                + BlocksColumns.BLOCK_TYPE + " TEXT,"
                + "UNIQUE (" + BlocksColumns.BLOCK_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagsColumns.TAG_ID + " TEXT NOT NULL,"
                + TagsColumns.TAG_CATEGORY + " TEXT NOT NULL,"
                + TagsColumns.TAG_NAME + " TEXT NOT NULL,"
                + TagsColumns.TAG_ORDER_IN_CATEGORY + " INTEGER,"
                + TagsColumns.TAG_COLOR + " TEXT NOT NULL,"
                + TagsColumns.TAG_ABSTRACT + " TEXT NOT NULL,"
                + "UNIQUE (" + TagsColumns.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.ROOMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RoomsColumns.ROOM_ID + " TEXT NOT NULL,"
                + RoomsColumns.ROOM_NAME + " TEXT,"
                + RoomsColumns.ROOM_FLOOR + " TEXT,"
                + "UNIQUE (" + RoomsColumns.ROOM_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_ID + " TEXT NOT NULL,"
                + Sessions.ROOM_ID + " TEXT " + References.ROOM_ID + ","
                + SessionsColumns.SESSION_START + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_END + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_LEVEL + " TEXT,"
                + SessionsColumns.SESSION_TITLE + " TEXT,"
                + SessionsColumns.SESSION_ABSTRACT + " TEXT,"
                + SessionsColumns.SESSION_REQUIREMENTS + " TEXT,"
                + SessionsColumns.SESSION_KEYWORDS + " TEXT,"
                + SessionsColumns.SESSION_HASHTAG + " TEXT,"
                + SessionsColumns.SESSION_URL + " TEXT,"
                + SessionsColumns.SESSION_YOUTUBE_URL + " TEXT,"
                + SessionsColumns.SESSION_MODERATOR_URL + " TEXT,"
                + SessionsColumns.SESSION_PDF_URL + " TEXT,"
                + SessionsColumns.SESSION_NOTES_URL + " TEXT,"
                + SessionsColumns.SESSION_CAL_EVENT_ID + " INTEGER,"
                + SessionsColumns.SESSION_LIVESTREAM_URL + " TEXT,"
                + SessionsColumns.SESSION_TAGS + " TEXT,"
                + SessionsColumns.SESSION_GROUPING_ORDER + " INTEGER,"
                + SessionsColumns.SESSION_SPEAKER_NAMES + " TEXT,"
                + SessionsColumns.SESSION_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + SessionsColumns.SESSION_MAIN_TAG + " TEXT,"
                + SessionsColumns.SESSION_COLOR + " INTEGER,"
                + SessionsColumns.SESSION_CAPTIONS_URL + " TEXT,"
                + SessionsColumns.SESSION_PHOTO_URL + " TEXT,"
                + SessionsColumns.SESSION_RELATED_CONTENT + " TEXT,"
                + "UNIQUE (" + SessionsColumns.SESSION_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SpeakersColumns.SPEAKER_ID + " TEXT NOT NULL,"
                + SpeakersColumns.SPEAKER_NAME + " TEXT,"
                + SpeakersColumns.SPEAKER_IMAGE_URL + " TEXT,"
                + SpeakersColumns.SPEAKER_COMPANY + " TEXT,"
                + SpeakersColumns.SPEAKER_ABSTRACT + " TEXT,"
                + SpeakersColumns.SPEAKER_URL + " TEXT,"
                + SpeakersColumns.SPEAKER_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + "UNIQUE (" + SpeakersColumns.SPEAKER_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.MY_SCHEDULE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MySchedule.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + MySchedule.MY_SCHEDULE_ACCOUNT_NAME + " TEXT NOT NULL,"
                + MySchedule.MY_SCHEDULE_DIRTY_FLAG + " INTEGER NOT NULL DEFAULT 1,"
                + MySchedule.MY_SCHEDULE_IN_SCHEDULE + " INTEGER NOT NULL DEFAULT 1,"
                + "UNIQUE (" + MySchedule.SESSION_ID + ","
                + MySchedule.MY_SCHEDULE_ACCOUNT_NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS_SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsSpeakers.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + " TEXT NOT NULL " + References.SPEAKER_ID + ","
                + "UNIQUE (" + SessionsSpeakers.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS_TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsTags.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsTags.TAG_ID + " TEXT NOT NULL " + References.TAG_ID + ","
                + "UNIQUE (" + SessionsTags.SESSION_ID + ","
                + SessionsTags.TAG_ID + ") ON CONFLICT REPLACE)");

        upgradeFrom2014Cto2015A(db);
        upgradeFrom2015Ato2015B(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        Account account = AccountUtils.getActiveAccount(mContext);
        if (account != null) {
            LOGI(TAG, "Cancelling any pending syncs for account");
            ContentResolver.cancelSync(account, ScheduleContract.CONTENT_AUTHORITY);
        }

        int version = oldVersion;

        boolean dataInvalidated = true;

        if (version == VER_2014_RELEASE_C) {
            LOGD(TAG, "Upgrading database from 2014 release C to 2015 release A.");
            upgradeFrom2014Cto2015A(db);
            version = VER_2015_RELEASE_A;
        }

        if (version == VER_2015_RELEASE_A) {
            LOGD(TAG, "Upgrading database from 2015 release A to 2015 release B.");
            upgradeFrom2015Ato2015B(db);
            version = VER_2015_RELEASE_B;
        }

        LOGD(TAG, "After upgrade logic, at version " + version);
    }

    private void upgradeFrom2014Cto2015A(SQLiteDatabase db) {
    }

    private void upgradeFrom2015Ato2015B(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + Tables.SPEAKERS
                + " ADD COLUMN " + SpeakersColumns.SPEAKER_PLUSONE_URL + " TEXT");
        db.execSQL("ALTER TABLE " + Tables.SPEAKERS
                + " ADD COLUMN " + SpeakersColumns.SPEAKER_TWITTER_URL + " TEXT");
    }

    static void updateSessionSearchIndex(SQLiteDatabase db) {

    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
