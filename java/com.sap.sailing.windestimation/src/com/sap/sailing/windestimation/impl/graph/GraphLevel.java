package com.sap.sailing.windestimation.impl.graph;

import com.sap.sailing.domain.tracking.Maneuver;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevel {

    private GraphLevel previousLevel = null;
    private GraphLevel nextLevel = null;

    private final Maneuver maneuver;
    private final SingleManeuverClassificationResult maneuverClassificationResult;

    private final GraphNode[] nodes = new GraphNode[PointOfSail.values().length];

    public GraphLevel(Maneuver maneuver, SingleManeuverClassifier singleManeuverClassifier, GraphLevel previousLevel) {
        this.maneuver = maneuver;
        if (previousLevel != null) {
            this.previousLevel = previousLevel;
            previousLevel.setNextLevel(this);
        }
        maneuverClassificationResult = singleManeuverClassifier.computeClassificationResult(maneuver);
        constructGraphNodes();
    }

    private void constructGraphNodes() {
        for (PointOfSail pointOfSail : PointOfSail.values()) {
            nodes[pointOfSail.ordinal()] = new GraphNode(pointOfSail);
        }
    }

    void setNextLevel(GraphLevel nextLevel) {
        this.nextLevel = nextLevel;
    }

    public GraphLevel getNextLevel() {
        return nextLevel;
    }

    void setPreviousLevel(GraphLevel previousLevel) {
        this.previousLevel = previousLevel;
    }

    public GraphLevel getPreviousLevel() {
        return previousLevel;
    }

    public Maneuver getManeuver() {
        return maneuver;
    }

    public GraphNode[] getNodes() {
        return nodes;
    }

    public SingleManeuverClassificationResult getManeuverClassificationResult() {
        return maneuverClassificationResult;
    }

}
