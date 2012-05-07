package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.WindField;
 
public class WindFieldImpl implements WindField {

	double windSpeed;
	double windBearing;
	protected Boundary boundary;
	
	public WindFieldImpl(Boundary b, double windspeed, double bearing)
	{
		boundary = b;
		windSpeed = windspeed;
		windBearing = bearing;
	}
	
	@Override
	public Wind getWind(TimedPosition coordinates) {
		return new WindImpl(coordinates.getPosition(),coordinates.getTimePoint(), 
				new KilometersPerHourSpeedWithBearingImpl(windSpeed,
						new DegreeBearingImpl(windBearing)));
	}

	@Override
	public Boundary getBoundaries() {

		return boundary;
	}
	
}


