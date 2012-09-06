package com.sap.sailing.server.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.SailingServerHttpServlet;

public class LeaderboardGroupJsonExportServlet extends SailingServerHttpServlet {
    private static final long serialVersionUID = 1351616646322956825L;
    private static final String PARAM_NAME_LEADERBOARDGROUPNAME = "leaderboardGroupName";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // to allow access to the json document directly from a client side javascript
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String leaderboardGroupName = req.getParameter(PARAM_NAME_LEADERBOARDGROUPNAME);
        if (leaderboardGroupName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Need to specify the name of a leaderboard group using the "+
                    PARAM_NAME_LEADERBOARDGROUPNAME+" parameter");
        } else {
            LeaderboardGroup leaderboardGroup = getService().getLeaderboardGroupByName(leaderboardGroupName);
            if (leaderboardGroup == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "LeaderboardGroup "+leaderboardGroupName+" not found");
            } else {
                TimePoint timePoint = MillisecondsTimePoint.now();
                JSONObject jsonLeaderboardGroup = new JSONObject();
                jsonLeaderboardGroup.put("name", leaderboardGroup.getName());
                jsonLeaderboardGroup.put("description", leaderboardGroup.getDescription());
                jsonLeaderboardGroup.put("timepoint", timePoint.toString());
                JSONArray jsonLeaderboardEntries = new JSONArray();
                jsonLeaderboardGroup.put("leaderboards", jsonLeaderboardEntries);
                for (Leaderboard leaderboard: leaderboardGroup.getLeaderboards()) {
                    JSONObject jsonLeaderboard = new JSONObject();
                    jsonLeaderboard.put("name", leaderboard.getName());
                    jsonLeaderboardEntries.add(jsonLeaderboard);

                    SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
                    if(scoreCorrection != null) {
                        JSONObject jsonScoreCorrection = new JSONObject();
                        jsonLeaderboard.put("scoreCorrection", jsonScoreCorrection);
                        jsonScoreCorrection.put("comment", scoreCorrection.getComment());
                        TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
                        jsonScoreCorrection.put("lastUpdateTimepoint", lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString(): null);
                    }

                    if(leaderboard instanceof RegattaLeaderboard) {
                        RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                        Regatta regatta = regattaLeaderboard.getRegatta();

                        ScoringSchemeType type = regatta.getScoringScheme().getType();
                        jsonLeaderboard.put("scoringScheme", type != null ? type.toString() : null);

                        Iterable<? extends Series> regattaSeries = regatta.getSeries();
                        // there should only be one series
                        if(regattaSeries != null && Util.size(regattaSeries) == 1) {
                            Series series = regattaSeries.iterator().next();
                            jsonLeaderboard.put("seriesName", series.getName());

                            JSONArray jsonFleetsEntries = new JSONArray();
                            jsonLeaderboard.put("fleets", jsonFleetsEntries);
                            for(Fleet fleet: series.getFleets()) {
                                
                                JSONObject jsonFleet = new JSONObject();
                                jsonFleet.put("name", fleet.getName());
                                jsonFleet.put("color", fleet.getColor() != null ? fleet.getColor().getAsHtml() : null);
                                jsonFleet.put("ordering", fleet.getOrdering());
                                jsonFleetsEntries.add(jsonFleet);
                                
                                JSONArray jsonRacesEntries = new JSONArray();
                                jsonFleet.put("races", jsonRacesEntries);
                                for(RaceColumnInSeries raceColumn: series.getRaceColumns()) {
                                    JSONObject jsonRaceColumn = new JSONObject();
                                    jsonRaceColumn.put("name", raceColumn.getName());
                                    jsonRaceColumn.put("isMedalRace" , raceColumn.isMedalRace());
                                    
                                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                                    jsonRaceColumn.put("isTracked", trackedRace != null ? true : false);
                                    jsonRacesEntries.add(jsonRaceColumn);
                                }
                            }
                        }
                    } else {
                        ScoringSchemeType type = leaderboard.getScoringScheme().getType();
                        jsonLeaderboard.put("scoringScheme", type != null ? type.toString() : null);

                        jsonLeaderboard.put("seriesName", "Default");

                        JSONArray jsonFleetsEntries = new JSONArray();
                        jsonLeaderboard.put("fleets", jsonFleetsEntries);
                        
                        Fleet fleet = leaderboard.getFleet("Default");
                        if(fleet != null) {
                            JSONObject jsonFleet = new JSONObject();
                            jsonFleet.put("name", fleet.getName());
                            jsonFleet.put("color", fleet.getColor() != null ? fleet.getColor().getAsHtml() : null);
                            jsonFleet.put("ordering", fleet.getOrdering());
                            jsonFleetsEntries.add(jsonFleet);
                            
                            JSONArray jsonRacesEntries = new JSONArray();
                            jsonFleet.put("races", jsonRacesEntries);
                            for(RaceColumn raceColumn: leaderboard.getRaceColumns()) {
                                JSONObject jsonRaceColumn = new JSONObject();
                                jsonRaceColumn.put("name", raceColumn.getName());
                                jsonRaceColumn.put("isMedalRace" , raceColumn.isMedalRace());

                                TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                                jsonRaceColumn.put("isTracked", trackedRace != null ? true : false);
                                jsonRacesEntries.add(jsonRaceColumn);
                            }
                        }
                    }
                }
                jsonLeaderboardGroup.writeJSONString(resp.getWriter());
            }
        }
    }
}
