package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeEventTrackImpl;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeFlagEventImpl;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeStartTimeEventImpl;

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
	
	@Test
	public void testTrackAPIForEventLogUsage() {
		RaceCommitteeEventTrack rcEventTrack = new RaceCommitteeEventTrackImpl("RaceCommitteeEventTrack");
		List<RaceCommitteeEvent> expectedOrderingList = new ArrayList<RaceCommitteeEvent>();
		TimePoint t1 = MillisecondsTimePoint.now();
		TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() - 1000);
		TimePoint t3 = new MillisecondsTimePoint(t1.asMillis() - 4000);
		TimePoint t4 = new MillisecondsTimePoint(t1.asMillis() - 5000);
		TimePoint t5 = new MillisecondsTimePoint(t1.asMillis() - 10000);
		
		TimePoint tx = new MillisecondsTimePoint(t1.asMillis() - 7000);
		
		List<Competitor> competitors = new ArrayList<Competitor>();
		int passId = 0;
		RaceCommitteeFlagEvent rcEvent1 = new RaceCommitteeFlagEventImpl(t1, UUID.randomUUID(), competitors, passId, Flags.CLASS, Flags.NONE, false);
		RaceCommitteeFlagEvent rcEvent2 = new RaceCommitteeFlagEventImpl(t2, UUID.randomUUID(), competitors, passId, Flags.PAPA, Flags.NONE, false);
		RaceCommitteeFlagEvent rcEvent3 = new RaceCommitteeFlagEventImpl(t3, UUID.randomUUID(), competitors, passId, Flags.PAPA, Flags.NONE, true);
		RaceCommitteeFlagEvent rcEvent4 = new RaceCommitteeFlagEventImpl(t4, UUID.randomUUID(), competitors, passId, Flags.CLASS, Flags.NONE, true);
		RaceCommitteeStartTimeEvent rcEvent5 = new RaceCommitteeStartTimeEventImpl(t5, UUID.randomUUID(), competitors, passId, t1);
		
		rcEventTrack.add(rcEvent5);
		rcEventTrack.add(rcEvent3);
		rcEventTrack.add(rcEvent2);
		rcEventTrack.add(rcEvent1);
		rcEventTrack.add(rcEvent4);
		
		expectedOrderingList.add(rcEvent5);
		expectedOrderingList.add(rcEvent4);
		expectedOrderingList.add(rcEvent3);
		expectedOrderingList.add(rcEvent2);
		expectedOrderingList.add(rcEvent1);
		
		rcEventTrack.lockForRead();
		try {
			
			Iterator<RaceCommitteeEvent> iterator = rcEventTrack.getFixes().iterator();
			int count = 0;
			
			do 
			{
				RaceCommitteeEvent event = iterator.next();
				assertEquals(event, expectedOrderingList.get(count));
				count++;
			} 
			while(iterator.hasNext());
			
			assertEquals(count, 5);
			
			iterator = rcEventTrack.getRawFixes().iterator();
			count = 0;
			
			do 
			{
				RaceCommitteeEvent event = iterator.next();
				assertEquals(event, expectedOrderingList.get(count));
				count++;
			} 
			while(iterator.hasNext());
			
			assertEquals(count, 5);
			
			RaceCommitteeEvent event = rcEventTrack.getFirstRawFix();
			if (event != null) {
				assertEquals(event, rcEvent5);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getLastRawFix();
			if (event != null) {
				assertEquals(event, rcEvent1);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getFirstFixAfter(t3);
			if (event != null) {
				assertEquals(event, rcEvent2);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getFirstFixAtOrAfter(t3);
			if (event != null) {
				assertEquals(event, rcEvent3);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getFirstRawFixAfter(t2);
			if (event != null) {
				assertEquals(event, rcEvent1);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getFirstRawFixAtOrAfter(t2);
			if (event != null) {
				assertEquals(event, rcEvent2);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getLastFixBefore(t3);
			if (event != null) {
				assertEquals(event, rcEvent4);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getLastFixAtOrBefore(t3);
			if (event != null) {
				assertEquals(event, rcEvent3);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getLastRawFixBefore(t4);
			if (event != null) {
				assertEquals(event, rcEvent5);
			} else {
				fail("Returned event was null");
			}
			
			event = rcEventTrack.getLastRawFixAtOrBefore(t4);
			if (event != null) {
				assertEquals(event, rcEvent4);
			} else {
				fail("Returned event was null");
			}
			
			iterator = rcEventTrack.getFixesIterator(tx, true);
			count = 0;
			
			do 
			{
				event = iterator.next();
				assertEquals(event, expectedOrderingList.get(count + 1));
				count++;
			} 
			while(iterator.hasNext());
			
			assertEquals(count, 4);
			
			iterator = rcEventTrack.getRawFixesIterator(tx, true);
			count = 0;
			
			do 
			{
				event = iterator.next();
				assertEquals(event, expectedOrderingList.get(count + 1));
				count++;
			} 
			while(iterator.hasNext());
			
			assertEquals(count, 4);
			
			
		} finally {
			rcEventTrack.unlockAfterRead();
		}
	}
}
