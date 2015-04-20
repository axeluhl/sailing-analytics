package com.sap.sailing.domain.ranking;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.RaceRankComparator;
import com.sap.sailing.domain.tracking.impl.WindwardToGoComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class OneDesignRankingMetric implements RankingMetric<Distance> {
    @Override
    public Comparator<Competitor> getRaceRankingComparator(TrackedRace trackedRace, TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return new RaceRankComparator(trackedRace, timePoint, cache);
    }

    @Override
    public Comparator<TrackedLegOfCompetitor> getLegRankingComparator(TrackedLeg trackedLeg,
            TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        return new WindwardToGoComparator(trackedLeg, timePoint, cache);
    }

    @Override
    public Duration getTimeToImprove(TrackedRace trackedRace, Competitor trailing, Competitor leading,
            TimePoint timePoint, WindLegTypeAndLegBearingCache cache) {
        final Duration result;
        // TODO find the last leg both competitors have already started and compute the gap in that leg;
        final TrackedLegOfCompetitor tlocTrailing = null;
        if (tlocTrailing != null) {
            result = tlocTrailing.getGapToLeader(timePoint, leading, WindPositionMode.LEG_MIDDLE, cache);
        } else {
            result = null;
        }
        return result;
    }
}
