package com.sap.sailing.server.impl;

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
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.SailingServerHttpServlet;

public class LeaderboardJsonExport extends SailingServerHttpServlet {
    private static final long serialVersionUID = -2460691283231361152L;
    private static final String PARAM_NAME_LEADERBOARDNAME = "leaderboardName";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        TimePoint timePoint = MillisecondsTimePoint.now();
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
                    List<Competitor> competitorsFromBestToWorstAccordingToTotalRank = leaderboard
                            .getCompetitorsFromBestToWorst(timePoint);
                    Map<RaceColumn, List<Competitor>> rankedCompetitorsPerColumn = new HashMap<RaceColumn, List<Competitor>>();
                    JSONObject jsonLeaderboard = new JSONObject();
                    jsonLeaderboard.put("name", leaderboard.getName());
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
                        jsonCompetitor.put("nationality", competitor.getTeam().getNationality()
                                .getThreeLetterIOCAcronym());
                        jsonCompetitor.put("rank",
                                competitorsFromBestToWorstAccordingToTotalRank.indexOf(competitor) + 1);
                        jsonCompetitor.put("totalPoints", leaderboard.getTotalPoints(competitor, timePoint));
                        jsonCompetitorEntries.add(jsonCompetitor);
                        JSONObject jsonRaceColumns = new JSONObject();
                        jsonCompetitor.put("raceScores", jsonRaceColumns);
                        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                            List<Competitor> rankedCompetitorsForColumn = rankedCompetitorsPerColumn.get(raceColumn);
                            if (rankedCompetitorsForColumn == null) {
                                rankedCompetitorsForColumn = leaderboard.getCompetitorsFromBestToWorst(raceColumn, timePoint);
                                rankedCompetitorsPerColumn.put(raceColumn, rankedCompetitorsForColumn);
                            }
                            JSONObject jsonEntry = new JSONObject();
                            jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                            final Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                            jsonEntry.put("fleet", fleetOfCompetitor==null?"":fleetOfCompetitor.getName());
                            jsonEntry.put("netPoints", leaderboard.getNetPoints(competitor, raceColumn, timePoint));
                            jsonEntry.put("totalPoints", leaderboard.getTotalPoints(competitor, raceColumn, timePoint));
                            jsonEntry.put("maxPointsReason", leaderboard.getMaxPointsReason(competitor, raceColumn, timePoint));
                            jsonEntry.put("rank", rankedCompetitorsForColumn.indexOf(competitor)+1);
                            jsonEntry.put("isDiscarded", leaderboard.isDiscarded(competitor, raceColumn, timePoint));
                            jsonEntry.put("isCorrected", leaderboard.getScoreCorrection().isScoreCorrected(competitor, raceColumn));
                        }
                    }
                    jsonLeaderboard.writeJSONString(resp.getWriter());
                } catch (NoWindException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            }
        }
    }
}
