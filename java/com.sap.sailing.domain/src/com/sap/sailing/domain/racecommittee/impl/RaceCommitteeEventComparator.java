package com.sap.sailing.domain.racecommittee.impl;

import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;

/**
 * This comparator compares two timed objects, in this case two {@link RaceCommitteeEvent}s and is used for the race committee event track. The event on top of the list shall be the latest
 *
 */
public class RaceCommitteeEventComparator implements Comparator<Timed> {
	
	public static final Comparator<Timed> INSTANCE = new RaceCommitteeEventComparator();

	@Override
	public int compare(Timed left, Timed right) {
		return left.getTimePoint().compareTo(right.getTimePoint());
	}

}
