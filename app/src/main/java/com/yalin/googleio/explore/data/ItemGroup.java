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
}
