package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogExcludeWindSourcesEvent;
import com.sap.sailing.domain.common.WindSource;

public class ExcludedWindSourcesFinder extends RaceLogAnalyzer<Iterable<WindSource>> {

    public ExcludedWindSourcesFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Iterable<WindSource> performAnalysis() {
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof RaceLogExcludeWindSourcesEvent) {
                RaceLogExcludeWindSourcesEvent excludedWindSourcesEvent = (RaceLogExcludeWindSourcesEvent) event;
                return excludedWindSourcesEvent.getWindSourcesToExclude();
            }
        }
        return null;
    }
}
