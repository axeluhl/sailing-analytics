package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v1/leaderboardgroups")
public class LeaderboardGroupsResource extends AbstractSailingServerResource {
    
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getLeaderboardGroups() {
        JSONArray jsonLeaderboardGroups = new JSONArray();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        for (String leaderboardGroupName : leaderboardGroups.keySet()) {
            jsonLeaderboardGroups.add(leaderboardGroupName);
        }
        
        String json = jsonLeaderboardGroups.toJSONString();
        
        // header option is set to allow communication between two sapsailing servers, especially for 
        // the master data import functionality
        return Response.ok(json, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*").build();
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboardGroup(@PathParam("name") String leaderboardGroupName) {
        Response response;
        LeaderboardGroup leaderboardGroup = getService().getLeaderboardGroupByName(leaderboardGroupName);
        if (leaderboardGroup == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a leaderboard group with name '"+leaderboardGroupName+"'.").type(MediaType.TEXT_PLAIN).build();
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
                if (scoreCorrection != null) {
                    jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
                    TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
                    jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString(): null);
                } else {
                    jsonLeaderboard.put("scoringComment", null);
                    jsonLeaderboard.put("lastScoringUpdate", null);
                }

                if (isRegattaLeaderboard) {
                    RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                    Regatta regatta = regattaLeaderboard.getRegatta();
                    jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());
                    jsonLeaderboard.put("regattaName", regatta.getName());
                } else {
                    jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());
                    jsonLeaderboard.put("regattaName", null);
                    JSONArray jsonSeriesEntries = new JSONArray();
                    jsonLeaderboard.put("series", jsonSeriesEntries);

                    // write a 'default' series to be conform with our common regatta structure 
                    JSONObject jsonSeries = new JSONObject();
                    jsonSeriesEntries.add(jsonSeries);
                    jsonSeries.put("name", LeaderboardNameConstants.DEFAULT_SERIES_NAME);
                    jsonSeries.put("isMedalSeries", null);
                    JSONArray jsonFleetsEntries = new JSONArray();
                    jsonSeries.put("fleets", jsonFleetsEntries);
                    Fleet fleet = leaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME);
                    if (fleet != null) {
                        JSONObject jsonFleet = new JSONObject();
                        jsonFleet.put("name", fleet.getName());
                        jsonFleet.put("color", fleet.getColor() != null ? fleet.getColor().getAsHtml() : null);
                        jsonFleet.put("ordering", fleet.getOrdering());
                        jsonFleetsEntries.add(jsonFleet);
                        JSONArray jsonRacesEntries = new JSONArray();
                        jsonFleet.put("races", jsonRacesEntries);
                        for (RaceColumn raceColumn: leaderboard.getRaceColumns()) {
                            JSONObject jsonRaceColumn = new JSONObject();
                            jsonRaceColumn.put("name", raceColumn.getName());
                            jsonRaceColumn.put("isMedalRace" , raceColumn.isMedalRace());
                            TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                jsonRaceColumn.put("isTracked", true);
                                jsonRaceColumn.put("regattaName", trackedRace.getTrackedRegatta().getRegatta().getName());
                                jsonRaceColumn.put("trackedRaceName", trackedRace.getRace().getName());
                                jsonRaceColumn.put("hasGpsData", trackedRace.hasGPSData());
                                jsonRaceColumn.put("hasWindData", trackedRace.hasWindData());
                            } else {
                                jsonRaceColumn.put("isTracked", false);
                                jsonRaceColumn.put("trackedRaceName", null);
                                jsonRaceColumn.put("hasGpsData", false);
                                jsonRaceColumn.put("hasWindData", false);
                            }
                            jsonRacesEntries.add(jsonRaceColumn);
                        }
                    }
                }
            }
            String json = jsonLeaderboardGroup.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
}
 