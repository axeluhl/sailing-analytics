package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundaries;
import com.sap.sailing.simulator.BoundariesIterator;
import com.sap.sailing.simulator.WindField;
import com.sap.sailing.simulator.WindFieldCoordinates;
import com.sap.sailing.simulator.impl.RectangularBoundary;
 
public class WindFieldImpl implements WindField {

	double windSpeed;
	double windBearing;
	protected Boundaries boundary;
	
	public WindFieldImpl(RectangularBoundary b, double windspeed, double bearing)
	{
		boundary = b;
		windSpeed = windspeed;
		windBearing = bearing;
	}
	
	@Override
	public Wind getWind(WindFieldCoordinates coordinates) {
		return new WindImpl(coordinates.getPosition(),coordinates.getTimePoint(), 
				new KilometersPerHourSpeedWithBearingImpl(windSpeed,
						new DegreeBearingImpl(windBearing)));
	}

	@Override
	public Boundaries getBoundaries() {

		return boundary;
	}
	
	public BoundariesIterator iterator()
	{
		return boundary.boundariesIterator();
	}
	
	
}


