package com.yalin.googleio.util;

import android.content.Context;

import com.bumptech.glide.disklrucache.DiskLruCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static com.yalin.googleio.util.LogUtils.*;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class MapUtils {
    private static final String TAG = makeLogTag(MapUtils.class);
    private static final String TILE_PATH = "maptiles";

    public static boolean hasTile(Context context, String filename) {
        return getTileFile(context, filename).exists();
    }

    public static File getTileFile(Context context, String filename) {
        File folder = new File(context.getFilesDir(), TILE_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, filename);
    }

    private static String[] mapTileAssets;

    public static boolean hasTileAsset(Context context, String filename) {
        if (mapTileAssets == null) {
            try {
                mapTileAssets = context.getAssets().list(TILE_PATH);
            } catch (IOException e) {
                mapTileAssets = new String[0];
            }
        }

        for (String s : mapTileAssets) {
            if (s.equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public static boolean copyTileAsset(Context context, String filename) {
        if (!hasTileAsset(context, filename)) {
            return false;
        }

        try {
            InputStream is = context.getAssets().open(TILE_PATH + File.separator + filename);
            File f = getTileFile(context, filename);
            FileOutputStream os = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int dataSize;
            while ((dataSize = is.read(buffer)) > 0) {
                os.write(buffer, 0, dataSize);
            }
            os.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static void removeUnusedTiles(Context context, final ArrayList<String> usedTiles) {
        File folder = new File(context.getFilesDir(), TILE_PATH);
        File[] unused = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return !usedTiles.contains(filename);
            }
        });
        if (unused != null) {
            for (File f : unused) {
                f.delete();
            }
        }
    }

    private static final int MAX_DISK_CACHE_BYTES = 1024 * 1024 * 2;

    public static DiskLruCache openDiskCache(Context context) {
        File cacheDir = new File(context.getCacheDir(), "tiles");
        try {
            return DiskLruCache.open(cacheDir, 1, 3, MAX_DISK_CACHE_BYTES);
        } catch (IOException e) {
            LOGE(TAG, "Couldn't open disk cache.");
        }
        return null;
    }

    public static void clearDiskCache(Context context) {
        DiskLruCache cache = openDiskCache(context);
        if (cache != null) {
            try {
                LOGD(TAG, "Clearing map tile dis cache.");
                cache.delete();
                cache.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
