package com.sap.sailing.windestimation.impl.graph;

public class PointOfSailWithTackGraphNode {

    private final double lowestSpeedPointRefersUpwind;
    private final double lowestSpeedPointRefersDownwind;

    public PointOfSailWithTackGraphNode(double lowestSpeedPointRefersUpwind,
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
