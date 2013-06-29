package com.sap.sailing.domain.racelog.analyzing.impl;

import java.util.List;
import java.util.Arrays;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;

public class RRS26StartModeFlagFinder extends RaceLogAnalyzer<Flags> {

    private final List<Flags> startModeFlags = Arrays.asList(Flags.PAPA, Flags.ZULU, Flags.BLACK, Flags.INDIA);
    private StartProcedureTypeAnalyzer procedureAnalyzer;

    /**
     * Searches for the start mode flag of a RRS26 race.
     * 
     * @param procedureAnalyzer
     *            to be used to ensure a RRS26 race. Must operate on the same race log. Otherwise a
     *            {@link IllegalArgumentException} is thrown.
     */
    public RRS26StartModeFlagFinder(StartProcedureTypeAnalyzer procedureAnalyzer, RaceLog raceLog) {
        super(raceLog);
        if (raceLog != procedureAnalyzer.getRaceLog()) {
            throw new IllegalArgumentException("Both analyzers must operate on the same race log.");
        }
        this.procedureAnalyzer = procedureAnalyzer;
    }

    @Override
    protected Flags performAnalyzation() {
        StartProcedureType type = procedureAnalyzer.analyze();
        if (StartProcedureType.RRS26.equals(type)) {
            for (RaceLogEvent event : getPassEventsDescending()) {
                if (event instanceof RaceLogFlagEvent) {
                    RaceLogFlagEvent flagEvent = (RaceLogFlagEvent) event;
                    if (isStartModeFlagEvent(flagEvent)) {
                        return flagEvent.getUpperFlag();
                    }
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
