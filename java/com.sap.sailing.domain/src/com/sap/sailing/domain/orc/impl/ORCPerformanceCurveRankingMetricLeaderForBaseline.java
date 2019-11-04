package com.sap.sailing.domain.orc.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Instead of using the boat with the least GPH as baseline for absolute corrected time calculation, this version uses
 * the boat leading by relative corrected time. Still, an {@link #getExplicitScratchBoat() explicitly-defined scratch
 * boat} will take precedence.
 * <p>
 * 
 * Note that official tools such as the ORC PCS Scorer used for the 2019 ORC Worlds uses the leader by corrected time as
 * base line for absolute corrected times, just as implemented here.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ORCPerformanceCurveRankingMetricLeaderForBaseline extends ORCPerformanceCurveRankingMetric {
    private static final long serialVersionUID = 3623878797931850165L;

    public ORCPerformanceCurveRankingMetricLeaderForBaseline(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public RankingMetrics getType() {
        return RankingMetrics.ORC_PERFORMANCE_CURVE_LEADER_FOR_BASELINE;
    }

    @Override
    protected Competitor getBaseLineCompetitorForAbsoluteCorrectedTimes(TimePoint timePoint,
            WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache) {
        final Competitor result;
        if (getExplicitScratchBoat() != null) {
            result = getExplicitScratchBoat();
        } else {
            final Set<Competitor> competitors = new HashSet<>();
            Util.addAll(getCompetitors(), competitors);
            result =Collections.min(competitors, getRaceRankingComparator(timePoint));
        }
        return result;
    }
}
