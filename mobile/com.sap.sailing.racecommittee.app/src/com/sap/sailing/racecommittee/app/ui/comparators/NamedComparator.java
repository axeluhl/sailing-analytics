package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.domain.common.Named;

public class NamedComparator implements Comparator<Named> {

	public int compare(Named lhs, Named rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}

}
