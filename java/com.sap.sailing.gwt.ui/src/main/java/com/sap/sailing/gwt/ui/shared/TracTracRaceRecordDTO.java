package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

public class TracTracRaceRecordDTO extends AbstractRaceRecordDTO {
    public String regattaName;
    public String jsonURL;
    public String id;
    public Date trackingStartTime;
    public Date trackingEndTime;
    public Date raceStartTime;
    public String raceStatus;
    public String raceVisibility;
    public Iterable<String> boatClassNames;

    public TracTracRaceRecordDTO() {}
    
    public TracTracRaceRecordDTO(String id, String regattaName, String name, Date trackingStartTime,
            Date trackingEndTime, Date raceStartTime, Iterable<String> boatClassNames, String status, String visibility, String jsonUrl, boolean hasRememberedRegatta) {
        super(name, hasRememberedRegatta);
        this.regattaName = regattaName;
        this.id = id;
        this.trackingStartTime = trackingStartTime;
        this.trackingEndTime = trackingEndTime;
        this.raceStartTime = raceStartTime;
        this.boatClassNames = boatClassNames;
        this.raceStatus = status;
        this.raceVisibility = visibility;
        this.jsonURL = jsonUrl;
    }
    
    @Override
    public Iterable<String> getBoatClassNames() {
        return boatClassNames;
    }

    @Override
    public String toString() {
        return "Regatta "+regattaName+", race "+getName()+" ("+trackingStartTime+" "+raceStatus+")";
    }
}
