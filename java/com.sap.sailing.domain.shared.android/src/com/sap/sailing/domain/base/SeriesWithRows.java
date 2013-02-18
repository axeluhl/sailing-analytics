package com.sap.sailing.domain.base;


public interface SeriesWithRows extends SeriesData {

	public Iterable<RaceRow> getRaceRows();
	
}
