package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.utils.AlphanumComparator;

public abstract class RaceNameComparator {
	
	private Comparator<String> comparator;

	public RaceNameComparator() {
		this.comparator = new AlphanumComparator();
	}

	/**
	 * compares two race names and returns the ordinal comparison result
	 * @param leftRaceName
	 * @param rightRaceName
	 * @return the comparison result (see String.compareTo)
	 */
	protected int compareRaceNames(String leftRaceName, String rightRaceName) {
		return comparator.compare(leftRaceName, rightRaceName);
	}

}