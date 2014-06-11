package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.sap.sailing.domain.common.Position;

public class Particle {
    public Position currentPosition;
    public Vector previousPixelCoordinate;
    public Vector currentPixelCoordinate;
    public int stepsToLive;
    public Vector v;
}
