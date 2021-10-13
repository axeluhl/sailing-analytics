package com.sap.sailing.domain.yellowbrickadapter.impl;

import com.sap.sailing.domain.yellowbrickadapter.YellowBrickRace;
import com.sap.sse.common.TimePoint;

public class YellowBrickRaceImpl implements YellowBrickRace {
    private final String raceUrl;
    private final TimePoint timePointOfLastFix;
    private int numberOfCompetitors;
    
    public YellowBrickRaceImpl(String raceUrl, TimePoint timePointOfLastFix, int numberOfCompetitors) {
        super();
        this.raceUrl = raceUrl;
        this.timePointOfLastFix = timePointOfLastFix;
        this.numberOfCompetitors = numberOfCompetitors;
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
        return numberOfCompetitors;
    }

}
