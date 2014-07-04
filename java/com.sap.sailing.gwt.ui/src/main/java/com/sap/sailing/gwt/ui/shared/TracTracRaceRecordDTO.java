package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TracTracRaceRecordDTO implements IsSerializable {
    public String regattaName;
    public String name;
    public String jsonURL;
    public String id;
    public Date trackingStartTime;
    public Date trackingEndTime;
    public Date raceStartTime;
    public String raceStatus;
    public Iterable<String> boatClassNames;
    public boolean hasRememberedRegatta;

    public TracTracRaceRecordDTO() {}
    
    public TracTracRaceRecordDTO(String id, String regattaName, String name, Date trackingStartTime,
            Date trackingEndTime, Date raceStartTime, Iterable<String> boatClassNames, String status, String jsonUrl, boolean hasRememberedRegatta) {
        super();
        this.regattaName = regattaName;
        this.name = name;
        this.id = id;
        this.trackingStartTime = trackingStartTime;
        this.trackingEndTime = trackingEndTime;
        this.raceStartTime = raceStartTime;
        this.boatClassNames = boatClassNames;
        this.raceStatus = status;
        this.jsonURL = jsonUrl;
        this.hasRememberedRegatta = hasRememberedRegatta;
    }
    
    @Override
    public String toString() {
        return "Race "+name+" ("+trackingStartTime+" "+raceStatus+")";
    }
}
