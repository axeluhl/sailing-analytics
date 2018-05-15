package com.sap.sailing.windestimation.maneuvergraph;

import java.util.List;

import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 * @param <T>
 */
public interface ManeuverSequenceGraph<T extends ManeuverNodesLevel<T>> {

    List<Pair<T, FineGrainedPointOfSail>> determineBestPath();

    List<WindWithConfidence<TimePoint>> estimateWindTrack();

    T getFirstGraphLevel();

    T getLastGraphLevel();

}
