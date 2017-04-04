package com.sap.sailing.gwt.home.communication.event.sixtyinch;

import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.commands.DTO;
import com.sap.sse.gwt.dispatch.shared.commands.Result;

public class GetSixtyInchStatisticDTO implements DTO, Result {
    private int competitors;
    private int legs;
    private Duration duration;

    public GetSixtyInchStatisticDTO() {
    }

    public GetSixtyInchStatisticDTO(int competitors, int legs, Duration duration) {
        this.competitors = competitors;
        this.legs = legs;
        this.duration = duration;
    }

    public int getCompetitors() {
        return competitors;
    }

    public int getLegs() {
        return legs;
    }

    public Duration getDuration() {
        return duration;
    }
}
