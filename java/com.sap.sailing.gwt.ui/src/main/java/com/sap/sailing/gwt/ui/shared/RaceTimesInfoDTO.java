package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class RaceTimesInfoDTO implements IsSerializable {
    private RaceIdentifier raceIdentifier;

    public List<LegInfoDTO> legTimes;
    
    public Date startOfRace;
    public Date startOfTracking;
    public Date endOfTracking;
    public Date endOfRace;

    public Date timePointOfLastEvent;
    public Date timePointOfNewestEvent;

    public RaceTimesInfoDTO(RaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
    }

    public RaceTimesInfoDTO() {}

    public RaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public void setRaceIdentifier(RaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
    }

    public LegInfoDTO getLastLegTimes() {
        if (legTimes == null || legTimes.isEmpty()) {
            return null;
        }
        LegInfoDTO lastLegTime = null;
        Iterator<LegInfoDTO> iterator = legTimes.iterator();
        while(iterator.hasNext()) {
            lastLegTime = iterator.next();
        }
        return lastLegTime;
    }

    public List<LegInfoDTO> getLegTimes() {
        return legTimes;
    }

    public void setLegTimes(List<LegInfoDTO> legTimes) {
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

    public Date getTimePointOfLastEvent() {
        return timePointOfLastEvent;
    }

    public void setTimePointOfLastEvent(Date timePointOfLastEvent) {
        this.timePointOfLastEvent = timePointOfLastEvent;
    }

    public Date getTimePointOfNewestEvent() {
        return timePointOfNewestEvent;
    }

    public void setTimePointOfNewestEvent(Date timePointOfNewestEvent) {
        this.timePointOfNewestEvent = timePointOfNewestEvent;
    }
}
