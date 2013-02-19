package com.sap.sailing.domain.base.racegroup;

import com.sap.sailing.domain.base.SeriesData;


public interface SeriesWithRows extends SeriesData {

	public Iterable<RaceRow> getRaceRows();
	
}
