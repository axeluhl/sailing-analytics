package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.common.TimePoint;

public class ProtestStartTimeFinderTest extends PassAwareRaceLogAnalyzerTest<ProtestStartTimeFinder, TimePoint> {

    @Override
    protected ProtestStartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new ProtestStartTimeFinder(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, RaceLogEventAuthor author) {
        RaceLogProtestStartTimeEvent event = createEvent(RaceLogProtestStartTimeEvent.class, 1, passId, author);
        when(event.getProtestStartTime()).thenReturn(mock(TimePoint.class));
        return new TargetPair(Arrays.asList(event), event.getProtestStartTime());
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

        assertEquals(event2.getProtestStartTime(), analyzer.analyze());
    }
}
