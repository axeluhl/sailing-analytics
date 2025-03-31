package com.sap.sailing.domain.swisstimingadapter;

import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sse.common.Util;

public interface SwissTimingRaceTracker extends RaceTracker {
    /**
     * The key for a SwissTiming race tracker is the triple of raceID, host name and port number.
     */
    Util.Triple<String, String, Integer> getID();
}
