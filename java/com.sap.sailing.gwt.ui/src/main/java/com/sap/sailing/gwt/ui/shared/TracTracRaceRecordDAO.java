package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TracTracRaceRecordDAO implements IsSerializable {
    public String eventName;
    public String name;
    public String replayURL;
    public String liveURI;
    public String storedURI;
    public String paramURL;
    public String ID;
    public Date trackingStartTime;
    public Date trackingEndTime;
    public Date raceStartTime;

    public TracTracRaceRecordDAO() {}
    
    public TracTracRaceRecordDAO(String id, String eventName, String name, String paramURL,
            String replayURL, String liveURI, String storedURI, Date trackingStartTime, Date trackingEndTime, Date raceStartTime) {
        super();
        this.eventName = eventName;
        this.name = name;
        this.replayURL = replayURL;
        this.paramURL = paramURL;
        this.liveURI = liveURI;
        this.storedURI = storedURI;
        ID = id;
        this.trackingStartTime = trackingStartTime;
        this.trackingEndTime = trackingEndTime;
        this.raceStartTime = raceStartTime;
    }
    
    @Override
    public String toString() {
        return "Race "+name+" ("+trackingStartTime+")";
    }
}
