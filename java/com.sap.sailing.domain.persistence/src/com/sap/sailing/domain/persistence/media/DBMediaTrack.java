package com.sap.sailing.domain.persistence.media;

import java.util.Date;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;

public class DBMediaTrack {
    
    public final String dbId;
    public final String title;
    public final String url;
    public final Date startTime;
    public final int durationInMillis;
    public final String mimeType;

    public DBMediaTrack(String dbId, String title, String url, Date startTime, int duration, String mimeType) {
        super();
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.durationInMillis = duration;
        this.mimeType = mimeType;
    }
    
    public TimePoint startTime() {
        if (this.startTime != null) {
            return new MillisecondsTimePoint(this.startTime);
        } else {
            return null;
        }
    }
    
}
