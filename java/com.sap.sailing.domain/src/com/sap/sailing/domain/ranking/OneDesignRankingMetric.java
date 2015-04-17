package com.sap.sailing.domain.ranking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sse.common.TimePoint;

public class OneDesignRankingMetric implements RankingMetric<Distance> {
    @Override
    public Distance getRankingMetric(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return trackedRace.getWindwardDistanceToOverallLeader(competitor, timePoint, WindPositionMode.LEG_MIDDLE, cache);
    }
}
