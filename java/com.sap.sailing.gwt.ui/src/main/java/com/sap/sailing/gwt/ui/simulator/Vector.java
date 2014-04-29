package com.sap.sailing.gwt.ui.simulator;

public class Vector {

	public double x;
	public double y;
	
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
