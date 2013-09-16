package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;

public class StartTimeFinderTest extends PassAwareRaceLogAnalyzerTest<StartTimeFinder, TimePoint> {

    @Override
    protected StartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new StartTimeFinder(raceLog);
    }
    
    @Override
    protected TimePoint setupTargetEventsForPassAwareTests(int passId) {
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1, passId);
        when(event.getStartTime()).thenReturn(mock(TimePoint.class));
        raceLog.add(event);
        return event.getStartTime();
    }
    
    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertNull(analyzer.analyze());
    }
    
    @Test
    public void testMostRecent() {
        RaceLogStartTimeEvent event1 = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event1.getStartTime()).thenReturn(mock(TimePoint.class));
        RaceLogStartTimeEvent event2 = createEvent(RaceLogStartTimeEvent.class, 2);
        when(event2.getStartTime()).thenReturn(mock(TimePoint.class));
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getStartTime(), analyzer.analyze());
    }
}
