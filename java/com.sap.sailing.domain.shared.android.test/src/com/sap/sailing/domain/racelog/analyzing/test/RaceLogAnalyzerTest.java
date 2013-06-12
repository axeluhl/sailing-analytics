package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogProtestStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public class RaceLogAnalyzerTest {

    private RaceLog raceLog;

    private <T extends RaceLogEvent> T createEvent(Class<T> type) {
        return createEvent(type, 0, 0);
    }
    
    private <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds) {
        return createEvent(type, milliseconds, 0);
    }
    
    private <T extends RaceLogEvent> T createEvent(Class<T> type, long milliseconds, int passId) {
        T event = mock(type);
        when(event.getPassId()).thenReturn(passId);
        when(event.getTimePoint()).thenReturn(new MillisecondsTimePoint(milliseconds));
        when(event.getId()).thenReturn("a");
        return event;
    }

    @Before
    public void setUp() {
        raceLog = new RaceLogImpl("RaceLogTest", "test-identifier");
    }

    @Test
    public void testProtestStartTimeFinder() {
        ProtestStartTimeFinder analyzer = new ProtestStartTimeFinder(raceLog);
        
        RaceLogProtestStartTimeEvent event1 = createEvent(RaceLogProtestStartTimeEvent.class);
        when(event1.getProtestStartTime()).thenReturn(mock(TimePoint.class));
        raceLog.add(event1);

        raceLog.lockForRead();
        try {
            assertEquals(event1.getProtestStartTime(), analyzer.getProtestStartTime());
        } finally {
            raceLog.unlockAfterRead();
        }
    }

    @Test
    public void testLastFlagFinder() {
        LastFlagFinder analyzer = new LastFlagFinder(raceLog);
        
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2);
        
        raceLog.add(event1);
        raceLog.add(event2);

        raceLog.lockForRead();
        try {
            assertEquals(event2, analyzer.getLastFlagEvent());
        } finally {
            raceLog.unlockAfterRead();
        }
    }

}
