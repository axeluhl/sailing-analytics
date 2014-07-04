package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;

public class RaceInformationFinder extends RaceLogAnalyzer<DenoteForTrackingEvent> {
    public RaceInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected DenoteForTrackingEvent performAnalysis() {
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof DenoteForTrackingEvent) {
                return (DenoteForTrackingEvent) event;
            }
        }

        return null;
    }
}
