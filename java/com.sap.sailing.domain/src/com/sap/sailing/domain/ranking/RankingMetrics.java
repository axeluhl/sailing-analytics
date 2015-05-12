package com.sap.sailing.domain.ranking;

import java.util.HashMap;
import java.util.Map;

public enum RankingMetrics {
    ONE_DESIGN(OneDesignRankingMetric::new),
    TIME_ON_TIME_AND_DISTANCE(TimeOnTimeAndDistanceRankingMetric::new),
    ORC_PERFORMANCE_CURVE(ORCPerformanceCurveRankingMetric::new);
    
    public static RankingMetrics getForClass(Class<? extends RankingMetric> c) {
        return byClass.get(c);
    }
    
    private RankingMetrics(RankingMetricConstructor rankingMetricConstructor) {
        this.rankingMetricConstructor = rankingMetricConstructor;
    }
    
    public RankingMetricConstructor getRankingMetricConstructor() {
        return rankingMetricConstructor;
    }

    private final RankingMetricConstructor rankingMetricConstructor;
    
    private static final Map<Class<? extends RankingMetric>, RankingMetrics> byClass;

    static {
        byClass = new HashMap<>();
        for (RankingMetrics rm : RankingMetrics.values()) {
            byClass.put(rm.getRankingMetricConstructor().apply(/* trackedRace */ null).getClass(), rm);
        }
    }
}
