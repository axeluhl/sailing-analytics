package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.Position;

public interface VectorField {
    public Position getRandomPosition();

    public boolean inBounds(Position p);

    public Vector getVector(Position p);

    public double getMaxLength();

    public double motionScale(int zoomLevel);

    public double particleWeight(Position p, Vector v);

    public int getIntensity(double speed);

    public String[] getColors();

    public double lineWidth(double speed);

    public Position[] getFieldCorners();

    public void setVisNE(Position visNE);

    public void setVisSW(Position visSW);

    public void setVisFullCanvas(boolean full);

    public double getParticleFactor();

    public void setStep(int step);

    public void nextStep();

    public void prevStep();
}
