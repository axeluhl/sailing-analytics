package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class EventStatisticsDTO implements DTO {

    private Integer regattasFoughtCount;
    private Integer competitorsCount;
    private Integer racesRunCount;
    private Integer trackedRacesCount;

    private long numberOfGPSFixes;
    private long numberOfWindFixes;
    private Triple<Competitor, Speed, TimePoint> maxSpeed;
    private Distance totalDistanceTraveled;
    
    @SuppressWarnings("unused")
    private EventStatisticsDTO() {
    }
    
    public EventStatisticsDTO(Integer regattasFoughtCount, Integer competitorsCount, Integer racesRunCount,
            Integer trackedRacesCount, long numberOfGPSFixes, long numberOfWindFixes, Triple<Competitor, Speed, TimePoint> maxSpeed, Distance totalDistanceTraveled) {
        super();
        this.regattasFoughtCount = regattasFoughtCount;
        this.competitorsCount = competitorsCount;
        this.racesRunCount = racesRunCount;
        this.trackedRacesCount = trackedRacesCount;
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.numberOfWindFixes = numberOfWindFixes;
        this.maxSpeed = maxSpeed;
        this.totalDistanceTraveled = totalDistanceTraveled;
    }

    public Integer getRegattasFoughtCount() {
        return regattasFoughtCount;
    }

    public void setRegattasFoughtCount(Integer regattasFoughtCount) {
        this.regattasFoughtCount = regattasFoughtCount;
    }

    public Integer getCompetitorsCount() {
        return competitorsCount;
    }

    public void setCompetitorsCount(Integer competitorsCount) {
        this.competitorsCount = competitorsCount;
    }

    public Integer getRacesRunCount() {
        return racesRunCount;
    }

    public void setRacesRunCount(Integer racesRunCount) {
        this.racesRunCount = racesRunCount;
    }

    public Integer getTrackedRacesCount() {
        return trackedRacesCount;
    }

    public void setTrackedRacesCount(Integer trackedRacesCount) {
        this.trackedRacesCount = trackedRacesCount;
    }


    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public Triple<Competitor, Speed, TimePoint> getMaxSpeed() {
        return maxSpeed;
    }

    public Distance getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }


}
