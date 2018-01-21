package com.sap.sailing.windestimation.impl.graph;

import com.sap.sailing.domain.common.SpeedWithBearing;
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
    private final SpeedWithBearing lowestSpeedWithinMainCurve;

    private final GraphNode[] nodes = new GraphNode[PointOfSail.values().length];

    public GraphLevel(Maneuver maneuver, SpeedWithBearing lowestSpeedWithinMainCurve, GraphLevel previousLevel) {
        this.maneuver = maneuver;
        this.lowestSpeedWithinMainCurve = lowestSpeedWithinMainCurve;
        if (previousLevel != null) {
            this.previousLevel = previousLevel;
            previousLevel.setNextLevel(this);
        }
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
    
    public SpeedWithBearing getLowestSpeedWithinMainCurve() {
        return lowestSpeedWithinMainCurve;
    }

}
