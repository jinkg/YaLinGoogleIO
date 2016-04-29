package com.yalin.googleio.framework;

import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * 作者：YaLin
 * 日期：2016/4/18.
 */
public interface Model {
    QueryEnum[] getQueries();

    boolean readDataFromCursor(Cursor cursor, QueryEnum query);

    Loader<Cursor> createCursorLoader(int loaderId, Uri uri, Bundle args);

    boolean requestModelUpdate(UserActionEnum action, @Nullable Bundle args);
}
