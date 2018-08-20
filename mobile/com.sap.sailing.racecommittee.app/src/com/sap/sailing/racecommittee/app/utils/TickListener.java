package com.sap.sailing.racecommittee.app.utils;

import com.sap.sse.common.TimePoint;

public interface TickListener {
    void notifyTick(TimePoint now);
}
