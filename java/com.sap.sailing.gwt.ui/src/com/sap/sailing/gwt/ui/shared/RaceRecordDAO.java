package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RaceRecordDAO implements IsSerializable {
    public String name;
    public String replayURL;
    public String paramURL;
    public String ID;
    public Date trackingStartTime;
    public Date trackingEndTime;
    public Date raceStartTime;

    public RaceRecordDAO() {}
    
    public RaceRecordDAO(String id, String name, String paramURL, String replayURL,
            Date trackingStartTime, Date trackingEndTime, Date raceStartTime) {
        super();
        this.name = name;
        this.replayURL = replayURL;
        this.paramURL = paramURL;
        ID = id;
        this.trackingStartTime = trackingStartTime;
        this.trackingEndTime = trackingEndTime;
        this.raceStartTime = raceStartTime;
    }
    
}
