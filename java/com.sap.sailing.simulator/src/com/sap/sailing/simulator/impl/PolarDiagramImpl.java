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
		northWest = wind.getBearing().add(new DegreeBearingImpl(330));
		northEast = wind.getBearing().add(new DegreeBearingImpl(30));
		southWest = wind.getBearing().add(new DegreeBearingImpl(210));
		southEast = wind.getBearing().add(new DegreeBearingImpl(150));
	}

	@Override
	public SpeedWithBearing getSpeedAtBearing(Bearing bearing) {
		if (bearing.equals(northEast) || bearing.equals(northWest)) return new KilometersPerHourSpeedWithBearingImpl(wind.getKilometersPerHour(), bearing);
		if (bearing.equals(southEast) || bearing.equals(southWest)) return new KilometersPerHourSpeedWithBearingImpl(0.7*wind.getKilometersPerHour(), bearing);
		return new KilometersPerHourSpeedWithBearingImpl(0.5*wind.getKilometersPerHour(), bearing);
	}

	@Override
	public Bearing[] optimalDirectionsUpwind() {
		return new Bearing[] {northWest, northEast};
	}

	@Override
	public Bearing[] optimalDirectionsDownwind() {
		return new Bearing[] {southEast, southWest};
	}

	@Override
	public long getTurnLoss() {
		// TODO Auto-generated method stub
		return 4000;
	}

	@Override
	public WindSide getWindSide(Bearing bearing) {
		// TODO Auto-generated method stub
		return null;
	}

}
