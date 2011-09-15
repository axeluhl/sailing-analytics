package com.sap.sailing.domain.tractracadapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.util.DateParser;
import com.sap.sailing.util.InvalidDateException;

public class RaceRecord {
    private static final Logger logger = Logger.getLogger(RaceRecord.class.getName());
    
    private final String eventName;
    private final String name;
    private final String replayURL;
    private final String ID;
    private final URL paramURL;
    private final TimePoint trackingstarttime;
    private final TimePoint trackingendtime;
    private final TimePoint racestarttime;
    
    public RaceRecord(URL jsonURL, String eventName, String name, String replayURL, String ID,
            String trackingstarttime, String trackingendtime, String racestarttime) throws MalformedURLException {
        super();
        this.eventName = eventName;
        this.name = name;
        this.replayURL = replayURL;
        this.ID = ID;
        TimePoint tp = null;
        if (trackingstarttime != null) {
            try {
                tp = new MillisecondsTimePoint(DateParser.parse(trackingstarttime).getTime());
            } catch (InvalidDateException e) {
                logger.warning("Unable to parse trackingstarttime of race "+name+": "+trackingstarttime+". Leaving null.");
            }
        }
        this.trackingstarttime = tp;
        tp = null;
        if (trackingendtime != null) {
            try {
                tp = new MillisecondsTimePoint(DateParser.parse(trackingendtime).getTime());
            } catch (InvalidDateException e) {
                logger.warning("Unable to parse trackingendtime of race "+name+": "+trackingendtime+". Leaving null.");
            }
        }
        this.trackingendtime = tp;
        tp = null;
        if (racestarttime != null) {
            try {
                tp = new MillisecondsTimePoint(DateParser.parse(racestarttime).getTime());
            } catch (InvalidDateException e) {
                logger.warning("Unable to parse racestarttime of race "+name+": "+racestarttime+". Leaving null.");
            }
        }
        this.racestarttime = tp;
        
        String jsonURLAsString = jsonURL.toString();
        int indexOfLastSlash = jsonURLAsString.lastIndexOf('/');
        int indexOfLastButOneSlash = jsonURLAsString.lastIndexOf('/', indexOfLastSlash-1);
        String technicalEventName = jsonURLAsString.substring(indexOfLastButOneSlash+1, indexOfLastSlash);
        paramURL = new URL(jsonURLAsString.substring(0, indexOfLastSlash)+"/clientparams.php?event="+
                technicalEventName+"&race="+ID);
    }

    public String getName() {
        return name;
    }

    public String getEventName() {
        return eventName;
    }

    public String getReplayURL() {
        return replayURL;
    }

    public String getID() {
        return ID;
    }

    public URL getParamURL() {
        return paramURL;
    }

    public TimePoint getTrackingStartTime() {
        return trackingstarttime;
    }

    public TimePoint getTrackingEndTime() {
        return trackingendtime;
    }

    public TimePoint getRaceStartTime() {
        return racestarttime;
    }
    
}

