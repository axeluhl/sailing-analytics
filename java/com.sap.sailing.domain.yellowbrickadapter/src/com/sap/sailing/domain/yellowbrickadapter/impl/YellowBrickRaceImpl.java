package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class YellowBrickRaceImpl implements YellowBrickRace {
    private final String raceUrl;
    private final TimePoint timePointOfLastFix;
    private Iterable<TeamPositions> teamPositions;
    
    public YellowBrickRaceImpl(String raceUrl, TimePoint timePointOfLastFix, Iterable<TeamPositions> teamPositions) {
        super();
        this.raceUrl = raceUrl;
        this.timePointOfLastFix = timePointOfLastFix;
        this.teamPositions = teamPositions;
    }

    @Override
    public String getRaceUrl() {
        return raceUrl;
    }

    @Override
    public TimePoint getTimePointOfLastFix() {
        return timePointOfLastFix;
    }

    @Override
    public int getNumberOfCompetitors() {
        return Util.size(teamPositions);
    }
    
    @Override
    public Iterable<TeamPositions> getTeamsPositions() {
        return teamPositions;
    }
}
