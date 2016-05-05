package com.yalin.googleio.explore.data;

import android.content.Context;
import android.text.TextUtils;

import com.yalin.googleio.util.UIUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 作者：YaLin
 * 日期：2016/4/20.
 */
public class SessionData {
    public String sessionName;
    public String details;
    public String sessionId;
    public String imageUrl;
    public String mainTag;
    public Date startDate;
    public Date endDate;
    public String liveStreamId;
    public String youtubeUrl;
    public String tags;
    public boolean inSchedule;

    public void update(String sessionName, String details, String sessionId, String imageUrl,
                       String mainTag, long startTime, long endTime, String liveStreamId,
                       String youtubeUrl, String tags, boolean inSchedule) {
        this.sessionName = sessionName;
        this.details = details;
        this.sessionId = sessionId;
        this.imageUrl = imageUrl;
        this.mainTag = mainTag;
        try {
            this.startDate = new Date(startTime);
        } catch (Exception ignored) {
        }
        try {
            this.endDate = new Date(endTime);
        } catch (Exception ignored) {
        }
        this.liveStreamId = liveStreamId;
        this.youtubeUrl = youtubeUrl;
        this.tags = tags;
        this.inSchedule = inSchedule;
    }

    public boolean isLiveStreamNow(Context context) {
        if (!isLiveStreamAvailable()) {
            return false;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(UIUtils.getCurrentTime(context));
        return startDate.before(now.getTime()) && endDate.after(now.getTime());
    }

    public boolean isLiveStreamAvailable() {
        return !TextUtils.isEmpty(liveStreamId);
    }

    public boolean isVideoAvailable() {
        return !TextUtils.isEmpty(youtubeUrl);
    }

    public String getSessionName() {
        return sessionName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMainTag() {
        return mainTag;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getLiveStreamId() {
        return liveStreamId;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public String getTags() {
        return tags;
    }

    public boolean isInSchedule() {
        return inSchedule;
    }
}
