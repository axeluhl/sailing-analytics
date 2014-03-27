package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingRaceRecordDTO implements IsSerializable {
    public String raceId;
    public String raceName;
    public Date raceStartTime;
    
    public String boatClass;
    public String gender;

    public boolean hasCourse;
    public boolean hasStartlist;
    
    public SwissTimingRaceRecordDTO() {}
    
    public SwissTimingRaceRecordDTO(String raceId, String raceName, Date raceStartTime) {
        super();
        this.raceId = raceId;
        this.raceName = raceName;
        this.raceStartTime = raceStartTime;
    }
    
    @Override
    public String toString() {
        return "Race "+raceId+" ("+raceStartTime+")";
    }
}
