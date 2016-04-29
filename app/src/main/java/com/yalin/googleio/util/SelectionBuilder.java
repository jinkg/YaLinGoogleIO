package com.yalin.googleio.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Selection;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.yalin.googleio.util.LogUtils.*;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class SelectionBuilder {
    private static final String TAG = makeLogTag(SelectionBuilder.class);

    private String mTable = null;

    private StringBuilder mSelection = new StringBuilder();

    private Map<String, String> mProjectionMap = new HashMap<>();

    private ArrayList<String> mSelectionArgs = new ArrayList<>();

    private String mGroupBy = null;

    private String mHaving = null;

    public SelectionBuilder table(String table) {
        mTable = table;
        return this;
    }

    public SelectionBuilder table(String table, String... tableParams) {
        if (tableParams != null && tableParams.length > 0) {
            String[] parts = table.split("[?]", tableParams.length + 1);
            StringBuilder sb = new StringBuilder(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                sb.append('"').append(tableParams[i - 1]).append('"')
                        .append(parts[i]);
            }
            mTable = sb.toString();
        } else {
            mTable = table;
        }
        return this;
    }

    public SelectionBuilder where(String selection, String... selectionArgs) {
        if (TextUtils.isEmpty(selection)) {
            if (selectionArgs != null && selectionArgs.length > 0) {
                throw new IllegalArgumentException(
                        "Valid selection required when including arguments=");
            }
            return this;
        }

        if (mSelection.length() > 0) {
            mSelection.append(" AND ");
        }

        mSelection.append("(").append(selection).append(")");
        if (selection != null) {
            Collections.addAll(mSelectionArgs, selectionArgs);
        }
        return this;
    }

    public SelectionBuilder mapToTable(String column, String table) {
        mProjectionMap.put(column, table + "." + column);
        return this;
    }

    public SelectionBuilder map(String fromColumn, String toClause) {
        mProjectionMap.put(fromColumn, toClause + " AS " + fromColumn);
        return this;
    }

    public SelectionBuilder groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    public String getSelection() {
        return mSelection.toString();
    }

    public String[] getSelectionArgs() {
        return mSelectionArgs.toArray(new String[mSelectionArgs.size()]);
    }

    private void mapColumns(String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            final String target = mProjectionMap.get(columns[i]);
            if (target != null) {
                columns[i] = target;
            }
        }
    }

    private void assertTable() {
        if (mTable == null) {
            throw new IllegalStateException("Table not specified.");
        }
    }

    @Override
    public String toString() {
        return "SelectionBuilder[table=" + mTable + ", selection=" + getSelection()
                + ", selectionArgs=" + Arrays.toString(getSelectionArgs())
                + "projectionMap= " + mProjectionMap + " ]";
    }

    public Cursor query(SQLiteDatabase db, boolean distinct, String[] columns, String orderBy,
                        String limit) {
        assertTable();
        if (columns != null) mapColumns(columns);
        LOGV(TAG, "query(columns=" + Arrays.toString(columns)
                + ", distinct=" + distinct + ")" + this);
        return db.query(distinct, mTable, columns, getSelection(), getSelectionArgs(), mGroupBy,
                mHaving, orderBy, limit);
    }

    public int update(SQLiteDatabase db, ContentValues values) {
        assertTable();
        LOGV(TAG, "update() " + this);
        return db.update(mTable, values, getSelection(), getSelectionArgs());
    }

    public int delete(SQLiteDatabase db) {
        assertTable();
        LOGV(TAG, "delete() " + this);
        return db.delete(mTable, getSelection(), getSelectionArgs());
    }
}
