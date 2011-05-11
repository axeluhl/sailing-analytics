package com.sap.sailing.domain.tracking;

public interface GPSFixWithSpeedAndHeading extends GPSFix {
	double getSpeedInKnots();
	double getSpeedInMetersPerSecond();
	double getSpeedInKilometersPerHour();
	
	/**
	 * @return heading in degrees where 0 is north, 90 is east, 180 is south, 270 is west
	 */
	double getHeading();
}
