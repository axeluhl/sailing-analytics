package com.sap.sailing.domain.persistence.media;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class DBMediaTrack {
    
    public final String dbId;
    public final String title;
    public final String url;
    public final TimePoint startTime;
    public final Duration duration;
    public final String mimeType;
    public RegattaAndRaceIdentifier regattaAndRace;

    public DBMediaTrack(String dbId, String title, String url, TimePoint startTime, Duration duration, String mimeType, RegattaAndRaceIdentifier regattaAndRace) {
        super();
        this.dbId = dbId;
        this.title = title;
        this.url = url;
        this.startTime = startTime;
        this.duration = duration;
        this.mimeType = mimeType;
        this.regattaAndRace = regattaAndRace;
    }
    
}
