package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.dto.PositionDTO;


public interface VectorField {

	public PositionDTO getRandomPosition();

	public boolean inBounds(PositionDTO p);

	public Vector interpolate(PositionDTO p);

	public Vector getVector(PositionDTO p);

	public double getMaxLength();

	public double motionScale(int zoomLevel);

	public double particleWeight(PositionDTO p, Vector v);

	public String[] getColors();

	public double lineWidth(int alpha);
	
	public PositionDTO getFieldNE();
	
	public PositionDTO getFieldSW();

	public void setVisNE(PositionDTO visNE);
	
	public void setVisSW(PositionDTO visSW);

	public double getParticleFactor();
	
	public void setStep(int step);
	
	public void nextStep();
	
	public void prevStep();
	
}
