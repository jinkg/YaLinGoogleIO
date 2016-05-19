package com.yalin.googleio.model;


/**
 * 作者：YaLin
 * 日期：2016/5/19.
 */
public class ScheduleItem implements Cloneable, Comparable<ScheduleItem> {
    public static final int FREE = 0;
    public static final int SESSION = 1;
    public static final int BREAK = 2;

    public static final int SESSION_TYPE_SESSION = 1;
    public static final int SESSION_TYPE_CODELAB = 2;
    public static final int SESSION_TYPE_BOXTALK = 3;
    public static final int SESSION_TYPE_MISC = 4;

    public long startTime = 0;
    public long endTime = 0;

    public int type = FREE;

    public int sessionType = SESSION_TYPE_MISC;

    public String sessionId = "";

    public String title = "";
    public String subtitle = "";
    public String room;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ignore) {
            return new ScheduleItem();
        }
    }

    @Override
    public int compareTo(ScheduleItem another) {
        return startTime < another.startTime ? -1 :
                (startTime > another.startTime ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ScheduleItem)) {
            return false;
        }
        ScheduleItem i = (ScheduleItem) o;
        return type == i.type && sessionId.equals(i.sessionId) &&
                startTime == i.startTime &&
                endTime == i.endTime;
    }
}
