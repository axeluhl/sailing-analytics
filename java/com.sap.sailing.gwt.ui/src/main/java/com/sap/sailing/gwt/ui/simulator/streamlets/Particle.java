package com.sap.sailing.gwt.ui.simulator.streamlets;

import com.google.gwt.maps.client.base.LatLng;

public class Particle {
    public LatLng currentPosition;
    public Vector previousPixelCoordinate;
    public Vector currentPixelCoordinate;
    public int stepsToLive;
    public Vector v;
}
