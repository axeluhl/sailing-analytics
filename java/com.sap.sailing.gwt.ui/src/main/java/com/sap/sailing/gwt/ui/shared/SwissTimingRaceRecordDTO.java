package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingRaceRecordDTO implements IsSerializable {
    public String ID;
    public String description;
    public Date raceStartTime;
    
    /** boatClass and discipline (men, woman, all) are derived data from the object ID */
    public String boatClass;
    public String discipline;

    public SwissTimingRaceRecordDTO() {}
    
    public SwissTimingRaceRecordDTO(String id, String description, Date raceStartTime) {
        super();
        ID = id;
        this.description = description;
        this.raceStartTime = raceStartTime;
    }
    
    @Override
    public String toString() {
        return "Race "+ID+" ("+raceStartTime+")";
    }
}
