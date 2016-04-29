package com.yalin.googleio.io.model;

import java.util.Random;

/**
 * 作者：YaLin
 * 日期：2016/4/19.
 */
public class Session {
    public String id;
    public String url;
    public String description;
    public String title;
    public String[] tags;
    public String startTimestamp;
    public String youtubeUrl;
    public String[] speakers;
    public String entTimestamp;
    public String hashtag;
    public String subtype;
    public String room;
    public String captionsUrl;
    public String photoUrl;
    public boolean isLivestream;
    public String mainTag;
    public String color;
    public RelatedContent[] relatedContent;
    public int groupingOrder;

    public class RelatedContent {
        public String id;
        public String name;
    }

    public String getImportHashCode() {
        return new Random().nextLong() + "";
    }

    public String makeTagsList() {
        int i;
        if (tags.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(tags[0]);
        for (i = 1; i < tags.length; i++) {
            sb.append(",").append(tags[i]);
        }
        return sb.toString();
    }
}
