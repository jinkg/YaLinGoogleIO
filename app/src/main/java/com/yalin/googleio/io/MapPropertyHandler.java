package com.yalin.googleio.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.gson.JsonElement;
import com.yalin.googleio.io.map.model.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class MapPropertyHandler extends JSONHandler {
    private HashMap<String, Tile> mTileOverlays = new HashMap<>();

    public MapPropertyHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {

    }

    @Override
    public void process(JsonElement element) {

    }

    public Collection<Tile> getTileOverlays() {
        return mTileOverlays.values();
    }
}
