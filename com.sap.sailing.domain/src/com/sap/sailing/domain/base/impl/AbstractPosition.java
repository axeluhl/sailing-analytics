package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Position;

public class AbstractPosition implements Position {
	@Override
	public double getLngRad() {
		return getLngDeg() / 180 * Math.PI;
	}
	
	@Override
	public double getLatRad() {
		return getLatDeg() / 180 * Math.PI;
	}

	@Override
	public double getLatDeg() {
		return getLatRad() / Math.PI * 180;
	}

	@Override
	public double getLngDeg() {
		return getLngRad() / Math.PI * 180;
	}

	@Override
	public double getCentralAngleRad(Position p) {
		return Math.acos(Math.sin(getLatRad())*Math.sin(p.getLatRad()) +
				Math.cos(getLatRad())*Math.cos(p.getLatRad())*Math.cos(p.getLngRad()-getLngRad()));
	}

	@Override
	public double distanceInSeaMiles(Position p) {
		return getCentralAngleRad(p) / Math.PI * 180 * 60; // 60 minutes to the degree
	}

	@Override
	public double distanceInMeters(Position p) {
		return distanceInSeaMiles(p) / 1852.216;
	}

}
