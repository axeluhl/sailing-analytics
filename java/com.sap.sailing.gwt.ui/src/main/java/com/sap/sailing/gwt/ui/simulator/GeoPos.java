package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.maps.client.base.LatLng;

public class GeoPos {

	public double lat;
	public double lng;
	
	public GeoPos() {
		
	}
	
	public GeoPos(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}
	
	public GeoPos(LatLng pos) {
		this.lat = pos.getLatitude();
		this.lng = pos.getLongitude();
	}
	
}
