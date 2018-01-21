package com.sap.sailing.windestimation.impl.graph;

public class GraphNode {

    private final PointOfSail pointOfSailBeforeManeuver;

    public GraphNode(PointOfSail pointOfSailBeforeManeuver) {
        this.pointOfSailBeforeManeuver = pointOfSailBeforeManeuver;
    }

    public PointOfSail getPointOfSailBeforeManeuver() {
        return pointOfSailBeforeManeuver;
    }

}
