package com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.tracking.DenoteForTrackingEvent;

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
