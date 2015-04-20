package com.sap.sailing.domain.ranking;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.RaceRankComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class OneDesignRankingMetric implements RankingMetric<Distance> {
    @Override
    public Comparator<Competitor> getRankingComparator(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return new RaceRankComparator(trackedRace, timePoint, cache);
    }

    @Override
    public Duration getTimeToImprove(TrackedRace trackedRace, Competitor trailing, Competitor leading,
            TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        // TODO find the last leg both competitors have already started and compute the gap in that leg;
        int i=0;
        final Duration result;
        final TrackedLegOfCompetitor tlocTrailing = null;
        if (tlocTrailing != null) {
            result = tlocTrailing.getGapToLeader(timePoint, leading, WindPositionMode.LEG_MIDDLE, cache);
        } else {
            result = null;
        }
        return null;
    }
}
