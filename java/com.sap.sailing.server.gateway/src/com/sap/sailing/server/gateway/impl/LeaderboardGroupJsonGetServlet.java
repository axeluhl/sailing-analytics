package com.sap.sailing.server.gateway.impl;

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
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class LeaderboardGroupJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = 1351616646322956825L;
    private static final String PARAM_NAME_LEADERBOARDGROUPNAME = "leaderboardGroupName";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
                    boolean isMetaLeaderboard = leaderboard instanceof MetaLeaderboard ? true : false;
                    boolean isRegattaLeaderboard = leaderboard instanceof RegattaLeaderboard ? true : false;

                    JSONObject jsonLeaderboard = new JSONObject();
                    jsonLeaderboard.put("name", leaderboard.getName());
                    jsonLeaderboard.put("displayName", leaderboard.getDisplayName());
                    jsonLeaderboard.put("isMetaLeaderboard", isMetaLeaderboard);
                    jsonLeaderboard.put("isRegattaLeaderboard", isRegattaLeaderboard);
                    jsonLeaderboardEntries.add(jsonLeaderboard);

                    SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
                    if(scoreCorrection != null) {
                        jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
                        TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
                        jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString(): null);
                    } else {
                        jsonLeaderboard.put("scoringComment", null);
                        jsonLeaderboard.put("lastScoringUpdate", null);
                    }

                    if(isRegattaLeaderboard) {
                        RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                        Regatta regatta = regattaLeaderboard.getRegatta();

                        jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());

                        Iterable<? extends Series> regattaSeries = regatta.getSeries();
                        // there should only be one series
                        if(regattaSeries != null && Util.size(regattaSeries) == 1) {
                            Series series = regattaSeries.iterator().next();
                            jsonLeaderboard.put("seriesName", series.getName());
                            jsonLeaderboard.put("regattaName", regatta.getName());

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
                                    if(trackedRace != null) {
                                        jsonRaceColumn.put("isTracked", true);
                                        jsonRaceColumn.put("trackedRaceName", trackedRace.getRace().getName());
                                    } else {
                                        jsonRaceColumn.put("isTracked", false);
                                        jsonRaceColumn.put("trackedRaceName", null);
                                    }
                                    jsonRacesEntries.add(jsonRaceColumn);
                                }
                            }
                        }
                    } else {
                        jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());
                        jsonLeaderboard.put("seriesName", "Default");
                        jsonLeaderboard.put("regattaName", null);

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
                                if(trackedRace != null) {
                                    jsonRaceColumn.put("isTracked", true);
                                    jsonRaceColumn.put("trackedRaceName", trackedRace.getRace().getName());
                                } else {
                                    jsonRaceColumn.put("isTracked", false);
                                    jsonRaceColumn.put("trackedRaceName", null);
                                }
                                jsonRacesEntries.add(jsonRaceColumn);
                            }
                        }
                    }
                }
                setJsonResponseHeader(resp);
                jsonLeaderboardGroup.writeJSONString(resp.getWriter());
            }
        }
    }
}
