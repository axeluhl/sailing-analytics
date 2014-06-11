package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.Position;

public interface VectorField {
    boolean inBounds(Position p, boolean visFull);

    Vector getVector(Position p);

    double getMaxLength();

    double motionScale(int zoomLevel);

    double particleWeight(Position p, Vector v);

    double lineWidth(double speed);

    Position[] getFieldCorners(boolean visFull);

    double getParticleFactor();

    void setStep(int step);

    void nextStep();

    void prevStep();

    String getColor(double speed);
}
