package com.sap.sailing.domain.abstractlog.race.state.impl;

import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sse.common.TimePoint;

public class RaceStateEventImpl implements RaceStateEvent {
    
    private static final long serialVersionUID = -5523748426501793355L;
    
    private final TimePoint timePoint;
    private final RaceStateEvents eventName;

    public RaceStateEventImpl(TimePoint timePoint, RaceStateEvents eventName) {
        this.timePoint = timePoint;
        this.eventName = eventName;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }

    @Override
    public RaceStateEvents getEventName() {
        return eventName;
    }

    @Override
    public String toString() {
        return "RaceStateEventImpl [timePoint=" + timePoint + ", eventName=" + eventName + "]";
    }

}
