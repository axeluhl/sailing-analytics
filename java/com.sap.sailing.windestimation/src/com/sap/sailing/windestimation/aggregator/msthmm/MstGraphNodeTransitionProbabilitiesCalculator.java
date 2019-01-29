package com.sap.sailing.windestimation.aggregator.msthmm;

import com.sap.sailing.windestimation.aggregator.hmm.GraphNode;
import com.sap.sailing.windestimation.aggregator.hmm.GraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.hmm.IntersectedWindRange;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Util.Pair;

public interface MstGraphNodeTransitionProbabilitiesCalculator extends GraphNodeTransitionProbabilitiesCalculator {

    double getCompoundDistance(ManeuverForEstimation fromManeuver, ManeuverForEstimation toManeuver);

    Pair<IntersectedWindRange, Double> mergeWindRangeAndGetTransitionProbability(GraphNode currentNode,
            MstGraphLevel currentLevel, PreviousNodeInfo previousNodeInfo);

}
