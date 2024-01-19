package com.sap.sailing.server.gateway.serialization.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.security.SecuredDomainType.LeaderboardActions;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.SingleCalculationPerSubjectCache;

public class MarkPassingsJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String ZERO_BASED_WAYPOINT_INDEX = "zeroBasedWaypointIndex";
    public static final String WAYPOINT_NAME = "waypointName";
    public static final String BYWAYPOINT = "bywaypoint";
    public static final String MARKPASSINGS = "markpassings";
    public static final String TRACKED_RANK_AT_MARK_PASSING = "trackedRankAtMarkPassing";
    public static final String ONE_BASED_PASSING_ORDER = "oneBasedPassingOrder";
    public static final String POINTS_BASED_ON_PASSING_ORDER = "pointsBasedOnPassingOrder";
    public static final String NET_POINTS_BASED_ON_PASSING_ORDER = "netPointsBasedOnPassingOrder";
    public static final String MAX_POINTS_REASON = "maxPointsReason";
    private static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private static SingleCalculationPerSubjectCache<Pair<Leaderboard, TrackedRace>, JSONObject> ongoingLiveCalculationsByRaceAndRequestingUsername =
            new SingleCalculationPerSubjectCache<>(MarkPassingsJsonSerializer::serializeForLiveTimePointToJSON, /* 10min timeout */ Duration.ONE_MINUTE.times(10));
    
    /**
     * The time point for which to obtain the ranking information at the marks
     */
    private final TimePoint timePoint;
    
    /**
     * An optional leaderboard that, when provided, is used to provide the score and IRM/penalty/MaxPointsReason in
     * the output.
     */
    private final Leaderboard leaderboard;

    /**
     * @param leaderboard
     *            An optional leaderboard that, when provided, is used to provide the score and
     *            IRM/penalty/MaxPointsReason in the output.
     * @param timePoint
     *            time point for which to obtain the ranking information at the marks; use {@code null} to use "now"
     *            minus the live delay
     */
    public MarkPassingsJsonSerializer(Leaderboard leaderboard, TimePoint timePoint) {
        super();
        this.leaderboard = leaderboard;
        this.timePoint = timePoint;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result;
        if (timePoint == null) {
            result = ongoingLiveCalculationsByRaceAndRequestingUsername.get(new Pair<>(leaderboard, trackedRace));
        } else {
            result = serializeToJSON(leaderboard, trackedRace, timePoint);
        }
        return result;
    }
    
    private static JSONObject serializeForLiveTimePointToJSON(Pair<Leaderboard, TrackedRace> leaderboardAndTrackedRace) {
        final TimePoint timePointForRanksAtMarks = MillisecondsTimePoint.now().minus(leaderboardAndTrackedRace.getB().getDelayToLiveInMillis());
        return serializeToJSON(leaderboardAndTrackedRace.getA(), leaderboardAndTrackedRace.getB(), timePointForRanksAtMarks);
    }

    private static JSONObject serializeToJSON(Leaderboard leaderboard, TrackedRace trackedRace, TimePoint timePoint) {
        final JSONObject result;
        final Course course = trackedRace.getRace().getCourse();
        final TimePoint timePointForRanksAtMarks = timePoint != null ? timePoint :
            MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
        result = new JSONObject();
        CompetitorAndBoatJsonSerializer competitorWithBoatSerializer = CompetitorAndBoatJsonSerializer.create(/* serializeNonPublicCompetitorFields */ false);
        CompetitorJsonSerializer competitorSerializer = CompetitorJsonSerializer.create();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        // start mark passing; rank is ordering by start time
        final Map<Waypoint, Map<Competitor, Integer>> orderOfMarkPassings = new HashMap<>();
        trackedRace.getRace().getCourse().lockForRead();
        try {
            for (final Waypoint waypoint : course.getWaypoints()) {
                final Iterable<MarkPassing> markPassingsInOrder = trackedRace.getMarkPassingsInOrder(waypoint);
                trackedRace.lockForRead(markPassingsInOrder);
                try {
                    int markPassingCounter = 1;
                    for (final MarkPassing markPassing : markPassingsInOrder) {
                        orderOfMarkPassings.computeIfAbsent(waypoint, wp->new HashMap<>()).put(markPassing.getCompetitor(), markPassingCounter++);
                    }
                } finally {
                    trackedRace.unlockAfterRead(markPassingsInOrder);
                }
            }
        } finally {
            trackedRace.getRace().getCourse().unlockAfterRead();
        }
        final Map<TrackedLeg, LinkedHashMap<Competitor, Integer>> legRanks = new HashMap<>();
        final WindLegTypeAndLegBearingAndORCPerformanceCurveCache cache = new LeaderboardDTOCalculationReuseCache(timePointForRanksAtMarks);
        for (Entry<Competitor, Boat> competitorAndBoatEntry : trackedRace.getRace().getCompetitorsAndTheirBoats().entrySet()) {
            Competitor competitor = competitorAndBoatEntry.getKey();
            Boat boat = competitorAndBoatEntry.getValue();
            JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            forCompetitorJson.put(COMPETITOR, competitorWithBoatSerializer.serialize(new Pair<>(competitor, boat)));
            final NavigableSet<MarkPassing> markPassingsForCompetitor = trackedRace.getMarkPassings(competitor);
            JSONArray markPassingsForCompetitorJson = new JSONArray();
            forCompetitorJson.put(MARKPASSINGS, markPassingsForCompetitorJson);
            final boolean leaderboardValidAndSubjectMaySeePremiumInformation = leaderboard != null &&
                    SecurityUtils.getSubject().isPermitted(SecuredDomainType.LEADERBOARD.getStringPermissionForObject(LeaderboardActions.PREMIUM_LEADERBOARD_INFORMATION, leaderboard));
            trackedRace.lockForRead(markPassingsForCompetitor);
            try {
                for (MarkPassing markPassing : markPassingsForCompetitor) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForCompetitorJson.add(markPassingJson);
                    addWaypoint(course, markPassing.getWaypoint(), markPassingJson);
                    addMarkPassingTime(markPassing, markPassingJson);
                    final int zeroBasedIndexOfWaypoint = course.getIndexOfWaypoint(markPassing.getWaypoint());
                    final Integer rank;
                    final Integer passingOrder = orderOfMarkPassings.getOrDefault(markPassing.getWaypoint(), Collections.emptyMap()).getOrDefault(competitor, /* default */ 0);
                    if (zeroBasedIndexOfWaypoint == 0) {
                        rank = passingOrder; // for the start waypoint it doesn't make sense to look at the actual "ranking"; use passing order as default
                    } else {
                        final TrackedLeg trackedLeg = trackedRace.getTrackedLeg(course.getLeg(zeroBasedIndexOfWaypoint-1));
                        rank = legRanks.computeIfAbsent(trackedLeg, tl->tl.getRanks(timePointForRanksAtMarks, cache)).get(competitor);
                    }
                    markPassingJson.put(TRACKED_RANK_AT_MARK_PASSING, rank);
                    markPassingJson.put(ONE_BASED_PASSING_ORDER, passingOrder);
                    // the following expensive-to-compute metrics will be delivered only to our valued "premium" customers:
                    if (leaderboardValidAndSubjectMaySeePremiumInformation) {
                        final Pair<RaceColumn, Fleet> raceColumnAndFleet = leaderboard.getRaceColumnAndFleet(trackedRace);
                        if (raceColumnAndFleet != null) {
                            final Double totalPoints = leaderboard.getScoreCorrection().getCorrectedScore(() -> passingOrder,
                                    competitor, raceColumnAndFleet.getA(), leaderboard, markPassing.getTimePoint(),
                                    leaderboard.getNumberOfCompetitorsInLeaderboardFetcher(), leaderboard.getScoringScheme(), cache)
                                    .getCorrectedScore();
                            final Function<RaceColumn, Double> totalPointsSupplier = raceColumn->{
                                final Double totalPointsForRaceColumn;
                                if (raceColumn == raceColumnAndFleet.getA()) {
                                    totalPointsForRaceColumn = totalPoints;
                                } else {
                                    totalPointsForRaceColumn = leaderboard.getTotalPoints(competitor, raceColumn, markPassing.getTimePoint());
                                }
                                return totalPointsForRaceColumn;
                            };
                            final Set<RaceColumn> discardedRaceColumns = leaderboard.getResultDiscardingRule().getDiscardedRaceColumns(competitor, leaderboard,
                                    leaderboard.getRaceColumns(), markPassing.getTimePoint(), leaderboard.getScoringScheme(), totalPointsSupplier, cache);
                            final Double netPoints = leaderboard.getNetPoints(competitor, raceColumnAndFleet.getA(),
                                    markPassing.getTimePoint(), discardedRaceColumns, ()->totalPoints);
                            markPassingJson.put(POINTS_BASED_ON_PASSING_ORDER, totalPoints);
                            markPassingJson.put(NET_POINTS_BASED_ON_PASSING_ORDER, netPoints);
                            markPassingJson.put(MAX_POINTS_REASON, leaderboard.getMaxPointsReason(competitor,
                                    raceColumnAndFleet.getA(), markPassing.getTimePoint()));
                        }
                    }
                }
            } finally {
                trackedRace.unlockAfterRead(markPassingsForCompetitor);
            }
        }
        JSONArray byWaypointJson = new JSONArray();
        result.put(BYWAYPOINT, byWaypointJson);
        for (Waypoint waypoint : course.getWaypoints()) {
            JSONObject jsonForWaypoint = new JSONObject();
            byWaypointJson.add(jsonForWaypoint);
            addWaypoint(course, waypoint, jsonForWaypoint);
            final Iterable<MarkPassing> markPassingsForWaypoint = trackedRace.getMarkPassingsInOrder(waypoint);
            trackedRace.lockForRead(markPassingsForWaypoint);
            try {
                JSONArray markPassingsForWaypointJson = new JSONArray();
                jsonForWaypoint.put(MARKPASSINGS, markPassingsForWaypointJson);
                for (MarkPassing markPassing : markPassingsForWaypoint) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForWaypointJson.add(markPassingJson);
                    markPassingJson.put(COMPETITOR, competitorSerializer.serialize(markPassing.getCompetitor()));
                    addMarkPassingTime(markPassing, markPassingJson);
                }
            } finally {
                trackedRace.unlockAfterRead(markPassingsForWaypoint);
            }
        }
        return result;
    }

    private static void addMarkPassingTime(MarkPassing markPassing, JSONObject markPassingJson) {
        markPassingJson.put(TIMEASMILLIS, markPassing.getTimePoint().asMillis());
        markPassingJson.put(TIMEASISO, TIMEPOINT_FORMATTER.format(markPassing.getTimePoint().asDate()));
    }

    private static void addWaypoint(final Course course, Waypoint waypoint, JSONObject jsonToAddTo) {
        jsonToAddTo.put(WAYPOINT_NAME, waypoint.getName());
        jsonToAddTo.put(ZERO_BASED_WAYPOINT_INDEX, course.getIndexOfWaypoint(waypoint));
    }
}
