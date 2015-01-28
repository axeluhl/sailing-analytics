package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;

/**
 * Returns all {@link RaceLogAdditionalScoringInformationEvent}s from the race log 
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
public class AdditionalScoringInformationFinder extends RaceLogAnalyzer<List<RaceLogAdditionalScoringInformationEvent>> {

    public AdditionalScoringInformationFinder(RaceLog raceLog) {
        super(raceLog);
    }
    
    /**
     * Analyze the race log for all events that match the given type filter. Returns only the <emph>newest</emph>
     * one as we assume that there can be only one unrevoked event of the same type.
     */
    public RaceLogAdditionalScoringInformationEvent analyze(AdditionalScoringInformationType filterBy) {
        RaceLogAdditionalScoringInformationEvent result = null;
        final List<RaceLogAdditionalScoringInformationEvent> allUnrevokedEventsFromNewestToOldest = analyze();
        for (RaceLogAdditionalScoringInformationEvent event : allUnrevokedEventsFromNewestToOldest) {
            if (event != null && event.getType() == filterBy) {
                result = event;
                break;
            }
        }
        return result;
    }

    @Override
    protected List<RaceLogAdditionalScoringInformationEvent> performAnalysis() {
        final List<RaceLogAdditionalScoringInformationEvent> result = new ArrayList<RaceLogAdditionalScoringInformationEvent>();
        // fetch all unrevoked events starting with newest going to oldest
        for (RaceLogEvent event : getLog().getUnrevokedEventsDescending()) {
            if (event instanceof RaceLogAdditionalScoringInformationEvent) {
                result.add((RaceLogAdditionalScoringInformationEvent) event);
            }
        }
        return result;
    }
}
