package com.sap.sailing.windestimation.aggregator.hmm;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sse.common.Util.Pair;

public interface GraphNodeTransitionProbabilitiesCalculator {

    Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode previousNode,
            GraphLevelBase previousLevel, IntersectedWindRange previousNodeIntersectedWindRange, GraphNode currentNode,
            GraphLevelBase currentLevel);

    WindCourseRange getWindCourseRangeForManeuverType(ManeuverForEstimation maneuver,
            ManeuverTypeForClassification maneuverType);

}
