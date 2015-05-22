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
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.DependentStartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinderStatus;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class DependentStartTimeFinderTest extends
        PassAwareRaceLogAnalyzerTest<DependentStartTimeFinder, TimePoint> {

    @Override
    protected DependentStartTimeFinder createAnalyzer(RaceLog raceLog) {
        return new DependentStartTimeFinder(mock(RaceLogResolver.class), raceLog);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, AbstractLogEventAuthor author) {
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1, passId, author);
        when(event.getStartTime()).thenReturn(mock(TimePoint.class));
        return new TargetPair(Arrays.asList(event), event.getStartTime());
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

        assertEquals(event2.getStartTime(), analyzer.analyze());
    }
    
    @Test
    public void testDependent() {
        RaceLogStartTimeEvent event1 = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event1.getStartTime()).thenReturn(mock(TimePoint.class));
        RaceLogDependentStartTimeEvent event2 = createEvent(RaceLogDependentStartTimeEvent.class, 2);

        raceLog.add(event1);
        raceLog.add(event2);

        assertNull(analyzer.analyze());
    }
}
