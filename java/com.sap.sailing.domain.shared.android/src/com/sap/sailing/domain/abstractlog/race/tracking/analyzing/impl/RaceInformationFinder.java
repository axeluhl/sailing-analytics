package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;

public class RaceInformationFinder extends RaceLogAnalyzer<RaceLogDenoteForTrackingEvent> {
    public RaceInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected RaceLogDenoteForTrackingEvent performAnalysis() {
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof RaceLogDenoteForTrackingEvent) {
                return (RaceLogDenoteForTrackingEvent) event;
            }
        }

        return null;
    }
}
