package com.sap.sailing.server.gateway.serialization.impl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.caching.LeaderboardDTOCalculationReuseCache;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindLegTypeAndLegBearingAndORCPerformanceCurveCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MarkPassingsJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String ZERO_BASED_WAYPOINT_INDEX = "zeroBasedWaypointIndex";
    public static final String WAYPOINT_NAME = "waypointName";
    public static final String BYWAYPOINT = "bywaypoint";
    public static final String MARKPASSINGS = "markpassings";
    public static final String TRACKED_RANK_AT_MARK_PASSING = "trackedRankAtMarkPassing";
    public static final String TOTAL_POINTS = "totalPoints";
    public static final String NET_POINTS = "netPoint";
    public static final String MAX_POINTS_REASON = "maxPointsReason";
    private static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
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
        final Course course = trackedRace.getRace().getCourse();
        final TimePoint timePointForRanksAtMarks = timePoint != null ? timePoint :
            MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis());
        JSONObject result = new JSONObject();
        CompetitorAndBoatJsonSerializer competitorWithBoatSerializer = CompetitorAndBoatJsonSerializer.create(/* serializeNonPublicCompetitorFields */ false);
        CompetitorJsonSerializer competitorSerializer = CompetitorJsonSerializer.create();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        final Map<Competitor, Integer> startOrder = new HashMap<>();
        // start mark passing; rank is ordering by start time
        final Waypoint startWaypoint = course.getFirstWaypoint();
        if (startWaypoint != null) {
            final Iterable<MarkPassing> startMarkPassings = trackedRace.getMarkPassingsInOrder(startWaypoint);
            trackedRace.lockForRead(startMarkPassings);
            try {
                int startRank = 1;
                for (final MarkPassing startMarkPassing : startMarkPassings) {
                    startOrder.put(startMarkPassing.getCompetitor(), startRank++);
                }
            } finally {
                trackedRace.unlockAfterRead(startMarkPassings);
            }
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
            trackedRace.lockForRead(markPassingsForCompetitor);
            try {
                for (MarkPassing markPassing : markPassingsForCompetitor) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForCompetitorJson.add(markPassingJson);
                    addWaypoint(course, markPassing.getWaypoint(), markPassingJson);
                    addMarkPassingTime(markPassing, markPassingJson);
                    final int zeroBasedIndexOfWaypoint = course.getIndexOfWaypoint(markPassing.getWaypoint());
                    final Integer rank;
                    if (zeroBasedIndexOfWaypoint == 0) {
                        rank = startOrder.getOrDefault(competitor, /* default */ 0);
                    } else {
                        final TrackedLeg trackedLeg = trackedRace.getTrackedLeg(course.getLeg(zeroBasedIndexOfWaypoint-1));
                        rank = legRanks.computeIfAbsent(trackedLeg, tl->tl.getRanks(timePointForRanksAtMarks, cache)).get(competitor);
                    }
                    markPassingJson.put(TRACKED_RANK_AT_MARK_PASSING, rank);
                    if (leaderboard != null) {
                        final Pair<RaceColumn, Fleet> raceColumnAndFleet = leaderboard.getRaceColumnAndFleet(trackedRace);
                        if (raceColumnAndFleet != null) {
                            final TimePoint markPassingTimePoint = markPassing.getTimePoint();
                            final LeaderboardDTOCalculationReuseCache cacheForMarkPassingTimePoint = new LeaderboardDTOCalculationReuseCache(markPassingTimePoint);
                            final Double totalPoints = leaderboard.getTotalPoints(
                                    competitor, raceColumnAndFleet.getA(), markPassingTimePoint, cacheForMarkPassingTimePoint);
                            markPassingJson.put(TOTAL_POINTS, totalPoints);
                            final Set<RaceColumn> discards = leaderboard.getResultDiscardingRule().getDiscardedRaceColumns(competitor, leaderboard,
                                    leaderboard.getRaceColumns(), markPassingTimePoint, cacheForMarkPassingTimePoint);
                            markPassingJson.put(NET_POINTS, leaderboard.getNetPoints(competitor, raceColumnAndFleet.getA(), markPassingTimePoint,
                                    discards, ()->totalPoints));
                            markPassingJson.put(MAX_POINTS_REASON, leaderboard.getMaxPointsReason(competitor, raceColumnAndFleet.getA(), markPassingTimePoint));
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

    private void addMarkPassingTime(MarkPassing markPassing, JSONObject markPassingJson) {
        markPassingJson.put(TIMEASMILLIS, markPassing.getTimePoint().asMillis());
        markPassingJson.put(TIMEASISO, TIMEPOINT_FORMATTER.format(markPassing.getTimePoint().asDate()));
    }

    private void addWaypoint(final Course course, Waypoint waypoint, JSONObject jsonToAddTo) {
        jsonToAddTo.put(WAYPOINT_NAME, waypoint.getName());
        jsonToAddTo.put(ZERO_BASED_WAYPOINT_INDEX, course.getIndexOfWaypoint(waypoint));
    }
}
