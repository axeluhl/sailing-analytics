package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.Position;

public interface VectorField {
    Position getRandomPosition();

    boolean inBounds(Position p);

    Vector getVector(Position p);

    double getMaxLength();

    double motionScale(int zoomLevel);

    double particleWeight(Position p, Vector v);

    double lineWidth(double speed);

    Position[] getFieldCorners();

    void setVisNE(Position visNE);

    void setVisSW(Position visSW);

    void setVisFullCanvas(boolean full);

    double getParticleFactor();

    void setStep(int step);

    void nextStep();

    void prevStep();

    String getColor(double speed);
}
