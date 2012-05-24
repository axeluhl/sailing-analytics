package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindFieldGenParamsDTO implements IsSerializable {
        
      
	private PositionDTO northWest;
	private PositionDTO southEast;
	
	private double xRes;
	private double yRes;
	
	private double windSpeedInKnots;
	private double windBearing;
	
	public PositionDTO getNorthWest() {
		return northWest;
	}
	public void setNorthWest(PositionDTO northWest) {
		this.northWest = northWest;
	}
	public PositionDTO getSouthEast() {
		return southEast;
	}
	public void setSouthEast(PositionDTO southEast) {
		this.southEast = southEast;
	}
	public double getxRes() {
		return xRes;
	}
	public void setxRes(double xRes) {
		this.xRes = xRes;
	}
	public double getyRes() {
		return yRes;
	}
	public void setyRes(double yRes) {
		this.yRes = yRes;
	}
	public double getWindSpeed() {
		return windSpeedInKnots;
	}
	public void setWindSpeed(double windSpeed) {
		this.windSpeedInKnots = windSpeed;
	}
	public double getWindBearing() {
		return windBearing;
	}
	public void setWindBearing(double windBearing) {
		this.windBearing = windBearing;
	}

}
