package com.sap.sailing.domain.racelog.impl;

import java.util.Comparator;

import com.sap.sailing.domain.base.Timed;

/**
 * This comparator compares two timed objects, in this case two {@link RaceLogEvent}s and is used for the race log. The event on top of the list shall be the latest
 *
 */
public class RaceLogEventComparator implements Comparator<Timed> {
	
	public static final Comparator<Timed> INSTANCE = new RaceLogEventComparator();

	@Override
	public int compare(Timed left, Timed right) {
		return left.getTimePoint().compareTo(right.getTimePoint());
	}

}
