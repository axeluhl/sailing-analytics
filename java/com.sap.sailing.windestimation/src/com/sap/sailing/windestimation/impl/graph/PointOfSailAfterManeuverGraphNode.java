package com.sap.sailing.windestimation.impl.graph;

public class PointOfSailAfterManeuverGraphNode {

    private final double lowestSpeedPointRefersUpwind;
    private final double lowestSpeedPointRefersDownwind;

    public PointOfSailAfterManeuverGraphNode(double lowestSpeedPointRefersUpwind,
            double lowestSpeedPointRefersDownwind) {
        this.lowestSpeedPointRefersUpwind = lowestSpeedPointRefersUpwind;
        this.lowestSpeedPointRefersDownwind = lowestSpeedPointRefersDownwind;
    }

    public double getLowestSpeedPointRefersUpwind() {
        return lowestSpeedPointRefersUpwind;
    }

    public double getLowestSpeedPointRefersDownwind() {
        return lowestSpeedPointRefersDownwind;
    }

}
