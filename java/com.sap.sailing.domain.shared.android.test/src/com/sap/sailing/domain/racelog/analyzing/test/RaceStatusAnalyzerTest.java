package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceStatusAnalyzerTest extends PassAwareRaceLogAnalyzerTest<RaceStatusAnalyzer, RaceLogRaceStatus> {

    private RacingProcedure racingProcedure = mock(RacingProcedure.class);
    
    @Override
    protected RaceStatusAnalyzer createAnalyzer(RaceLog raceLog) {
        return new RaceStatusAnalyzer(raceLog, racingProcedure);
    }

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(int passId, RaceLogEventAuthor author) {
        RaceLogRaceStatusEvent event = createEvent(RaceLogRaceStatusEvent.class, 1, passId, author);
        when(event.getNextStatus()).thenReturn(RaceLogRaceStatus.RUNNING);
        doAnswer(new StatusVisitorAnswer()).when(event).accept(any(RaceLogEventVisitor.class));
        return new TargetPair(Arrays.asList(event), event.getNextStatus());
    }
    
    @Override
    protected TargetPair getBlockingEventsAndResultForPassAwareTests(
            int passId, RaceLogEventAuthor author) {
        RaceLogRaceStatusEvent event = createEvent(RaceLogRaceStatusEvent.class, 1, passId, author);
        when(event.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        doAnswer(new StatusVisitorAnswer()).when(event).accept(any(RaceLogEventVisitor.class));
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
        when(event1.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2);
        when(event2.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        doAnswer(new StatusVisitorAnswer()).when(event2).accept(any(RaceLogEventVisitor.class));
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getNextStatus(), analyzer.analyze());
    }
    
    @Test
    public void testStartphaseNotYetActive() {
        when(racingProcedure.isStartphaseActive(any(TimePoint.class), any(TimePoint.class))).thenReturn(false);
        
        RaceLogStartTimeEvent event = createStartTimeEvent(MillisecondsTimePoint.now().plus(20000).asMillis(), true);
        raceLog.add(event);
        
        assertEquals(RaceLogRaceStatus.SCHEDULED, analyzer.analyze());
    }
    
    @Test
    public void testStartphaseActive() {
        when(racingProcedure.isStartphaseActive(any(TimePoint.class), any(TimePoint.class))).thenReturn(true);
        
        RaceLogStartTimeEvent event = createStartTimeEvent(MillisecondsTimePoint.now().plus(20000).asMillis(), true);
        raceLog.add(event);
        
        assertEquals(RaceLogRaceStatus.STARTPHASE, analyzer.analyze());
    }
    
    @Test
    public void testStartTimePassed() {
        when(racingProcedure.isStartphaseActive(any(TimePoint.class), any(TimePoint.class))).thenReturn(false);
        
        RaceLogStartTimeEvent event = createStartTimeEvent(0, false);
        raceLog.add(event);
        
        assertEquals(RaceLogRaceStatus.RUNNING, analyzer.analyze());
    }
    
    @Test
    public void testClock() {
        final TimePoint startTime = MillisecondsTimePoint.now(); 
        analyzer = new RaceStatusAnalyzer(raceLog, new RaceStatusAnalyzer.Clock() {
            @Override
            public TimePoint now() {
                return startTime.minus(1);
            }
        }, racingProcedure);
        
        when(racingProcedure.isStartphaseActive(any(TimePoint.class), any(TimePoint.class))).thenReturn(false);
        
        RaceLogStartTimeEvent event = createStartTimeEvent(startTime);
        raceLog.add(event);
        
        assertEquals(RaceLogRaceStatus.SCHEDULED, analyzer.analyze());
    }

    @Test
    @Override
    public void testPassAwareHidingMinorAuthor() {
        // makes no sense here as the status events are not of the same type - redirect to testStartSetByStartVesselAndFinishSetByShoreControl()
        testStartSetByStartVesselAndFinishSetByShoreControl();
    }

    public void testStartSetByStartVesselAndFinishSetByShoreControl() {
        RaceLogEventAuthor authorStartVessel = new RaceLogEventAuthorImpl("Race Officer on Finish Vessel", 1);
        RaceLogEventAuthor authorShoreControl = new RaceLogEventAuthorImpl("Shore Control", 2);
        
        RaceLogRaceStatusEvent event1 = createEvent(RaceLogRaceStatusEvent.class, 1, 1 /** passId*/, authorStartVessel);
        when(event1.getNextStatus()).thenReturn(RaceLogRaceStatus.SCHEDULED);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2, 1 /** passId*/, authorShoreControl);
        when(event2.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        doAnswer(new StatusVisitorAnswer()).when(event2).accept(any(RaceLogEventVisitor.class));
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getNextStatus(), analyzer.analyze());
    }

    private static RaceLogStartTimeEvent createStartTimeEvent(long startTimeAsMillis, boolean isGreater) {
        TimePoint startTime = mock(TimePoint.class);
        when(startTime.asMillis()).thenReturn(startTimeAsMillis);
        when(startTime.compareTo(any(TimePoint.class))).thenReturn(isGreater ? 1 : -1);
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event.getStartTime()).thenReturn(startTime);
        doAnswer(new StartTimeVisitorAnswer()).when(event).accept(any(RaceLogEventVisitor.class));
        return event;
    }
    
    private static RaceLogStartTimeEvent createStartTimeEvent(TimePoint startTime) {
        RaceLogStartTimeEvent event = createEvent(RaceLogStartTimeEvent.class, 1);
        when(event.getStartTime()).thenReturn(startTime);
        doAnswer(new StartTimeVisitorAnswer()).when(event).accept(any(RaceLogEventVisitor.class));
        return event;
    }
    
    private static class StartTimeVisitorAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            RaceLogStartTimeEvent event = (RaceLogStartTimeEvent) invocation.getMock();
            RaceLogEventVisitor visitor = (RaceLogEventVisitor) invocation.getArguments()[0];
            visitor.visit(event);
            return null;
        }
    }
    
    private static class StatusVisitorAnswer implements Answer<Void> {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            RaceLogRaceStatusEvent event = (RaceLogRaceStatusEvent) invocation.getMock();
            RaceLogEventVisitor visitor = (RaceLogEventVisitor) invocation.getArguments()[0];
            visitor.visit(event);
            return null;
        }
    }
}
