package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class WindFieldGenParamsDTO implements Serializable {
        
        public enum WindPattern {
            CONSTANT,
            OSCILLATING
        }
	private static final long serialVersionUID = 1L;
	
	private PositionDTO northWest;
	private PositionDTO southEast;
	
	private double xRes;
	private double yRes;
	
	private double windSpeed;
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
		return windSpeed;
	}
	public void setWindSpeed(double windSpeed) {
		this.windSpeed = windSpeed;
	}
	public double getWindBearing() {
		return windBearing;
	}
	public void setWindBearing(double windBearing) {
		this.windBearing = windBearing;
	}

}
