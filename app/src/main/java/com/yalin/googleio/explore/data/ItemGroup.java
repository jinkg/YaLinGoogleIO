package com.yalin.googleio.explore.data;

import java.util.ArrayList;

/**
 * 作者：YaLin
 * 日期：2016/4/20.
 */
public class ItemGroup {
    private String mTitle;
    private String mId;
    private ArrayList<SessionData> sessions = new ArrayList<>();

    public void addSessionData(SessionData session) {
        sessions.add(session);
    }

    public void trimSessionData(int sessionLimit) {
        while (sessions.size() > sessionLimit) {
            sessions.remove(0);
        }
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public ArrayList<SessionData> getSessions() {
        return sessions;
    }
}
