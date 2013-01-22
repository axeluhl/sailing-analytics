package com.sap.sailing.domain.persistence.media;

import java.util.Date;

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
}
