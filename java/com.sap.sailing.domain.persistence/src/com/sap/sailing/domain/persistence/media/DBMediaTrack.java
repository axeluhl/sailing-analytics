package com.sap.sailing.domain.persistence.media;

import java.util.Date;

public class DBMediaTrack {
    
    public final String title;
    public final String url;
    public final Date startTime;
    public final String mimeType;
    public final String mimeSubType;

    public DBMediaTrack(String title, String url, Date startTime, String mimeType, String mimeSubType) {
        super();
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.mimeType = mimeType;
        this.mimeSubType = mimeSubType;
    }
}
