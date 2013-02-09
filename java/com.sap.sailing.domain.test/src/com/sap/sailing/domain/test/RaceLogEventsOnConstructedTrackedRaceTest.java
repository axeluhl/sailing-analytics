package com.sap.sailing.domain.test;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

public class RaceLogEventsOnConstructedTrackedRaceTest extends TrackBasedTest {
	
	private List<Competitor> competitors;
    private CompetitorImpl plattner;
    private CompetitorImpl hunger;
    private MillisecondsTimePoint start;
	
	/**
     * Creates the race and two competitors ({@link #plattner} and {@link #hunger}) and sets the start line passing for both of them
     * to {@link #start}.
     */
    @Before
    public void setUp() {
        competitors = new ArrayList<Competitor>();
        hunger = createCompetitor("Wolfgang Hunger");
        competitors.add(hunger);
        plattner = createCompetitor("Prof. Dr. hc. Hasso Plattner");
        competitors.add(plattner);
        start = new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime());
        setTrackedRace(createTestTrackedRace("Kieler Woche", "505 Race 2", "505", competitors, start));
    }
    
    @Ignore
    public void testRaceLogExists() {
//    	RaceLog track = getTrackedRace().getRaceLog();
//    	assertNotNull(track);
//    	
//    	track.lockForRead();
//    	try {
//    		assertEquals(Util.size(track.getRawFixes()), 0);
//        	assertEquals(Util.size(track.getFixes()), 0);
//    	} finally {
//    		track.unlockAfterRead();
//    	}
    }
    
    @Ignore
    public void testRaceLogInsertSomeEvents() {
//    	RaceLog track = getTrackedRace().getRaceLog();
//    	assertNotNull(track);
//    	
//    	TimePoint t1 = MillisecondsTimePoint.now();
//		TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() - 1000);
//		TimePoint t3 = new MillisecondsTimePoint(t1.asMillis() - 4000);
//		TimePoint t4 = new MillisecondsTimePoint(t1.asMillis() - 5000);
//		TimePoint t5 = new MillisecondsTimePoint(t1.asMillis() - 10000);
//		
//		int passId = 0;
//		RaceLogFlagEvent rcEvent1 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, false);
//		RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.PAPA, Flags.NONE, false);
//		RaceLogFlagEvent rcEvent3 = RaceLogEventFactory.INSTANCE.createFlagEvent(t3, passId, Flags.PAPA, Flags.NONE, true);
//		RaceLogFlagEvent rcEvent4 = RaceLogEventFactory.INSTANCE.createFlagEvent(t4, passId, Flags.CLASS, Flags.NONE, true);
//		RaceLogStartTimeEvent rcEvent5 = RaceLogEventFactory.INSTANCE.createStartTimeEvent(t5, passId, t1);
//		
//		getTrackedRace().recordRaceLogEvent(rcEvent1);
//		getTrackedRace().recordRaceLogEvent(rcEvent2);
//		getTrackedRace().recordRaceLogEvent(rcEvent3);
//		getTrackedRace().recordRaceLogEvent(rcEvent4);
//		getTrackedRace().recordRaceLogEvent(rcEvent5);
//		
//    	track.lockForRead();
//    	try {
//    		assertEquals(Util.size(track.getRawFixes()), 5);
//        	assertEquals(Util.size(track.getFixes()), 5);
//    	} finally {
//    		track.unlockAfterRead();
//    	}
    }

}
