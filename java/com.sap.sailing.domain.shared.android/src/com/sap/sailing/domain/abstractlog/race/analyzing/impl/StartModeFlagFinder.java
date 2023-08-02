package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureFactoryImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.ConfigurableStartModeFlagRacingProcedure;
import com.sap.sailing.domain.base.configuration.impl.EmptyRegattaConfiguration;
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
        if (type == RacingProcedureType.UNKNOWN ||
                !(new RacingProcedureFactoryImpl(/* author */ null, new EmptyRegattaConfiguration()).createRacingProcedure(type, new RaceLogImpl(UUID.randomUUID()), /* raceLogResolver */ null)
                        instanceof ConfigurableStartModeFlagRacingProcedure)) {
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
        final boolean result;
        // no matter if displayed or removed
        if (event.getLowerFlag().equals(Flags.NONE)) {
            result = startModeFlags.contains(event.getUpperFlag());
        } else {
            result = false;
        }
        return result;
    }
}
