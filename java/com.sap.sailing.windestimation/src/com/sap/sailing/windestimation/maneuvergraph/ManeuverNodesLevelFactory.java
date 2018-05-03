package com.sap.sailing.windestimation.maneuvergraph;

public interface ManeuverNodesLevelFactory<T extends ManeuverNodesLevel<T>, R> {

    T createNewManeuverNodesLevel(R nodeLevelReference);

}
