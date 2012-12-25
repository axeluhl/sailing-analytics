package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeEventTrackImpl;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeFlagEventImpl;

public class RaceCommitteeTrackTest {
	
	@Test
	public void testAddOfEvent() {
		RaceCommitteeEventTrack rcEventTrack = new RaceCommitteeEventTrackImpl("RaceCommitteeEventTrack");
		TimePoint t1 = MillisecondsTimePoint.now();
		List<Competitor> competitors = new ArrayList<Competitor>();
		int passId = 0;
		boolean isDisplayed = true;
		RaceCommitteeFlagEvent rcEvent = new RaceCommitteeFlagEventImpl(t1, UUID.randomUUID(), competitors, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		rcEventTrack.add(rcEvent);
		
		rcEventTrack.lockForRead();
		try {
			Iterator<RaceCommitteeEvent> iterator = rcEventTrack.getFixes().iterator();
			int count = 0;
			
			do 
			{
				iterator.next();
				count++;
			} 
			while(iterator.hasNext());
			
			assertEquals(count, 1);
			
		} finally {
			rcEventTrack.unlockAfterRead();
		}
	}
}
