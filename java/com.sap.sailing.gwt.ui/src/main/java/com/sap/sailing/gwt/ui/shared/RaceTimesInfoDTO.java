package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.gwt.client.player.Timer;

public class RaceTimesInfoDTO implements IsSerializable {
    private RegattaAndRaceIdentifier raceIdentifier;

    public List<LegInfoDTO> legInfos;

    public List<MarkPassingTimesDTO> markPassingTimes;
    
    public Date startOfRace;
    public Date startOfTracking;
    public Date endOfTracking;
    public Date endOfRace;
    public Date raceFinishedTime;
    public Date newestTrackingEvent;
    
    /**
     * The current time on the server when this object was created. Clients can use this to synchronize a clock
     * difference, e.g., in the {@link Timer} class.
     */
    public Date currentServerTime;
    
    public long delayToLiveInMs;

    public RaceTimesInfoDTO(RegattaAndRaceIdentifier raceIdentifier) {
        this.raceIdentifier = raceIdentifier;
    }

    public RaceTimesInfoDTO() {}

    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return raceIdentifier;
    }

    public void setRaceIdentifier(RegattaAndRaceIdentifier raceIdentifier) {
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
    
    public Date getFinishedTime() {
        return raceFinishedTime;
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
    
    @Override
	public String toString() {
		return "RaceTimesInfoDTO [raceIdentifier=" + raceIdentifier
				+ ", legInfos=" + legInfos + ", markPassingTimes="
				+ markPassingTimes + ", startOfRace=" + startOfRace
				+ ", startOfTracking=" + startOfTracking + ", endOfTracking="
				+ endOfTracking + ", endOfRace=" + endOfRace + ", finishedTime=" + raceFinishedTime
				+ ", newestTrackingEvent=" + newestTrackingEvent
                                + ", currentServerTime=" + currentServerTime
				+ ", delayToLiveInMs=" + delayToLiveInMs + "]";
	}
}
