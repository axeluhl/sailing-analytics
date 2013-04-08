package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;

public class RaceListDataTypeTitle extends RaceListDataType {

	private BoatClassSeriesDataFleet data;
	
	public RaceListDataTypeTitle(BoatClassSeriesDataFleet data) {
		this.data = data;
	}
	
	public BoatClassSeriesDataFleet getBoatClassSeriesDataFleet() {
		return data;
	}

	public BoatClass getBoatClass() {
		return data.getBoatClass();
	}
	
	public SeriesBase getSeries() {
		return data.getSeries();
	}

	public Fleet getFleet() {
		return data.getFleet();
	}

	public String toString() {
		return getBoatClass().getName() + " - " + getSeries().getName() + " - " + getFleet().getName();
	}
}
