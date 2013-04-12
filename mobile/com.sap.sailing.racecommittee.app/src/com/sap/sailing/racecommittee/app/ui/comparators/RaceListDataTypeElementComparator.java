package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeElement;

public class RaceListDataTypeElementComparator extends RaceNameComparator implements Comparator<RaceListDataTypeElement>  {
	
	public int compare(RaceListDataTypeElement lhs, RaceListDataTypeElement rhs) {
		String leftRaceName = lhs.getRaceName();
		String rightRaceName = rhs.getRaceName();
		
		return compareRaceNames(leftRaceName, rightRaceName);
	}

}
