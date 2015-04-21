package com.sap.sailing.domain.ranking;

import java.util.Comparator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.domain.tracking.impl.RaceRankComparator;
import com.sap.sailing.domain.tracking.impl.WindwardToGoComparator;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class OneDesignRankingMetric implements RankingMetric {
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
        final TrackedLegOfCompetitor currentLeg = trackedRace.getCurrentLeg(trailing, timePoint);
        if (currentLeg != null) {
            result = currentLeg.getGapToLeader(timePoint, leading, WindPositionMode.LEG_MIDDLE, cache);
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Duration getCorrectedTime(TrackedRace trackedRace, Competitor competitor, TimePoint timePoint,
            WindLegTypeAndLegBearingCache cache) {
        final Duration result;
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        if (startOfRace == null) {
            result = null;
        } else {
            final Waypoint finish = trackedRace.getRace().getCourse().getLastWaypoint();
            if (finish == null) {
                result = null;
            } else {
                final MarkPassing finishingMarkPassing = trackedRace.getMarkPassing(competitor, finish);
                if (finishingMarkPassing != null) {
                    result = startOfRace.until(finishingMarkPassing.getTimePoint());
                } else {
                    result = startOfRace.until(timePoint);
                }
            }
        }
        return result;
    }
}
