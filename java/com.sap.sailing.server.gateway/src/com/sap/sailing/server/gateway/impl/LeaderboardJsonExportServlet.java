package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

/**
 * Exports a leaderboard to the JSON format
 * @author Frank
 *
 */
public class LeaderboardJsonExportServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -2460691283231361152L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";
    private static final String PARAM_NAME_RESULTSTATE = "resultState";
    
    public static enum ResultStates { Live, Preliminary, Final };
    
    // for backward compatibility the default result state is live
    private final ResultStates DEFAULT_RESULT_STATE = ResultStates.Live;  

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String leaderboardName = req.getParameter(PARAM_NAME_LEADERBOARDNAME);
        if (leaderboardName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify a leaderboard name using the "+
                    PARAM_NAME_LEADERBOARDNAME+" parameter");
        } else {
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Leaderboard "+leaderboardName+" not found");
            } else {
                try {
                    ResultStates resultState = resolveRequestedResultState(req.getParameter(PARAM_NAME_RESULTSTATE));
                    TimePoint requestTimePoint = MillisecondsTimePoint.now();
                    TimePoint resultTimePoint = calculateTimePointForResultState(leaderboard, resultState);
                    
                    List<Competitor> competitorsFromBestToWorstAccordingToTotalRank = leaderboard
                            .getCompetitorsFromBestToWorst(resultTimePoint != null ? resultTimePoint : requestTimePoint);
                    Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
                    JSONObject jsonLeaderboard = new JSONObject();
                    jsonLeaderboard.put("name", leaderboard.getName());
                    
                    // for backward compatibility 
                    jsonLeaderboard.put("timepoint", resultTimePoint != null ? resultTimePoint.toString() : requestTimePoint.toString());

                    jsonLeaderboard.put("resultTimepoint", resultTimePoint != null ? resultTimePoint.toString() : null);
                    jsonLeaderboard.put("requestTimepoint", requestTimePoint.toString());
                    jsonLeaderboard.put("resultState", resultState.name());
                    
                    SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
                    if (scoreCorrection != null) {
                        jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
                        TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
                        jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString(): null);
                    } else {
                        jsonLeaderboard.put("scoringComment", null);
                        jsonLeaderboard.put("lastScoringUpdate", null);
                    }
                    
                    JSONArray jsonColumnNames = new JSONArray();
                    jsonLeaderboard.put("columnNames", jsonColumnNames);
                    for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                        jsonColumnNames.add(raceColumn.getName());
                    }
                    JSONArray jsonCompetitorEntries = new JSONArray();
                    jsonLeaderboard.put("competitors", jsonCompetitorEntries);
                    for (Competitor competitor : competitorsFromBestToWorstAccordingToTotalRank) {
                        JSONObject jsonCompetitor = new JSONObject();
                        jsonCompetitor.put("name", competitor.getName());
                        final String displayName = leaderboard.getDisplayName(competitor);
                        jsonCompetitor.put("displayName", displayName==null?competitor.getName():displayName);
                        jsonCompetitor.put("id", competitor.getId().toString());
                        jsonCompetitor.put("sailID", competitor.getBoat().getSailID());
                        Nationality nationality = competitor.getTeam().getNationality();
                        jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym(): null);
                        jsonCompetitor.put("countryCode", nationality != null ? nationality.getCountryCode().getTwoLetterISOCode(): null);
                        
                        // only add results if we have a valid resultTimePoint
                        if(resultTimePoint != null) {
                            jsonCompetitor.put("rank", competitorsFromBestToWorstAccordingToTotalRank.indexOf(competitor) + 1);
                            jsonCompetitor.put("carriedPoints", leaderboard.getCarriedPoints(competitor));
                            jsonCompetitor.put("totalPoints", leaderboard.getTotalPoints(competitor, resultTimePoint));
                            jsonCompetitorEntries.add(jsonCompetitor);
                            JSONObject jsonRaceColumns = new JSONObject();
                            jsonCompetitor.put("raceScores", jsonRaceColumns);
                            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                                if (rankedCompetitorsForColumn == null) {
                                    rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn, resultTimePoint);
                                    rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                                }
                                JSONObject jsonEntry = new JSONObject();
                                jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                                final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                                jsonEntry.put("fleet", fleetOfCompetitor==null?"":fleetOfCompetitor.getName());
                                jsonEntry.put("netPoints", leaderboard.getNetPoints(competitor, raceColumn, resultTimePoint));
                                jsonEntry.put("totalPoints", leaderboard.getTotalPoints(competitor, raceColumn, resultTimePoint));
                                MaxPointsReason maxPointsReason = leaderboard.getMaxPointsReason(competitor, raceColumn, resultTimePoint);
                                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString(): null);
                                jsonEntry.put("rank", rankedCompetitorsForColumn.indexOf(competitor)+1);
                                final TrackedRace trackedRace = raceColumn.getTrackedRace(competitor);
                                if (trackedRace != null) {
                                    jsonEntry.put("raceRank", trackedRace.getRank(competitor, resultTimePoint));
                                }
                                jsonEntry.put("isDiscarded", leaderboard.isDiscarded(competitor, raceColumn, resultTimePoint));
                                jsonEntry.put("isCorrected", leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn));
                            }
                        } else {
                            jsonCompetitor.put("rank", 0);
                            jsonCompetitor.put("carriedPoints", null);
                            jsonCompetitor.put("totalPoints", null);
                            jsonCompetitorEntries.add(jsonCompetitor);
                            JSONObject jsonRaceColumns = new JSONObject();
                            jsonCompetitor.put("raceScores", jsonRaceColumns);
                            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                JSONObject jsonEntry = new JSONObject();
                                jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                                final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                                jsonEntry.put("fleet", fleetOfCompetitor==null?"":fleetOfCompetitor.getName());
                                jsonEntry.put("netPoints", null);
                                jsonEntry.put("totalPoints", null);
                                jsonEntry.put("maxPointsReason", "");
                                jsonEntry.put("rank", 0);
                                jsonEntry.put("isDiscarded", false);
                                jsonEntry.put("isCorrected", false);
                            }
                        }
                    }
                    setJsonResponseHeader(resp);
                    jsonLeaderboard.writeJSONString(resp.getWriter());
                } catch (NoWindException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }

    private ResultStates resolveRequestedResultState(String resultStateParam) {
        ResultStates result = DEFAULT_RESULT_STATE;
        if(resultStateParam != null) {
            for(ResultStates state: ResultStates.values()) {
                if(state.name().equalsIgnoreCase(resultStateParam)) {
                    result = state;
                    break;
                }
            }
        }
        return result;
    }

    private TimePoint calculateTimePointForResultState(Leaderboard leaderboard, ResultStates resultState) {
        TimePoint result = null;
        switch (resultState) {
        case Live:
            result = MillisecondsTimePoint.now();
            break;
        case Preliminary:
        case Final:
            if(leaderboard.getScoreCorrection() != null) {
                result = leaderboard.getScoreCorrection().getTimePointOfLastCorrectionsValidity();
            }
            break;
        }
        
        return result;
    }
}
