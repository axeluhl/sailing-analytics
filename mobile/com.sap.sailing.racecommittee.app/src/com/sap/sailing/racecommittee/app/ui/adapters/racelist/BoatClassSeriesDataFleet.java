package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesData;

public class BoatClassSeriesDataFleet {
	
	private BoatClass boatClass;
	private SeriesData series;
	private Fleet fleet;
	
	public BoatClassSeriesDataFleet(BoatClass boatClass, SeriesData series, Fleet fleet) {
		this.boatClass = boatClass;
		this.series = series;
		this.fleet = fleet;
	}

	public String getBoatClassName() {
		return boatClass.getName();
	}

	public String getFleetName() {
		return fleet.getName();
	}

	public String getSeriesName() {
		return series.getName();
	}
	
	public BoatClass getBoatClass() {
		return boatClass;
	}
	
	public SeriesData getSeries() {
		return series;
	}
	
	public Fleet getFleet() {
		return fleet;
	}
	
	@Override
	public boolean equals(Object obj) {
		BoatClassSeriesDataFleet other = (BoatClassSeriesDataFleet) obj;
		return getBoatClassName().equals(other.getBoatClassName()) 
				&& getSeriesName().equals(other.getSeriesName()) 
				&& getFleetName().equals(other.getFleetName());
	}
	
	@Override
    public int hashCode() {
		/// TODO: Check implementation of equals/hashCode on BoatGroupAndSeries
        return 123;
    }
}
