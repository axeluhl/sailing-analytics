package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;

public class RaceStatusAnalyzerTest extends PassAwareRaceLogAnalyzerTest<RaceStatusAnalyzer, RaceLogRaceStatus> {

    @Override
    protected RaceStatusAnalyzer createAnalyzer(RaceLog raceLog) {
        return new RaceStatusAnalyzer(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, RaceLogEventAuthor author) {
        RaceLogRaceStatusEvent event = createEvent(RaceLogRaceStatusEvent.class, 1, passId, author);
        when(event.getNextStatus()).thenReturn(RaceLogRaceStatus.RUNNING);
        return new TargetPair(Arrays.asList(event), event.getNextStatus());
    }
    
    @Override
    protected TargetPair getBlockingEventsAndResultForPassAwareTests(
            int passId, RaceLogEventAuthor author) {
        RaceLogRaceStatusEvent event = createEvent(RaceLogRaceStatusEvent.class, 1, passId, author);
        when(event.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        return new TargetPair(Arrays.asList(event), event.getNextStatus());
    }
    
    @Test
    public void testUnscheduldedForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertEquals(RaceLogRaceStatus.UNSCHEDULED, analyzer.analyze());
    }
    
    @Test
    public void testMostRecent() {
        RaceLogRaceStatusEvent event1 = createEvent(RaceLogRaceStatusEvent.class, 1);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2);
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getNextStatus(), analyzer.analyze());
    }
}
