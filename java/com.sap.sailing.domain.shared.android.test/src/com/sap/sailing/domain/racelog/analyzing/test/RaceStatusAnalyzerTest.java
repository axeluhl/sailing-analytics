package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
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
        when(event1.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHING);
        RaceLogRaceStatusEvent event2 = createEvent(RaceLogRaceStatusEvent.class, 2);
        when(event2.getNextStatus()).thenReturn(RaceLogRaceStatus.FINISHED);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                RaceLogEventVisitor visitor = (RaceLogEventVisitor) invocation.getArguments()[0];
                RaceLogRaceStatusEvent event = (RaceLogRaceStatusEvent) invocation.getMock();
                visitor.visit(event);
                return null;
            };
        }).when(event2).accept(any(RaceLogEventVisitor.class));
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getNextStatus(), analyzer.analyze());
    }
}
