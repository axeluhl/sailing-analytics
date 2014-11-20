package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Test;

import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.IndividualRecallFinder;
import com.sap.sse.common.TimePoint;

public class IndividualRecallFinderTest extends PassAwareRaceLogAnalyzerTest<IndividualRecallFinder, TimePoint> {

    @Override
    protected TargetPair getTargetEventsAndResultForPassAwareTests(
            int passId, RaceLogEventAuthor author) {
        RaceLogFlagEvent event = createEvent(RaceLogFlagEvent.class, 1, passId, author);
        when(event.getUpperFlag()).thenReturn(Flags.XRAY);
        when(event.isDisplayed()).thenReturn(true);
        return new TargetPair(Arrays.asList(event), event.getLogicalTimePoint());
    }

    @Override
    protected IndividualRecallFinder createAnalyzer(RaceLog raceLog) {
        return new IndividualRecallFinder(raceLog) {
            @Override
            protected boolean isRelevant(RaceLogFlagEvent flagEvent) {
                return flagEvent.getUpperFlag().equals(Flags.XRAY) && flagEvent.isDisplayed();
            }
        };
    }
    
    @Test
    public void testNullForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);
        assertNull(analyzer.analyze());
    }
    
    @Test
    public void testMostRecent() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        when(event1.getUpperFlag()).thenReturn(Flags.XRAY);
        when(event1.isDisplayed()).thenReturn(true);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2);
        when(event2.getUpperFlag()).thenReturn(Flags.XRAY);
        when(event2.isDisplayed()).thenReturn(true);
        
        raceLog.add(event1);
        raceLog.add(event2);

        assertEquals(event2.getLogicalTimePoint(), analyzer.analyze());
    }

}
