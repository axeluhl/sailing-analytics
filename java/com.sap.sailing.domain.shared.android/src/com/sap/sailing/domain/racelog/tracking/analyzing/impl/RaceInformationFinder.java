package com.sap.sailing.domain.racelog.tracking.analyzing.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;

public class RaceInformationFinder extends RaceLogAnalyzer<Pair<String, BoatClass>> {
    public RaceInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Pair<String, BoatClass> performAnalysis() {
        for (RaceLogEvent event : getAllEvents()) {
            if (event instanceof DenoteForTrackingEvent) {
                DenoteForTrackingEvent e = (DenoteForTrackingEvent) event;
                return new Pair<String, BoatClass>(e.getRaceName(), e.getBoatClass());
            }
        }

        return null;
    }
}
