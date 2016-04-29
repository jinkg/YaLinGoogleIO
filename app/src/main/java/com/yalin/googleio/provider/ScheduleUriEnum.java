package com.yalin.googleio.provider;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public enum ScheduleUriEnum {
    BLOCKS(100, "blocks", ScheduleContract.Blocks.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.BLOCKS),
    TAGS(200, "tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.TAGS),
    SESSIONS(400, "sessions", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.SESSIONS),
    SESSIONS_ID(405, "sessions/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, true, null),
    SESSIONS_ID_SPEAKERS(406, "sessions/*/speakers", ScheduleContract.Speakers.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.SESSIONS_SPEAKERS),
    SESSIONS_ID_TAGS(407, "sessions/*/tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.SESSIONS_TAGS),
    SPEAKERS(500, "speakers", ScheduleContract.Speakers.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.SPEAKERS),
    SPEAKERS_ID(501, "speakers/*", ScheduleContract.Speakers.CONTENT_TYPE_ID, true, null),
    SEARCH_INDEX(801, "search_index", null, false, null);


    public int code;
    public String path;
    public String contentType;
    public String table;

    ScheduleUriEnum(int code, String path, String contentTypeId, boolean item, String table) {
        this.code = code;
        this.path = path;
        this.contentType = item ? ScheduleContract.makeContentItemType(contentTypeId)
                : ScheduleContract.makeContentType(contentTypeId);
        this.table = table;
    }
}
