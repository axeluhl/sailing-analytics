package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Util.Pair;

public interface GraphNodeTransitionProbabilitiesCalculator {

    IntersectedWindRange getInitialWindRange(GraphNode currentNode, GraphLevel currentLevel);

    Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
            GraphLevel previousLevel, BestManeuverNodeInfo previousNodeInfo, GraphNode currentNode,
            GraphLevel currentLevel);

    WindCourseRange getWindCourseRangeForManeuverType(ManeuverForEstimation maneuver,
            ManeuverTypeForClassification maneuverType);

}
