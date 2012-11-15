package com.sap.sailing.domain.persistence.media;

import java.util.Date;

public class DBMediaTrack {
    
    public final String dbId;
    public final String title;
    public final String url;
    public final Date startTime;
    public final int durationInMillis;
    public final String mimeType;
    public final String mimeSubType;

    public DBMediaTrack(String dbId, String title, String url, Date startTime, int duration, String mimeType, String mimeSubType) {
        super();
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.durationInMillis = duration;
        this.mimeType = mimeType;
        this.mimeSubType = mimeSubType;
    }
}
