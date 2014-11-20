package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sse.common.TimePoint;

public class StartTimeFinderTest extends PassAwareRaceLogAnalyzerTest<StartTimeFinder, TimePoint> {

    @Override
    protected StartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new StartTimeFinder(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId,
            RaceLogEventAuthor author) {
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1, passId, author);
        when(event.getStartTime()).thenReturn(mock(TimePoint.class));
        return new TargetPair(Arrays.asList(event), event.getStartTime());
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
