package com.sap.sailing.domain.abstractlog.orc;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;

/**
 * Returns a map with unique entries per {@link RaceLogORCLegDataEvent#getOneBasedLegNumber() one-based leg number}
 * where the effective {@link RaceLogORCLegDataEvent} is reported as the value in the resulting map.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RaceLogORCLegDataEventFinder extends RaceLogAnalyzer<Map<Integer, RaceLogORCLegDataEvent>> {

    public RaceLogORCLegDataEventFinder(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    protected Map<Integer, RaceLogORCLegDataEvent> performAnalysis() {
        final Map<Integer, RaceLogORCLegDataEvent> result = new HashMap<>();
        for (RaceLogEvent event : getLog().getUnrevokedEvents()) {
            if (event instanceof RaceLogORCLegDataEvent) {
                final RaceLogORCLegDataEvent legDataEvent = (RaceLogORCLegDataEvent) event;
                result.put(legDataEvent.getOneBasedLegNumber(), legDataEvent);
            }
        }
        return result;
    }
}
