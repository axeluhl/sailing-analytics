package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;

/**
 * Analyzer to perform a query over a given {@link RaceLog}. Each subclass defines its <code>ResultType</code>.
 * 
 * @param <ResultType>
 *            type of analyzation's result.
 */
public abstract class RaceLogAnalyzer<ResultType> {

    protected RaceLog raceLog;

    public RaceLogAnalyzer(RaceLog raceLog) {
        this.raceLog = raceLog;
    }

    public RaceLog getRaceLog() {
        return raceLog;
    }

    public ResultType analyze() {
        raceLog.lockForRead();
        try {
            return performAnalysis();
        } finally {
            raceLog.unlockAfterRead();
        }
    }

    protected abstract ResultType performAnalysis();

    protected Iterable<RaceLogEvent> getPassEvents() {
        return raceLog.getFixes();
    }

    protected Iterable<RaceLogEvent> getAllEvents() {
        return raceLog.getRawFixes();
    }

    protected Iterable<RaceLogEvent> getPassEventsDescending() {
        return raceLog.getFixesDescending();
    }

    protected Iterable<RaceLogEvent> getAllEventsDescending() {
        return raceLog.getRawFixesDescending();
    }
}
