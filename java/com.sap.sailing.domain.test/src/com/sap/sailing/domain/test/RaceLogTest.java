package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;

public class RaceLogTest {
	
	@Test
	public void testAddOfEvent() {
		RaceLog rcEventTrack = new RaceLogImpl("RaceLogTest");
		TimePoint t1 = MillisecondsTimePoint.now();
		int passId = 0;
		boolean isDisplayed = true;
		RaceLogFlagEvent rcEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, isDisplayed);
		rcEventTrack.add(rcEvent);
		
		rcEventTrack.lockForRead();
		try {
			Iterator<RaceLogEvent> iterator = rcEventTrack.getFixes().iterator();
			int count = 0;
			
			do 
			{
				assertSame(iterator.next(), rcEvent);
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
		RaceLog rcEventTrack = new RaceLogImpl("RaceLog");
		List<RaceLogEvent> expectedOrderingList = new ArrayList<RaceLogEvent>();
		TimePoint t1 = MillisecondsTimePoint.now();
		TimePoint t2 = new MillisecondsTimePoint(t1.asMillis() - 1000);
		TimePoint t3 = new MillisecondsTimePoint(t1.asMillis() - 4000);
		TimePoint t4 = new MillisecondsTimePoint(t1.asMillis() - 5000);
		TimePoint t5 = new MillisecondsTimePoint(t1.asMillis() - 10000);
		
		TimePoint tx = new MillisecondsTimePoint(t1.asMillis() - 7000);
		
		int passId = 0;
		RaceLogFlagEvent rcEvent1 = RaceLogEventFactory.INSTANCE.createFlagEvent(t1, passId, Flags.CLASS, Flags.NONE, false);
		RaceLogFlagEvent rcEvent2 = RaceLogEventFactory.INSTANCE.createFlagEvent(t2, passId, Flags.PAPA, Flags.NONE, false);
		RaceLogFlagEvent rcEvent3 = RaceLogEventFactory.INSTANCE.createFlagEvent(t3, passId, Flags.PAPA, Flags.NONE, true);
		RaceLogFlagEvent rcEvent4 = RaceLogEventFactory.INSTANCE.createFlagEvent(t4, passId, Flags.CLASS, Flags.NONE, true);
		RaceLogStartTimeEvent rcEvent5 = RaceLogEventFactory.INSTANCE.createStartTimeEvent(t5, passId, t1);
		
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
			
			Iterator<RaceLogEvent> iterator = rcEventTrack.getFixes().iterator();
			checkOrderingAndListLength(expectedOrderingList, iterator, 0, 5);
			
			iterator = rcEventTrack.getRawFixes().iterator();
			checkOrderingAndListLength(expectedOrderingList, iterator, 0, 5);
			
			RaceLogEvent event = rcEventTrack.getFirstRawFix();
			checkEquality(rcEvent5, event);
			
			event = rcEventTrack.getLastRawFix();
			checkEquality(rcEvent1, event);
			
			event = rcEventTrack.getFirstFixAfter(t3);
			checkEquality(rcEvent2, event);
			
			event = rcEventTrack.getFirstFixAtOrAfter(t3);
			checkEquality(rcEvent3, event);
			
			event = rcEventTrack.getFirstRawFixAfter(t2);
			checkEquality(rcEvent1, event);
			
			event = rcEventTrack.getFirstRawFixAtOrAfter(t2);
			checkEquality(rcEvent2, event);
			
			event = rcEventTrack.getLastFixBefore(t3);
			checkEquality(rcEvent4, event);
			
			event = rcEventTrack.getLastFixAtOrBefore(t3);
			checkEquality(rcEvent3, event);
			
			event = rcEventTrack.getLastRawFixBefore(t4);
			checkEquality(rcEvent5, event);
			
			event = rcEventTrack.getLastRawFixAtOrBefore(t4);
			checkEquality(rcEvent4, event);
			
			iterator = rcEventTrack.getFixesIterator(tx, true);
			checkOrderingAndListLength(expectedOrderingList, iterator, 1, 4);
			
			iterator = rcEventTrack.getRawFixesIterator(tx, true);
			checkOrderingAndListLength(expectedOrderingList, iterator, 1, 4);
			
			
		} finally {
			rcEventTrack.unlockAfterRead();
		}
	}

	private void checkOrderingAndListLength(List<RaceLogEvent> expectedOrderingList, Iterator<RaceLogEvent> iterator, int countOffset, int expectedLength) {
		int count = 0;
		
		while(iterator.hasNext()) 
		{
			RaceLogEvent event = iterator.next();
			assertEquals(event, expectedOrderingList.get(count + countOffset));
			count++;
		} 
		
		
		//for Iterables the size of the list can also be retrieved via Util.size(Iterable<T> i)
		assertEquals(count, expectedLength);
	}
	

	private void checkEquality(RaceLogEvent knownRcEvent, RaceLogEvent trackEvent) {
		if (trackEvent != null) {
			assertEquals(trackEvent, knownRcEvent);
		} else {
			fail("Returned event was null");
		}
	}
}
