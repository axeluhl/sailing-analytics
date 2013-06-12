package com.sap.sailing.domain.racelog.analyzing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagsFinder;

public class LastFlagsFinderTest extends RaceLogAnalyzerTest<LastFlagsFinder> {

    @Override
    protected LastFlagsFinder createAnalyzer(RaceLog raceLog) {
        return new LastFlagsFinder(raceLog);
    }

    @Test
    public void testEmptyListForNone() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        raceLog.add(event1);

        List<RaceLogFlagEvent> result = analyzer.analyze(); 
        assertNotNull(result);
        assertEquals(0, result.size());
    }
    
    @Test
    public void testSingleLast() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2);
        
        raceLog.add(event1);
        raceLog.add(event2);

        List<RaceLogFlagEvent> result = analyzer.analyze();
        assertEquals(1, result.size());
        assertEquals(event2, result.get(0));
    }
    
    @Test
    public void testSingleInBetween() {
        RaceLogEvent event1 = createEvent(RaceLogEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2);
        RaceLogEvent event3 = createEvent(RaceLogEvent.class, 3);
        
        raceLog.add(event1);
        raceLog.add(event2);
        raceLog.add(event3);

        List<RaceLogFlagEvent> result = analyzer.analyze();
        assertEquals(1, result.size());
        assertEquals(event2, result.get(0));
    }
    
    @Test
    public void testTwoLast() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2, "a");
        RaceLogFlagEvent event3 = createEvent(RaceLogFlagEvent.class, 2, "b");
        
        raceLog.add(event1);
        raceLog.add(event2);
        raceLog.add(event3);

        List<RaceLogFlagEvent> result = analyzer.analyze();
        assertEquals(2, result.size());
        assertEquals(event3, result.get(0));
        assertEquals(event2, result.get(1));
    }    
    
    @Test
    public void testTwoLastWithInBetween() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2, "a");
        RaceLogEvent event3 = createEvent(RaceLogEvent.class, 2, "b");
        RaceLogFlagEvent event4 = createEvent(RaceLogFlagEvent.class, 2, "c");
        
        raceLog.add(event1);
        raceLog.add(event2);
        raceLog.add(event3);
        raceLog.add(event4);

        List<RaceLogFlagEvent> result = analyzer.analyze();
        assertEquals(2, result.size());
        assertEquals(event4, result.get(0));
        assertEquals(event2, result.get(1));
    }
    
    @Test
    public void testMoreThanTwoLast() {
        RaceLogFlagEvent event1 = createEvent(RaceLogFlagEvent.class, 1);
        RaceLogFlagEvent event2 = createEvent(RaceLogFlagEvent.class, 2, "a");
        RaceLogFlagEvent event3 = createEvent(RaceLogFlagEvent.class, 2, "b");
        RaceLogFlagEvent event4 = createEvent(RaceLogFlagEvent.class, 2, "c");
        
        raceLog.add(event1);
        raceLog.add(event2);
        raceLog.add(event3);
        raceLog.add(event4);

        List<RaceLogFlagEvent> result = analyzer.analyze();
        assertEquals(3, result.size());
        assertEquals(event4, result.get(0));
        assertEquals(event3, result.get(1));
        assertEquals(event2, result.get(2));
    }
}
