package com.sap.sailing.gwt.ui.simulator;


public interface VectorField {

	public GeoPos getRandomPosition();

	public boolean inBounds(GeoPos p);

	public Vector interpolate(GeoPos p);

	public Vector getVector(GeoPos p);

	public double getMaxLength();

	public double motionScale(int zoomLevel);

	public double particleWeight(GeoPos p, Vector v);

	public String[] getColors();

	public double lineWidth(int alpha);
	
	public GeoPos getFieldNE();
	
	public GeoPos getFieldSW();

	public void setVisNE(GeoPos visNE);
	
	public void setVisSW(GeoPos visSW);

	public double getParticleFactor();
	
	public void setStep(int step);
	
	public void nextStep();
	
	public void prevStep();
	
}
