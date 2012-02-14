package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class RaceTimesInfoDTO implements IsSerializable {
    private RaceIdentifier raceIdentifier;

    private List<LegTimesInfoDTO> legTimes;
    
    private Date startOfRace;
    private Date startOfTracking;
    private Date endOfTracking;
    private Date endOfRace;

    public RaceTimesInfoDTO() {}

    public RaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
    }

    public LegTimesInfoDTO getLastLegTimes() {
        if(legTimes == null || legTimes.isEmpty())
            return null;
        
        LegTimesInfoDTO lastLegTime = null;
        
        Iterator<LegTimesInfoDTO> iterator = legTimes.iterator();
        while(iterator.hasNext())
            lastLegTime = iterator.next();
        
        return lastLegTime;
    }

    public List<LegTimesInfoDTO> getLegTimes() {
        return legTimes;
    }

    public void setLegTimes(List<LegTimesInfoDTO> legTimes) {
        this.legTimes = legTimes;
    }

    public Date getStartOfRace() {
        return startOfRace;
    }

    public void setStartOfRace(Date startOfRace) {
        this.startOfRace = startOfRace;
    }

    public Date getStartOfTracking() {
        return startOfTracking;
    }

    public void setStartOfTracking(Date startOfTracking) {
        this.startOfTracking = startOfTracking;
    }

    public Date getEndOfTracking() {
        return endOfTracking;
    }

    public void setEndOfTracking(Date endOfTracking) {
        this.endOfTracking = endOfTracking;
    }

    public Date getEndOfRace() {
        return endOfRace;
    }

    public void setEndOfRace(Date endOfRace) {
        this.endOfRace = endOfRace;
    }

    
}
