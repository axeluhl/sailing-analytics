package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.abstractlog.race.tracking.StartTrackingEvent;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;

public class RaceLogTrackingStateAnalyzer extends RaceLogAnalyzer<RaceLogTrackingState> {

    public RaceLogTrackingStateAnalyzer(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogTrackingState performAnalysis() {
        for (RaceLogEvent event : getLog().getUnrevokedEventsDescending()) {
            if (event instanceof StartTrackingEvent) {
                return RaceLogTrackingState.TRACKING;
            } else if (event instanceof DenoteForTrackingEvent) {
                return RaceLogTrackingState.AWAITING_RACE_DEFINITION;
            }
        }
        return RaceLogTrackingState.NOT_A_RACELOG_TRACKED_RACE;
    }

}
