package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.sap.sailing.domain.common.RaceIdentifier;

public class VideoTrack {

    private final RaceIdentifier raceIdentifier;
    private Date startTime;
    // private final double lengthInSeconds;
    private final String url;

    public RaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public Date getStartTime() {
        return startTime;
    }

    // public double getLengthInSeconds() {
    // return lengthInSeconds;
    // }
    //
    public VideoTrack(RaceIdentifier raceName, Date startTime, /* double lengthInSeconds, */String url) {
        this.raceIdentifier = raceName;
        this.startTime = startTime;
        // this.lengthInSeconds = lengthInSeconds;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

}
