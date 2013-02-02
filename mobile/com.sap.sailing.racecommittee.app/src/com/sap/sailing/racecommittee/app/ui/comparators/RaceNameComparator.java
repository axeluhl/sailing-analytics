package com.sap.sailing.racecommittee.app.ui.comparators;

public abstract class RaceNameComparator {

	public RaceNameComparator() {
		super();
	}

	/**
	 * compares two race names and returns the ordinal comparison result
	 * @param leftRaceName
	 * @param rightRaceName
	 * @return the comparison result (see String.compareTo)
	 */
	protected int compareRaceNames(String leftRaceName, String rightRaceName) {
		leftRaceName = leftRaceName.replace("Race ", "");
		rightRaceName = rightRaceName.replace("Race ", "");
		
		Integer leftRaceNumber = Integer.valueOf(leftRaceName);
		Integer rightRaceNumber = Integer.valueOf(rightRaceName);
		
		return leftRaceNumber.compareTo(rightRaceNumber);
	}

}