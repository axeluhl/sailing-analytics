package com.sap.sailing.simulator.impl;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KilometersPerHourSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.simulator.Boundary;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.WindControlParameters;
import com.sap.sailing.simulator.WindField;
 
public class WindFieldImpl implements WindField {

	protected WindControlParameters windParameters;
	protected Boundary boundary;
	
	public WindFieldImpl(Boundary b, WindControlParameters parameters)
	{
		boundary = b;
		windParameters = parameters;
	}
	
	@Override
	public Wind getWind(TimedPosition coordinates) {
	        KnotSpeedImpl knotSpeedImpl = new KnotSpeedImpl(windParameters.baseSpeed);
		
	        double wBearing = windParameters.windBearing *
				(1 + coordinates.getPosition().getDistance(boundary.getCorners().get("NorthWest")).getMeters()/boundary.getHeight().getMeters());
		SpeedWithBearing wspeed = new KilometersPerHourSpeedWithBearingImpl(knotSpeedImpl.getKilometersPerHour(), new DegreeBearingImpl(wBearing));
		
		return new WindImpl(coordinates.getPosition(),coordinates.getTimePoint(), wspeed);
				
	}

	@Override
	public Boundary getBoundaries() {

		return boundary;
	}
	
}


