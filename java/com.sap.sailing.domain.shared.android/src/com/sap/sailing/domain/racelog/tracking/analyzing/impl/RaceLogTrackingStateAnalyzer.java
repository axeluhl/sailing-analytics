package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.StartTrackingEvent;

public class RaceLogTrackingStateAnalyzer extends RaceLogAnalyzer<RaceLogTrackingState> {

    public RaceLogTrackingStateAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogTrackingState performAnalysis() {
        for (RaceLogEvent event : raceLog.getUnrevokedEventsDescending()) {
            if (event instanceof StartTrackingEvent) {
                return RaceLogTrackingState.TRACKING;
            } else if (event instanceof DenoteForTrackingEvent) {
                return RaceLogTrackingState.AWAITING_RACE_DEFINITION;
            }
        }
        return RaceLogTrackingState.NOT_A_RACELOG_TRACKED_RACE;
    }

}
