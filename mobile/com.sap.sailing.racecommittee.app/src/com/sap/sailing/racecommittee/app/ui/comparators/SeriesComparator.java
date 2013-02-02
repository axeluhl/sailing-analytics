package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.BoatClassSeriesDataFleet;

public class SeriesComparator implements Comparator<BoatClassSeriesDataFleet> {

	public int compare(BoatClassSeriesDataFleet lhs, BoatClassSeriesDataFleet rhs) {
		int result = lhs.getSeries().getOrderNumber().compareTo(rhs.getSeries().getOrderNumber());
		if (result == 0) {
			return lhs.getFleetName().compareTo(rhs.getFleetName());
		}
		return result;
	}

}
