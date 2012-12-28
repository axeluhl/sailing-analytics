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
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;

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
        plattner = createCompetitor("Prof. Dr. Hasso Plattner");
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
    	
    	track.lockForRead();
    	try {
    		assertEquals(Util.size(track.getRawFixes()), 0);
        	assertEquals(Util.size(track.getFixes()), 0);
    	} finally {
    		track.unlockAfterRead();
    	}
    }

}
