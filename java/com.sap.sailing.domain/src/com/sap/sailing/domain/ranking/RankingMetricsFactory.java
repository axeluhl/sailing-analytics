package com.sap.sailing.domain.ranking;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.common.RankingMetrics;

public final class RankingMetricsFactory {
    public static RankingMetrics getForClass(Class<? extends RankingMetric> c) {
        return byClass.get(c);
    }
    
    public static RankingMetricConstructor getRankingMetricConstructor(RankingMetrics rankingMetric) {
        switch (rankingMetric) {
        case ONE_DESIGN:
            return OneDesignRankingMetric::new;
        case ORC_PERFORMANCE_CURVE:
            return ORCPerformanceCurveRankingMetric::new;
        case TIME_ON_TIME_AND_DISTANCE:
            return TimeOnTimeAndDistanceRankingMetric::new;
        }
        throw new IllegalArgumentException(rankingMetric.name());
    }

    private static final Map<Class<? extends RankingMetric>, RankingMetrics> byClass;

    static {
        byClass = new HashMap<>();
        for (RankingMetrics rm : RankingMetrics.values()) {
            byClass.put(getRankingMetricConstructor(rm).apply(/* trackedRace */ null).getClass(), rm);
        }
    }
}
