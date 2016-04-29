package com.yalin.googleio.sync;


import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.ConsoleRequestLogger;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.RequestLogger;
import com.yalin.googleio.io.BlocksHandler;
import com.yalin.googleio.io.HashtagsHandler;
import com.yalin.googleio.io.JSONHandler;
import com.yalin.googleio.io.MapPropertyHandler;
import com.yalin.googleio.io.RoomsHandler;
import com.yalin.googleio.io.SearchSuggestHandler;
import com.yalin.googleio.io.SessionsHandler;
import com.yalin.googleio.io.SpeakersHandler;
import com.yalin.googleio.io.TagsHandler;
import com.yalin.googleio.io.VideosHandler;
import com.yalin.googleio.io.map.model.Tile;
import com.yalin.googleio.provider.ScheduleContract;
import com.yalin.googleio.util.IOUtils;
import com.yalin.googleio.util.MapUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static com.yalin.googleio.util.LogUtils.LOGD;
import static com.yalin.googleio.util.LogUtils.LOGE;
import static com.yalin.googleio.util.LogUtils.LOGW;
import static com.yalin.googleio.util.LogUtils.makeLogTag;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class ConferenceDataHandler {
    private static final String TAG = makeLogTag(ConferenceDataHandler.class);

    private static final String SP_KEY_DATA_TIMESTAMP = "data_timestamp";

    private static final String DATA_KEY_ROOMS = "rooms";
    private static final String DATA_KEY_BLOCKS = "blocks";
    private static final String DATA_KEY_TAGS = "tags";
    private static final String DATA_KEY_SPEAKERS = "speakers";
    private static final String DATA_KEY_SESSIONS = "sessions";
    private static final String DATA_KEY_SEARCH_SUGGESTIONS = "search_suggestions";
    private static final String DATA_KEY_MAP = "map";
    private static final String DATA_KEY_HASH_TAGS = "hashtags";
    private static final String DATA_KEY_VIDEOS = "video_library";

    private static final String[] DATA_KEYS_IN_ORDER = {
            DATA_KEY_ROOMS,
            DATA_KEY_BLOCKS,
            DATA_KEY_TAGS,
            DATA_KEY_SPEAKERS,
            DATA_KEY_SESSIONS,
            DATA_KEY_SEARCH_SUGGESTIONS,
            DATA_KEY_MAP,
            DATA_KEY_HASH_TAGS,
            DATA_KEY_VIDEOS
    };

    private HashMap<String, JSONHandler> mHandlerForKey = new HashMap<>();

    private Context mContext;

    RoomsHandler mRoomsHandler = null;
    BlocksHandler mBlocksHandler = null;
    TagsHandler mTagsHandler = null;
    SpeakersHandler mSpeakersHandler = null;
    SessionsHandler mSessionsHandler = null;
    SearchSuggestHandler mSearchSuggestHandler = null;
    MapPropertyHandler mMapPropertyHandler = null;
    HashtagsHandler mHashtagsHandler = null;
    VideosHandler mVideosHandler = null;

    private int mContentProviderOperationsDone = 0;

    public ConferenceDataHandler(Context context) {
        mContext = context;
    }

    public void applyConferenceData(String[] dataBodies, String dataTimestamp,
                                    boolean downloadsAllowed) throws IOException {
        LOGD(TAG, "Applying data from " + dataBodies.length + " files, timestamp " + dataTimestamp);

        mHandlerForKey.put(DATA_KEY_ROOMS, mRoomsHandler = new RoomsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_BLOCKS, mBlocksHandler = new BlocksHandler(mContext));
        mHandlerForKey.put(DATA_KEY_TAGS, mTagsHandler = new TagsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_SPEAKERS, mSpeakersHandler = new SpeakersHandler(mContext));
        mHandlerForKey.put(DATA_KEY_SESSIONS, mSessionsHandler = new SessionsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_SEARCH_SUGGESTIONS, mSearchSuggestHandler = new SearchSuggestHandler(mContext));
        mHandlerForKey.put(DATA_KEY_MAP, mMapPropertyHandler = new MapPropertyHandler(mContext));
        mHandlerForKey.put(DATA_KEY_HASH_TAGS, mHashtagsHandler = new HashtagsHandler(mContext));
        mHandlerForKey.put(DATA_KEY_VIDEOS, mVideosHandler = new VideosHandler(mContext));

        LOGD(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            LOGD(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            processDataBody(dataBodies[i]);
        }

        mSessionsHandler.setTagMap(mTagsHandler.getTagMap());
        mSessionsHandler.setSpeakerMap(mSpeakersHandler.getSpeakerMap());

        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        for (String key : DATA_KEYS_IN_ORDER) {
            LOGD(TAG, "Building content provider operations for: " + key);
            mHandlerForKey.get(key).makeContentProviderOperations(batch);
            LOGD(TAG, "Content provider operations so far: " + batch.size());
        }
        LOGD(TAG, "Total content provider operations: " + batch.size());

        LOGD(TAG, "Processing map overlay files.");
        processMapOverlayFiles(mMapPropertyHandler.getTileOverlays(), downloadsAllowed);

        LOGD(TAG, "Applying " + batch.size() + " content provider operations.");

        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(ScheduleContract.CONTENT_AUTHORITY, batch);
            }
            LOGD(TAG, "Successfully applied " + operations + " content provider operations");
            mContentProviderOperationsDone += operations;
        } catch (RemoteException e) {
            LOGE(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", e);
        } catch (OperationApplicationException e) {
            LOGE(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", e);
        }

        LOGD(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        for (String path : ScheduleContract.TOP_LEVEL_PATHS) {
            Uri uri = ScheduleContract.BASE_CONTENT_URI.buildUpon().appendPath(path).build();
            resolver.notifyChange(uri, null);
        }

        setDataTimestamp(dataTimestamp);
        LOGD(TAG, "Done applying conference data.");
    }

    private void processDataBody(String dataBody) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(dataBody));
        JsonParser parser = new JsonParser();

        try {
            reader.setLenient(true);
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (mHandlerForKey.containsKey(key)) {
                    mHandlerForKey.get(key).process(parser.parse(reader));
                } else {
                    LOGW(TAG, "Skipping unknown key in conference data json: " + key);
                    reader.skipValue();
                }
            }
            reader.endObject();
        } finally {
            reader.close();
        }
    }

    private void processMapOverlayFiles(Collection<Tile> collection, boolean downloadAllowed) {
        boolean shouldClearCache = false;
        ArrayList<String> usedTiles = new ArrayList<>();
        for (Tile tile : collection) {
            final String filename = tile.filename;
            final String url = tile.url;

            usedTiles.add(filename);

            if (!MapUtils.hasTile(mContext, filename)) {
                shouldClearCache = true;
                if (MapUtils.hasTileAsset(mContext, filename)) {
                    MapUtils.copyTileAsset(mContext, filename);
                } else if (downloadAllowed && !TextUtils.isEmpty(url)) {
                    try {
                        File tileFile = MapUtils.getTileFile(mContext, filename);
                        BasicHttpClient httpClient = new BasicHttpClient();
                        httpClient.setRequestLogger(mQuietLogger);
                        HttpResponse httpResponse = httpClient.get(url, null);

                        IOUtils.writeToFile(httpResponse.getBody(), tileFile);

                        InputStream is = new FileInputStream(tileFile);
                        SVG svg = new SVGBuilder().readFromInputStream(is).build();
                        is.close();
                    } catch (IOException e) {
                        LOGD(TAG, "FAILED downloading map overlay tile " + url + ": " + e.getMessage(), e);
                    }
                } else {
                    LOGD(TAG, "Skipping download of map overlay tile" +
                            " (since downloadsAllowed=false)");
                }
            }
        }
        if (shouldClearCache) {
            MapUtils.clearDiskCache(mContext);
        }
        MapUtils.removeUnusedTiles(mContext, usedTiles);
    }

    private void setDataTimestamp(String timestamp) {
        LOGD(TAG, "Setting data timestamp to: " + timestamp);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(
                SP_KEY_DATA_TIMESTAMP, timestamp).commit();
    }

    private RequestLogger mQuietLogger = new ConsoleRequestLogger() {
        @Override
        public void logRequest(HttpURLConnection uc, Object content) throws IOException {
        }

        @Override
        public void logResponse(HttpResponse res) {
        }
    };
}
