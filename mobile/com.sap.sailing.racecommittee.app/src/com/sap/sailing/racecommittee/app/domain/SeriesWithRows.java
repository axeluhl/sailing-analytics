package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.RaceRow;
import com.sap.sailing.domain.base.SeriesData;

public interface SeriesWithRows extends SeriesData {

	public Iterable<RaceRow> getRaceRows();
	
}
