package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.common.Named;

public class NamedRaceComparator extends RaceNameComparator implements Comparator<Named> {

	public int compare(Named lhs, Named rhs) {
		String left = lhs.getName();
		String right = rhs.getName();
		
		return compareRaceNames(left, right);
	}

}
