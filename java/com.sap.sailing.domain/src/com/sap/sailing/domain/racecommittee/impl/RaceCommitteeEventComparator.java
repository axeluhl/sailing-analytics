package com.sap.sailing.domain.racecommittee.impl;

import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;

/**
 * This comparator compares two timed objects, in this case two {@link RaceCommitteeEvent}s and is used for the event track. The newest event shall be on top of the list.
 *
 */
public class RaceCommitteeEventComparator implements Comparator<Timed> {
	
	public static final Comparator<Timed> INSTANCE = new RaceCommitteeEventComparator();

	@Override
	public int compare(Timed left, Timed right) {
		return (-1)*left.getTimePoint().compareTo(right.getTimePoint());
	}

}
