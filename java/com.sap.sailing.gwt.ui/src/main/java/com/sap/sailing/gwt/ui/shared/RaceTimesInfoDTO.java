package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RaceIdentifier;

public class RaceTimesInfoDTO implements IsSerializable {
    private RaceIdentifier raceIdentifier;

    public List<LegInfoDTO> legInfos;

    public List<MarkPassingTimesDTO> markPassingTimes;
    
    public Date startOfRace;
    public Date startOfTracking;
    public Date endOfTracking;
    public Date endOfRace;
    public Date newestTrackingEvent;
    public long delayToLiveInMs;

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

    public MarkPassingTimesDTO getLastMarkPassingTimes() {
        if (markPassingTimes == null || markPassingTimes.isEmpty()) {
            return null;
        }
        MarkPassingTimesDTO lastTimes = null;
        Iterator<MarkPassingTimesDTO> iterator = markPassingTimes.iterator();
        while(iterator.hasNext()) {
            lastTimes = iterator.next();
        }
        return lastTimes;
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

    public Date getNewestTrackingEvent() {
        return newestTrackingEvent;
    }

    public void setNewestTrackingEvent(Date newestTrackingEvent) {
        this.newestTrackingEvent = newestTrackingEvent;
    }

    public List<LegInfoDTO> getLegInfos() {
        return legInfos;
    }

    public void setLegInfos(List<LegInfoDTO> legInfos) {
        this.legInfos = legInfos;
    }

    public List<MarkPassingTimesDTO> getMarkPassingTimes() {
        return markPassingTimes;
    }

    public void setMarkPassingTimes(List<MarkPassingTimesDTO> markPassingTimes) {
        this.markPassingTimes = markPassingTimes;
    }
}
