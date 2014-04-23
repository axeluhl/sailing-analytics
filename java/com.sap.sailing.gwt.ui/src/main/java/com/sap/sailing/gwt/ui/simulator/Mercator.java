package com.sap.sailing.gwt.ui.simulator;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;

public class Mercator {

	private FullCanvasOverlay canvas;
	private MapWidget map;
	
	private double alpha;
	private double beta;
	private double gamma;
	private double delta;
	
	public Mercator(FullCanvasOverlay canvas, MapWidget map) {
		this.canvas = canvas;
		this.alpha = 0.0;
		this.beta = 0.0;
		this.gamma = 0.0;
		this.delta = 0.0;
		this.map = map;
		this.calibrate();
	}

	public void calibrate() {

		Point pointSW;
		Point pointNE;
		
		int canvasHeight = canvas.getCanvas().getOffsetHeight();

		LatLng mapSW = this.map.getBounds().getSouthWest();
		LatLng mapNE = this.map.getBounds().getNorthEast();
			
		pointSW = this.sphere2plane(mapSW);
		pointNE = this.sphere2plane(mapNE);
			
		if (pointNE.getX() < pointSW.getX()) {
			pointSW = Point.newInstance(pointSW.getX() - 2*Math.PI, pointSW.getY());
		}
		
		this.alpha = canvasHeight / (pointNE.getY() - pointSW.getY());
		this.beta = pointSW.getX();
		
		this.gamma = - this.alpha;
		this.delta = pointNE.getY();	
	}

	public Point sphere2plane(LatLng p) {
		double x = p.getLongitude()*Math.PI/180.0;
		double latsin = Math.sin(p.getLatitude()*Math.PI/180.0);
		double y = 0.5*Math.log((1.0+latsin)/(1.0-latsin));
		return Point.newInstance(x, y);
	}

	public LatLng plane2sphere(Point px) {
		double lng = px.getX()*180.0/Math.PI;
		double lat = Math.atan(Math.sinh(px.getY()))*180.0/Math.PI;
		return LatLng.newInstance(lat, lng);
	}

	public Point latlng2pixel(LatLng p) {
		Point proj = this.sphere2plane(p);
		double x = this.alpha * (proj.getX() - this.beta);
		double y = this.gamma * (proj.getY() - this.delta);
		return Point.newInstance(x, y);
	}

	public LatLng pixel2latlng(Point px) {
		double p = px.getX() / this.alpha + this.beta;
		double q = px.getY() / this.gamma + this.delta;
		return this.plane2sphere(Point.newInstance(p, q));
	}

	public void clearCanvas() {
		double w = this.canvas.getCanvas().getOffsetWidth();
		double h = this.canvas.getCanvas().getOffsetHeight();
		Context2d g = this.canvas.getCanvas().getContext2d();
		g.clearRect(0, 0, w, h);
	};
	
}
