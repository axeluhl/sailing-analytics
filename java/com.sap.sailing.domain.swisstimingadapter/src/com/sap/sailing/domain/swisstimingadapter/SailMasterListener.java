package com.sap.sailing.domain.swisstimingadapter;

import java.util.Collection;

import com.sap.sailing.domain.base.TimePoint;

public interface SailMasterListener {
    void receivedRacePositionData(String raceID, RaceStatus status, TimePoint timePoint, TimePoint startTime,
            long millisecondsSinceRaceStart, int nextMarkIndexForLeader, double distanceToNextMarkForLeaderInMeters,
            Collection<Fix> fixes);
}
