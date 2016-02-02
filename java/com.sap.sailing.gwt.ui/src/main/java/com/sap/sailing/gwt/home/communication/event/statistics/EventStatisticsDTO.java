package com.sap.sailing.gwt.home.communication.event.statistics;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.gwt.dispatch.client.DTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Triple;

public class EventStatisticsDTO implements DTO {
    private int regattasFoughtCount;
    private int competitorsCount;
    private int racesRunCount;
    private int trackedRacesCount;
    private long numberOfGPSFixes;
    private long numberOfWindFixes;
    private String competitorInfo;
    private Double competitorSpeedInKnots;
    private Double totalDistanceTraveled;

    @SuppressWarnings("unused")
    private EventStatisticsDTO() {
    }

    @GwtIncompatible
    public EventStatisticsDTO(int regattasFoughtCount, int competitorsCount, int racesRunCount, int trackedRacesCount,
            long numberOfGPSFixes, long numberOfWindFixes,
            Triple<Competitor, Speed, TimePoint> maxSpeed, Distance totalDistanceTraveled) {
        super();
        this.regattasFoughtCount = regattasFoughtCount;
        this.competitorsCount = competitorsCount;
        this.racesRunCount = racesRunCount;
        this.trackedRacesCount = trackedRacesCount;
        this.numberOfGPSFixes = numberOfGPSFixes;
        this.numberOfWindFixes = numberOfWindFixes;
        if (maxSpeed != null) {
            this.competitorInfo = maxSpeed.getA().getName();
            this.competitorSpeedInKnots = maxSpeed.getB().getKnots();
        }
        if (totalDistanceTraveled != null) {
            this.totalDistanceTraveled = totalDistanceTraveled.getSeaMiles();
        }
    }

    public int getRegattasFoughtCount() {
        return regattasFoughtCount;
    }

    public void setRegattasFoughtCount(int regattasFoughtCount) {
        this.regattasFoughtCount = regattasFoughtCount;
    }

    public int getCompetitorsCount() {
        return competitorsCount;
    }

    public void setCompetitorsCount(int competitorsCount) {
        this.competitorsCount = competitorsCount;
    }

    public int getRacesRunCount() {
        return racesRunCount;
    }

    public void setRacesRunCount(int racesRunCount) {
        this.racesRunCount = racesRunCount;
    }

    public int getTrackedRacesCount() {
        return trackedRacesCount;
    }

    public void setTrackedRacesCount(int trackedRacesCount) {
        this.trackedRacesCount = trackedRacesCount;
    }

    public long getNumberOfGPSFixes() {
        return numberOfGPSFixes;
    }

    public long getNumberOfWindFixes() {
        return numberOfWindFixes;
    }

    public String getCompetitorInfo() {
        return competitorInfo;
    }

    public Double getCompetitorSpeed() {
        return competitorSpeedInKnots;
    }

    public Double getTotalDistanceTraveled() {
        return totalDistanceTraveled;
    }
}
