package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;

/**
 * Returns all {@link AdditionalScoringInformationEvent}s from the race log 
 * that have not been revoked when using {@link #analyze()}. The resulting list 
 * has the newest event first. The pass id is not relevant for the result.
 * 
 * The reason that this finder returns a list is that there could be more than
 * one event of the same class but having a different {@link AdditionalScoringInformationType}.
 *  
 * One can also use {@link #analyze(AdditionalScoringInformationType)} to check
 * for the existence of an event with exactly the provided type.
 * 
 * @author Simon Marcel Pamies
 */
public class AdditionalScoringInformationFinder extends RaceLogAnalyzer<List<AdditionalScoringInformationEvent>> {

    public AdditionalScoringInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    /**
     * Analyze the race log for all events that match the given type filter. Returns only the <emph>newest</emph>
     * one as we assume that there can be only one unrevoked event of the same type.
     */
    public AdditionalScoringInformationEvent analyze(AdditionalScoringInformationType filterBy) {
        AdditionalScoringInformationEvent result = null;
        final List<AdditionalScoringInformationEvent> allUnrevokedEventsFromNewestToOldest = analyze();
        for (AdditionalScoringInformationEvent event : allUnrevokedEventsFromNewestToOldest) {
            if (event != null && event.getType() == filterBy) {
                result = event;
                break;
            }
        }
        return result;
    }

    @Override
    protected List<AdditionalScoringInformationEvent> performAnalysis() {
        final List<AdditionalScoringInformationEvent> result = new ArrayList<AdditionalScoringInformationEvent>();
        // fetch all unrevoked events starting with newest going to oldest
        for (RaceLogEvent event : getRaceLog().getUnrevokedEventsDescending()) {
            if (event instanceof AdditionalScoringInformationEvent) {
                result.add((AdditionalScoringInformationEvent) event);
            }
        }
        return result;
    }
}
