package com.sap.sailing.windestimation.maneuvergraph.bestpath;

import java.util.List;

import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.GraphLevel;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface BestPathsEvaluator {

    BestPathEvaluationResult evaluateBestPath(List<Pair<GraphLevel, FineGrainedPointOfSail>> bestPath);

}
