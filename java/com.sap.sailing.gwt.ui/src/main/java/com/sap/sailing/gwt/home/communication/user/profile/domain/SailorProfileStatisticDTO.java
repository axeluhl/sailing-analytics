package com.sap.sailing.gwt.home.communication.user.profile.domain;

import java.io.Serializable;

import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

/** Contains the result of a single statistic, to allow async loading of statistics without blocking the rest **/
public class SailorProfileStatisticDTO implements Result, Serializable {
    private static final long serialVersionUID = 2924378586764418626L;
    private Double value;
    private TimePoint timeOfBest;
    private String bestCompetitorIdAsString;


    protected SailorProfileStatisticDTO() {
        super();
    }

    public SailorProfileStatisticDTO(Double value, TimePoint timeOfBest, String bestCompetitorIdAsString) {
        this.value = value;
        this.timeOfBest = timeOfBest;
        this.bestCompetitorIdAsString = bestCompetitorIdAsString;
    }

    public TimePoint getTimeOfBest() {
        return timeOfBest;
    }

    public Double getValue() {
        return value;
    }

    public String getBestCompetitorIdAsString() {
        return bestCompetitorIdAsString;
    }
}
