package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.ProtestStartTimeFinder;

public class ProtestStartTimeFinderTest extends PassAwareRaceLogAnalyzerTest<ProtestStartTimeFinder, TimePoint> {

    @Override
    protected ProtestStartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new ProtestStartTimeFinder(raceLog);
    }

    @Override
    protected TimePoint setupTargetEventsForPassAwareTests(int passId) {
        RaceLogProtestStartTimeEvent event = createEvent(RaceLogProtestStartTimeEvent.class, 1, passId);
        when(event.getProtestStartTime()).thenReturn(mock(TimePoint.class));
        raceLog.add(event);
        return event.getProtestStartTime();
    }
    
    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertNull(analyzer.analyze());
    }
    
    @Test
    public void testMostRecent() {
        RaceLogProtestStartTimeEvent event1 = createEvent(RaceLogProtestStartTimeEvent.class, 1);
        when(event1.getProtestStartTime()).thenReturn(mock(TimePoint.class));
        RaceLogProtestStartTimeEvent event2 = createEvent(RaceLogProtestStartTimeEvent.class, 2);
        when(event2.getProtestStartTime()).thenReturn(mock(TimePoint.class));
        
        raceLog.add(event1);
        raceLog.add(event2);

        raceLog.lockForRead();
        assertEquals(event2.getProtestStartTime(), analyzer.analyze());
    }
}
