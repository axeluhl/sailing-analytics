package com.sap.sailing.racecommittee.app.ui.comparators;

import java.util.Comparator;

import com.sap.sailing.racecommittee.app.ui.adapters.racelist.RaceListDataTypeTitle;


public class RaceListDataTypeTitleComparator implements Comparator<RaceListDataTypeTitle> {

	public int compare(RaceListDataTypeTitle left, RaceListDataTypeTitle right) {
		
		if (left.getBoatClass().getName() == null || right.getBoatClass().getName() == null)
			return 0;
		
		int boatClassResult = left.getBoatClass().getName().compareTo(right.getBoatClass().getName());
		if (boatClassResult != 0)
			return boatClassResult;
		
		if (left.getSeries().getName() == null || right.getSeries().getName() == null)
			return 0;
		
		int seriesResult = left.getSeries().getOrderNumber().compareTo(right.getSeries().getOrderNumber());
		if (seriesResult != 0)
			return seriesResult;
		
		if (left.getFleet().getName() == null || right.getFleet().getName() == null)
			return 0;
			
		int groupResult = left.getFleet().getName().compareTo(right.getFleet().getName());
		return groupResult;
	}

}
