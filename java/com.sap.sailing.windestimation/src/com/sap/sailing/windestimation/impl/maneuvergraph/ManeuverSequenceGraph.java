package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.Maneuver;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverSequenceGraph {

    private final SingleManeuverClassifier singleManeuverClassifier;

    private ManeuverNodesLevel firstGraphLevel = null;
    private ManeuverNodesLevel lastGraphLevel = null;

    public ManeuverSequenceGraph(BoatClass boatClass, PolarDataService polarService,
            IManeuverSpeedRetriever maneuverSpeedRetriever, Iterable<Maneuver> maneuverSequence) {
        singleManeuverClassifier = new SingleManeuverClassifier(boatClass, polarService, maneuverSpeedRetriever);
        for (Maneuver maneuver : maneuverSequence) {
            appendManeuverAsGraphLevel(maneuver);
        }
    }

    private void appendManeuverAsGraphLevel(Maneuver maneuver) {
        lastGraphLevel = new ManeuverNodesLevel(maneuver, singleManeuverClassifier, lastGraphLevel);
        if (firstGraphLevel == null) {
            firstGraphLevel = lastGraphLevel;
        }
    }

    public void computePossiblePathsWithDistances() {
        ManeuverNodesLevel currentLevel = firstGraphLevel;
        while (currentLevel != null) {
            currentLevel.computeDistances();
            currentLevel = currentLevel.getNextLevel();
        }
    }

}
