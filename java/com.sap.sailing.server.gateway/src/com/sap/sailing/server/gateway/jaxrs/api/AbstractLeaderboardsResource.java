package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.TimePoint;

public abstract class AbstractLeaderboardsResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(AbstractLeaderboardsResource.class.getName());

    public enum ResultStates {
        Live, Final
    }

    protected boolean isValidLeaderboard(Leaderboard leaderboard) {
        final boolean result;
        if (leaderboard == null) {
            logger.warning("Could not find a leaderboard with the given name");
            result = false;
        } else if (!(leaderboard instanceof HasRegattaLike)) {
            logger.warning("Specified Leaderboard does not have a RegattaLike child (is not a RegattaLeaderboard/FlexibleLeaderboard)");
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    protected JSONObject createEmptyLeaderboardJson(Leaderboard leaderboard, ResultStates resultState, Integer maxCompetitorsCount) throws NoWindException {
        JSONObject jsonLeaderboard = new JSONObject();
        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, null, maxCompetitorsCount);
        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        for (Competitor competitor : leaderboard.getCompetitors()) {
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            jsonCompetitor.put("shortName", competitor.getShortName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName == null ? competitor.getName() : displayName);
            jsonCompetitor.put("id", competitor.getId().toString());
            if (competitor.hasBoat()) {
                CompetitorWithBoat competitorWithBoat = (CompetitorWithBoat) competitor; 
                jsonCompetitor.put("sailID", competitorWithBoat.getBoat().getSailID());
            }
            Nationality nationality = competitor.getTeam().getNationality();
            jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym() : null);
            jsonCompetitor.put("countryCode", nationality != null ? (nationality.getCountryCode() != null ? nationality
                    .getCountryCode().getTwoLetterISOCode() : null) : null);
            jsonCompetitor.put("rank", 0);
            jsonCompetitor.put("carriedPoints", null);
            jsonCompetitor.put("netPoints", null);
            jsonCompetitorEntries.add(jsonCompetitor);
            JSONObject jsonRaceColumns = new JSONObject();
            jsonCompetitor.put("raceScores", jsonRaceColumns);
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                JSONObject jsonEntry = new JSONObject();
                jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                jsonEntry.put("totalPoints", null);
                jsonEntry.put("netPoints", null);
                jsonEntry.put("maxPointsReason", "");
                jsonEntry.put("rank", 0);
                jsonEntry.put("isDiscarded", false);
                jsonEntry.put("isCorrected", false);
            }
        }
        return jsonLeaderboard;
    }
    
    protected void writeCompetitorBaseData(JSONObject jsonCompetitor, CompetitorDTO competitor, LeaderboardDTO leaderboard, boolean competitorAndBoatIdsOnly) {
        jsonCompetitor.put("id", competitor.getIdAsString());
        if (!competitorAndBoatIdsOnly) {
            jsonCompetitor.put("name", competitor.getName());
            jsonCompetitor.put("shortName", competitor.getShortName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName == null ? competitor.getName() : displayName);
            jsonCompetitor.put("sailID", competitor.hasBoat() ? ((CompetitorWithBoatDTO) competitor).getSailID() : null);  // only for backward compatibility
            jsonCompetitor.put("nationality", competitor.getThreeLetterIocCountryCode());
            jsonCompetitor.put("countryCode", competitor.getTwoLetterIsoCountryCode());
        }
    }

    protected void writeBoatData(JSONObject jsonBoat, BoatDTO boat, boolean competitorAndBoatIdsOnly) {
        jsonBoat.put("id", boat.getIdAsString());
        if (!competitorAndBoatIdsOnly) {
            jsonBoat.put("name", boat.getName());
            jsonBoat.put("displayName", boat.getDisplayName());
            jsonBoat.put("sailId", boat.getSailId());
            jsonBoat.put("boatClass", boat.getBoatClass().getName());
            jsonBoat.put("color", boat.getColor() != null ? boat.getColor().toString() : null);
        }
    }

    protected void writeCommonLeaderboardData(JSONObject jsonLeaderboard, Leaderboard leaderboard,
            ResultStates resultState, Date resultTimePoint, Integer maxCompetitorsCount) {
        jsonLeaderboard.put("name", leaderboard.getName());
        final String displayName = leaderboard.getDisplayName();
        jsonLeaderboard.put("displayName", displayName == null ? leaderboard.getName() : displayName);
        jsonLeaderboard.put("resultTimepoint", resultTimePoint != null ? resultTimePoint.getTime() : null);
        jsonLeaderboard.put("resultState", resultState.name());
        jsonLeaderboard.put("type", leaderboard.getLeaderboardType().name());
        if (leaderboard instanceof RegattaLeaderboard) {
            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
            jsonLeaderboard.put("canBoatsOfCompetitorsChangePerRace", regattaLeaderboard.getRegatta().canBoatsOfCompetitorsChangePerRace());
        } else {
            jsonLeaderboard.put("canBoatsOfCompetitorsChangePerRace", false);
        }
        jsonLeaderboard.put("maxCompetitorsCount", maxCompetitorsCount);
        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        if (scoreCorrection != null) {
            jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
            TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
            jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asMillis() : null);
        } else {
            jsonLeaderboard.put("scoringComment", null);
            jsonLeaderboard.put("lastScoringUpdate", null);
        }
        JSONArray jsonColumnNames = new JSONArray();
        jsonLeaderboard.put("columnNames", jsonColumnNames);
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            jsonColumnNames.add(raceColumn.getName());
        }
    }

    protected TimePoint calculateTimePointForResultState(Leaderboard leaderboard, ResultStates resultState) {
        TimePoint result = null;
        switch (resultState) {
        case Live:
            // the timepoint to be used for a live leaderboard is 'null' 
            result = null;
            break;
        case Final:
            if (leaderboard.getScoreCorrection() != null
                    && leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity() != null) {
                result = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
                // As we don't have implemented bug 1246 (Define a clear result state for races and leaderboards) so far
                // we need to make sure that the timpoint for the final state is not determined in the middle of a
                // running race,
                // because this would deliver not only final results but also some "mixed-in" live results.
                // Therefore, if there is a race that hasn't finished yet and whose first start mark passing is before
                // the current result, move result to before the start mark passing.
                for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
                    TimePoint endOfRace = trackedRace.getEndOfRace();
                    if (endOfRace == null) {
                        Waypoint firstWaypoint = trackedRace.getRace().getCourse().getFirstWaypoint();
                        if (firstWaypoint != null) {
                            Iterable<MarkPassing> markPassingsForFirstWaypoint = trackedRace
                                    .getMarkPassingsInOrder(firstWaypoint);
                            if (markPassingsForFirstWaypoint != null) {
                                trackedRace.lockForRead(markPassingsForFirstWaypoint);
                                try {
                                    Iterator<MarkPassing> i = markPassingsForFirstWaypoint.iterator();
                                    if (i.hasNext()) {
                                        TimePoint earliestMarkPassingTimePoint = i.next().getTimePoint();
                                        if (result == null || earliestMarkPassingTimePoint.before(result)) {
                                            result = earliestMarkPassingTimePoint.minus(1);
                                        }
                                    }
                                } finally {
                                    trackedRace.unlockAfterRead(markPassingsForFirstWaypoint);
                                }
                            }
                        }
                    }
                }
            }
            break;
        }
        return result;
    }

    protected abstract JSONObject getLeaderboardJson(Leaderboard leaderboard, TimePoint resultTimePoint,
            ResultStates resultState, Integer maxCompetitorsCount, List<String> raceColumnNames,
            List<String> raceDetailNames, boolean competitorAndBoatIdsOnly) throws NoWindException, InterruptedException, ExecutionException;

    protected JSONObject getLeaderboardJson(ResultStates resultState, Integer maxCompetitorsCount,
            TimePoint requestTimePoint, Leaderboard leaderboard, TimePoint timePoint, List<String> raceColumnNames,
            List<String> raceDetailNames, boolean competitorAndBoatIdsOnly) throws NoWindException, InterruptedException, ExecutionException {
        final JSONObject jsonLeaderboard;
        if (timePoint != null || resultState == ResultStates.Live) {
            jsonLeaderboard = getLeaderboardJson(leaderboard, timePoint, resultState, maxCompetitorsCount,
                    raceColumnNames, raceDetailNames, competitorAndBoatIdsOnly);
        } else {
            jsonLeaderboard = createEmptyLeaderboardJson(leaderboard, resultState, maxCompetitorsCount);
        }
        return jsonLeaderboard;
    }
}
