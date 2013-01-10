package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TracTracRaceRecordDTO implements IsSerializable {
    public String regattaName;
    public String name;
    public String replayURL;
    public String liveURI;
    public String storedURI;
    public String paramURL;
    public String ID;
    public Date trackingStartTime;
    public Date trackingEndTime;
    public Date raceStartTime;
    public Iterable<String> boatClassNames;

    public TracTracRaceRecordDTO() {}
    
    public TracTracRaceRecordDTO(String id, String regattaName, String name, String paramURL,
            String replayURL, String liveURI, String storedURI, Date trackingStartTime, Date trackingEndTime, Date raceStartTime,
            Iterable<String> boatClassNames) {
        super();
        this.regattaName = regattaName;
        this.name = name;
        this.replayURL = replayURL;
        this.paramURL = paramURL;
        this.liveURI = liveURI;
        this.storedURI = storedURI;
        ID = id;
        this.trackingStartTime = trackingStartTime;
        this.trackingEndTime = trackingEndTime;
        this.raceStartTime = raceStartTime;
        this.boatClassNames = boatClassNames;
    }
    
    @Override
    public String toString() {
        return "Race "+name+" ("+trackingStartTime+")";
    }
}
