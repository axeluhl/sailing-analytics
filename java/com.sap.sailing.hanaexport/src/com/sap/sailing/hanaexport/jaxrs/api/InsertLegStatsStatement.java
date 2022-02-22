package com.sap.sailing.hanaexport.jaxrs.api;

import java.sql.Connection;
import java.sql.SQLException;

import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.ranking.RankingMetric.RankingInfo;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.hanaexport.jaxrs.api.InsertLegStatsStatement.TrackedLegOfCompetitorRankingInfoCacheAndNow;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public class InsertLegStatsStatement extends AbstractPreparedInsertStatement<TrackedLegOfCompetitorRankingInfoCacheAndNow> {
    static class TrackedLegOfCompetitorRankingInfoCacheAndNow {
        private final TimePoint now;
        private final RankingInfo rankingInfo;
        private final WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache;
        private final TrackedLegOfCompetitor trackedLegOfCompetitor;
        public TrackedLegOfCompetitorRankingInfoCacheAndNow(TimePoint now, RankingInfo rankingInfo,
                WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache,
                TrackedLegOfCompetitor trackedLegOfCompetitor) {
            super();
            this.now = now;
            this.rankingInfo = rankingInfo;
            this.cache = cache;
            this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        }
        public TimePoint getNow() {
            return now;
        }
        public RankingInfo getRankingInfo() {
            return rankingInfo;
        }
        public WindLegTypeAndLegBearingAndORCPerformanceCurveCache getCache() {
            return cache;
        }
        public TrackedLegOfCompetitor getTrackedLegOfCompetitor() {
            return trackedLegOfCompetitor;
        }
    }
    
    protected InsertLegStatsStatement(Connection connection) throws SQLException {
        super(connection.prepareStatement(
                "INSERT INTO SAILING.\"LegStats\" (\"race\", \"regatta\", \"number\", \"competitorId\", \"rankOneBased\", \"distanceSailedInMeters\", \"elapsedTimeInSeconds\", "+
                        "\"avgCrossTrackErrorInMeters\", \"absoluteAvgCrossTrackErrorInMeters\", \"numberOfTacks\", "+
                        "\"numberOfGybes\", \"numberOfPenaltyCircles\", \"avgVelocityMadeGoodInKnots\", \"gapToLeaderInSeconds\") "+
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"));
    }

    @Override
    public void parameterizeStatement(TrackedLegOfCompetitorRankingInfoCacheAndNow trackedLegOfCompetitorAndNow) throws SQLException {
        getPreparedStatement().setString(1, trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getTrackedLeg().getTrackedRace().getRace().getName());
        getPreparedStatement().setString(2, trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getTrackedLeg().getTrackedRace().getTrackedRegatta().getRegatta().getName());
        getPreparedStatement().setInt(3, trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getTrackedLeg().getLeg().getZeroBasedIndexOfStartWaypoint());
        getPreparedStatement().setString(4, trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getCompetitor().getId().toString());
        getPreparedStatement().setInt(5, trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getRank(trackedLegOfCompetitorAndNow.getNow()));
        setDouble(6, metersOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getDistanceTraveled(trackedLegOfCompetitorAndNow.getNow())));
        setDouble(7, secondsOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getTime(trackedLegOfCompetitorAndNow.getNow())));
        setDouble(8, metersOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getAverageSignedCrossTrackError(trackedLegOfCompetitorAndNow.getNow(), /* waitForLatest */ false)));
        setDouble(9, metersOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getAverageAbsoluteCrossTrackError(trackedLegOfCompetitorAndNow.getNow(), /* waitForLatest */ false)));
        try {
            getPreparedStatement().setInt(10, intOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getNumberOfTacks(trackedLegOfCompetitorAndNow.getNow(), /* waitForLatest */ false)));
            getPreparedStatement().setInt(11, intOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getNumberOfJibes(trackedLegOfCompetitorAndNow.getNow(), /* waitForLatest */ false)));
            getPreparedStatement().setInt(12, intOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getNumberOfPenaltyCircles(trackedLegOfCompetitorAndNow.getNow(), /* waitForLatest */ false)));
        } catch (NoWindException nwe) {
            getPreparedStatement().setInt(10, 0);
            getPreparedStatement().setInt(11, 0);
            getPreparedStatement().setInt(12, 0);
        }
        final Speed vmg = trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getAverageVelocityMadeGood(trackedLegOfCompetitorAndNow.getNow());
        setDouble(13, vmg==null?0:vmg.getKnots());
        setDouble(14, secondsOr0ForNull(trackedLegOfCompetitorAndNow.getTrackedLegOfCompetitor().getGapToLeader(trackedLegOfCompetitorAndNow.getNow(), WindPositionMode.LEG_MIDDLE,
                trackedLegOfCompetitorAndNow.getRankingInfo(), trackedLegOfCompetitorAndNow.getCache())));
    }
}
