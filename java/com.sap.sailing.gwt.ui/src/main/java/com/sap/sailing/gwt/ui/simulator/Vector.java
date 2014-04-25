package com.sap.sailing.gwt.ui.simulator;

public class Vector {

	public double x;
	public double y;
	
	public double length() {
		return Math.sqrt(x*x + y*y);
	}
	
}
