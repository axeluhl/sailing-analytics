package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.scoring.AdditionalScoringInformationEvent;

public class AdditionalScoringInformationFinder extends RaceLogAnalyzer<AdditionalScoringInformationEvent> {

    public AdditionalScoringInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected AdditionalScoringInformationEvent performAnalysis() {
        AdditionalScoringInformationEvent result = null;
        for (RaceLogEvent event : getAllEventsDescending()) {
            if (event instanceof AdditionalScoringInformationEvent) {
                AdditionalScoringInformationEvent scoringEvent = (AdditionalScoringInformationEvent) event;
                result = scoringEvent;
            }
        }
        return result;
    }
}
