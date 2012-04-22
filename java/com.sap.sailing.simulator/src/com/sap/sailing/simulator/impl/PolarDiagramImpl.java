package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.simulator.PolarDiagram;

public class PolarDiagramImpl implements PolarDiagram {

	private SpeedWithBearing wind;
	private Bearing northWest;
	private Bearing northEast;
	private Bearing southEast;
	private Bearing southWest;
	
	public PolarDiagramImpl(double windSpeed) {
		
		setWind(new KilometersPerHourSpeedWithBearingImpl(windSpeed, new DegreeBearingImpl(0)));
		
	}
	
	@Override
	public SpeedWithBearing getWind() {
		return wind;
	}

	@Override
	public void setWind(SpeedWithBearing newWind) {
		wind = newWind;
		
		northWest = wind.getBearing().add(new DegreeBearingImpl(0));
		northEast = wind.getBearing().add(new DegreeBearingImpl(0));
		southWest = wind.getBearing().add(new DegreeBearingImpl(0));
		southEast = wind.getBearing().add(new DegreeBearingImpl(0));
	}

	@Override
	public SpeedWithBearing getSpeedAtBearing(Bearing bearing) {
		return new KilometersPerHourSpeedWithBearingImpl(0, bearing);
	}

	@Override
	public Bearing[] optimalDirectionsUpwind() {
		return new Bearing[] {northWest, northEast};
	}

	@Override
	public Bearing[] optimalDirectionsDownwind() {
		return new Bearing[] {southEast, southWest};
	}

}
