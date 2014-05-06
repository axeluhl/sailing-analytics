package com.sap.sailing.gwt.ui.simulator.streamlets;

public class Vector {

	public double x;
	public double y;
	
	public Vector() {
	}

	public Vector(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
	public Vector setLength(double length) {
		double current = this.length();
		if (current > 0) {
			double scale = length / current;
			this.x *= scale;
			this.y *= scale;
		}
		return this;
	}
}
