package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.maps.client.geom.LatLng;

public class RegattaArea {
	
	String name;
	double radius;
	LatLng centerPos;
	LatLng edgePos;
	String color;
	

	public RegattaArea(String name, LatLng centerPos, double radius, String color) {
		this.name = name;
		this.radius = radius;
		this.centerPos = centerPos;		
		this.edgePos = this.getEdgePoint(centerPos, radius);
		this.color = color;
	}
	
	protected LatLng getEdgePoint(LatLng pos, double dist) {
		
	    double lat1 = pos.getLatitudeRadians();
	    double lon1 = pos.getLongitudeRadians();
	    
	    double brng = 0.0;
	    
	    double R = 6371;
	    double d = 1.852*dist;
	    double lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) + Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
	    double lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1), Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));
	    lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalise to -180° ... +180°*/
	    
	    double lat2deg = lat2/Math.PI*180;
	    double lon2deg = lon2/Math.PI*180;
	    
	    LatLng result = LatLng.newInstance(lat2deg, lon2deg);
		
		return result;
	}

}
