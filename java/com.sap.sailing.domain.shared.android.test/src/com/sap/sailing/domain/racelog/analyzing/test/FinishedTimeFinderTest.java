package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class FinishedTimeFinderTest extends PassAwareRaceLogAnalyzerTest<FinishedTimeFinder, TimePoint> {

    @Override
    protected FinishedTimeFinder createAnalyzer(RaceLog raceLog) {
        return new FinishedTimeFinder(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, AbstractLogEventAuthor author) {
        RaceLogRaceStatusEvent event = createEvent(RaceLogRaceStatusEvent.class, 1, passId, author);
        when(event.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        return new TargetPair(Arrays.asList(event), event.getLogicalTimePoint());
    }
    
    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertNull(analyzer.analyze());
    }
    
    @Test
    public void testMostRecent() {
        RaceLogRaceStatusEvent event1 = createEvent(RaceLogRaceStatusEvent.class, 1);
        when(event1.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2);
        when(event2.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getLogicalTimePoint(), analyzer.analyze());
    }
    
    @Test
    public void testOnlyFinished() {
        RaceLogRaceStatusEvent event1 = createEvent(RaceLogRaceStatusEvent.class, 1);
        when(event1.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2);
        when(event2.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event1.getLogicalTimePoint(), analyzer.analyze());
    }
}
