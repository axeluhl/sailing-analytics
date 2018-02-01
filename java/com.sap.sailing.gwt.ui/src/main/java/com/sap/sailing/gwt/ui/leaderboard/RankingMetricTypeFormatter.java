package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class RankingMetricTypeFormatter {
    public static String format(RankingMetrics rankingMetricType, StringMessages stringMessages) {
        switch (rankingMetricType) {
        case ONE_DESIGN:
            return stringMessages.oneDesignRankingMetric();
        case ORC_PERFORMANCE_CURVE:
            return stringMessages.orcPerformanceCurveRankingMetric();
        case TIME_ON_TIME_AND_DISTANCE:
            return stringMessages.timeOnTimeAndDistanceRankingMetric();
        }
        return null;
    }
    
    public static String getDescription(RankingMetrics rankingMetricType, StringMessages stringMessages) {
        switch (rankingMetricType) {
        case ONE_DESIGN:
            return stringMessages.oneDesignRankingMetricDescription();
        case ORC_PERFORMANCE_CURVE:
            return stringMessages.orcPerformanceCurveRankingMetricDescription();
        case TIME_ON_TIME_AND_DISTANCE:
            return stringMessages.timeOnTimeAndDistanceRankingMetricDescription();
        }
        return format(rankingMetricType, stringMessages);
    }
}
