package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;

public class StartModeFlagFinder extends RaceLogAnalyzer<Flags> {

    private final RacingProcedureTypeAnalyzer procedureAnalyzer;
    private final List<Flags> startModeFlags;

    /**
     * Searches for the start mode flags.
     * 
     * @param procedureAnalyzer
     *            to be used to ensure a racing procedure with start mode flags. Must operate on the same race log. Otherwise a
     *            {@link IllegalArgumentException} is thrown.
     */
    public StartModeFlagFinder(RacingProcedureTypeAnalyzer procedureAnalyzer, RaceLog raceLog, List<Flags> startModeFlags) {
        super(raceLog);
        if (raceLog != procedureAnalyzer.getLog()) {
            throw new IllegalArgumentException("Both analyzers must operate on the same race log.");
        }
        this.procedureAnalyzer = procedureAnalyzer;
        this.startModeFlags = startModeFlags;
    }

    @Override
    protected Flags performAnalysis() {
        RacingProcedureType type = procedureAnalyzer.analyze();
        if (!(RacingProcedureType.RRS26.equals(type) || RacingProcedureType.SWC.equals(type))) {
            return null;
        }
        
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogFlagEvent) {
                RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                if (isStartModeFlagEvent(flagEvent)) {
                    return flagEvent.getUpperFlag();
                }
            }
        }
        return null;
    }
    
    private boolean isStartModeFlagEvent(RaceLogFlagEvent event) {
        // no matter if displayed or removed
        if (event.getLowerFlag().equals(Flags.NONE)) {
            return startModeFlags.contains(event.getUpperFlag());
        }
        return false;
    }
}
