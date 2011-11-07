package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SwissTimingRaceRecordDAO implements IsSerializable {
    public String ID;
    public String description;
    public Date raceStartTime;

    public SwissTimingRaceRecordDAO() {}
    
    public SwissTimingRaceRecordDAO(String id, String description, Date raceStartTime) {
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
