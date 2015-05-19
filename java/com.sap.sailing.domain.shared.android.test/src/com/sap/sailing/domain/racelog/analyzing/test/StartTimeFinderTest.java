package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogDependentStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderStatus;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class StartTimeFinderTest extends
        PassAwareRaceLogAnalyzerTest<StartTimeFinder, Pair<StartTimeFinderStatus, TimePoint>> {

    @Override
    protected StartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new StartTimeFinder(raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, AbstractLogEventAuthor author) {
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1, passId, author);
        when(event.getStartTime()).thenReturn(mock(TimePoint.class));
        return new TargetPair(Arrays.asList(event), new Pair<StartTimeFinderStatus, TimePoint>(
                StartTimeFinderStatus.STARTTIME_FOUND, event.getStartTime()));
    }

    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertEquals(new Pair<StartTimeFinderStatus, TimePoint>(StartTimeFinderStatus.STARTTIME_UNKNOWN, null),
                analyzer.analyze());
    }

    @Test
    public void testMostRecent() {
        RaceLogStartTimeEvent event1 = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event1.getStartTime()).thenReturn(mock(TimePoint.class));
        RaceLogStartTimeEvent event2 = createEvent(RaceLogStartTimeEvent.class, 2);
        when(event2.getStartTime()).thenReturn(mock(TimePoint.class));

        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(StartTimeFinderStatus.STARTTIME_FOUND, analyzer.analyze().getA());
        assertEquals(event2.getStartTime(), analyzer.analyze().getB());
    }
    
    @Test
    public void testDependent() {
        RaceLogStartTimeEvent event1 = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event1.getStartTime()).thenReturn(mock(TimePoint.class));
        RaceLogDependentStartTimeEvent event2 = createEvent(RaceLogDependentStartTimeEvent.class, 2);

        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(StartTimeFinderStatus.STARTTIME_DEPENDENT, analyzer.analyze().getA());
        assertNull(analyzer.analyze().getB());
    }
}
