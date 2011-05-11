package com.sap.sailing.domain.base.impl;


public class DegreePosition extends AbstractPosition {
	private final double lat;
	private final double lng;
	
	public DegreePosition(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	@Override
	public double getLatDeg() {
		return lat;
	}

	@Override
	public double getLngDeg() {
		return lng;
	}

}
