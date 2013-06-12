package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagFinder;

public class LastFlagFinderTest extends RaceLogAnalyzerTest<LastFlagFinder> {

    @Override
    protected LastFlagFinder createAnalyzer(RaceLog raceLog) {
        return new LastFlagFinder(raceLog);
    }

    @Test
    public void testLastFlagFinder() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2);
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2, analyzer.getLastFlagEvent());
    }
}
