package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventFactory;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;

public class RaceCommitteeEventsOnConstructedTrackedRaceTest extends TrackBasedTest {
	
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
    
    @Test
    public void testRaceCommitteeEventTrackExists() {
    	RaceCommitteeEventTrack track = getTrackedRace().getOrCreateRaceCommitteeEventTrack();
    	assertNotNull(track);
    	
    	track.lockForRead();
    	try {
    		assertEquals(Util.size(track.getRawFixes()), 0);
        	assertEquals(Util.size(track.getFixes()), 0);
    	} finally {
    		track.unlockAfterRead();
    	}
    }
    
    @Test
    public void testRaceCommitteeEventTrackInsertSomeEvents() {
    	RaceCommitteeEventTrack track = getTrackedRace().getOrCreateRaceCommitteeEventTrack();
    	assertNotNull(track);
    	
    	TimePoint t1 = MillisecondsTimePoint.now();
		TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() - 1000);
		TimePoint t3 = new MillisecondsTimePoint(t1.asMillis() - 4000);
		TimePoint t4 = new MillisecondsTimePoint(t1.asMillis() - 5000);
		TimePoint t5 = new MillisecondsTimePoint(t1.asMillis() - 10000);
		
		int passId = 0;
		RaceCommitteeFlagEvent rcEvent1 = RaceCommitteeEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, false);
		RaceCommitteeFlagEvent rcEvent2 = RaceCommitteeEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.PAPA, Flags.NONE, false);
		RaceCommitteeFlagEvent rcEvent3 = RaceCommitteeEventFactory.INSTANCE.createFlagEvent(t3, passId, Flags.PAPA, Flags.NONE, true);
		RaceCommitteeFlagEvent rcEvent4 = RaceCommitteeEventFactory.INSTANCE.createFlagEvent(t4, passId, Flags.CLASS, Flags.NONE, true);
		RaceCommitteeStartTimeEvent rcEvent5 = RaceCommitteeEventFactory.INSTANCE.createStartTimeEvent(t5, passId, t1);
		
		getTrackedRace().recordRaceCommitteeEvent(rcEvent1);
		getTrackedRace().recordRaceCommitteeEvent(rcEvent2);
		getTrackedRace().recordRaceCommitteeEvent(rcEvent3);
		getTrackedRace().recordRaceCommitteeEvent(rcEvent4);
		getTrackedRace().recordRaceCommitteeEvent(rcEvent5);
		
    	track.lockForRead();
    	try {
    		assertEquals(Util.size(track.getRawFixes()), 5);
        	assertEquals(Util.size(track.getFixes()), 5);
    	} finally {
    		track.unlockAfterRead();
    	}
    }

}
