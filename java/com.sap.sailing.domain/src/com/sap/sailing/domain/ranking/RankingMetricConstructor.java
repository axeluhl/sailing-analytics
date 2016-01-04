package com.sap.sailing.domain.ranking;

import java.io.Serializable;
import java.util.function.Function;

import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A serializable constructor for a {@link RankingMetric} that takes a {@link TrackedRace} as a constructor
 * argument.
 * 
 * @author Axel Uhl (d043530)
 *
 */
@FunctionalInterface
public interface RankingMetricConstructor extends Serializable, Function<TrackedRace, RankingMetric> {
}
