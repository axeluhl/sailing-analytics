package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.hierarchy.SailingHierarchyOwnershipUpdater;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v1/leaderboardgroups")
public class LeaderboardGroupsResource extends AbstractSailingServerResource {
    @POST
    public Response migrateOwnershipForLeaderboardGroup(@QueryParam("leaderboardGroupName") String leaderboardGroupName,
            @QueryParam("createNewGroup") Boolean createNewGroup,
            @QueryParam("existingGroupId") UUID existingGroupIdOrNull, @QueryParam("newGroupName") String newGroupName,
            @QueryParam("migrateCompetitors") Boolean migrateCompetitors,
            @QueryParam("migrateBoats") Boolean migrateBoats) throws ParseException, JsonDeserializationException {
        LeaderboardGroup leaderboardGroup = getService().getLeaderboardGroupByName(leaderboardGroupName);
        SailingHierarchyOwnershipUpdater updater = SailingHierarchyOwnershipUpdater.createOwnershipUpdater(
                createNewGroup, existingGroupIdOrNull, newGroupName, migrateCompetitors, migrateBoats, getService());
        updater.updateGroupOwnershipForLeaderboardGroupHierarchy(leaderboardGroup);
        return Response.ok().build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getLeaderboardGroups() {
        JSONArray jsonLeaderboardGroups = new JSONArray();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        for (Entry<String, LeaderboardGroup> leaderboardGroupEntry : leaderboardGroups.entrySet()) {
            if (getSecurityService().hasCurrentUserReadPermission(leaderboardGroupEntry.getValue())) {
                jsonLeaderboardGroups.add(leaderboardGroupEntry.getKey());
            }
        }

        String json = jsonLeaderboardGroups.toJSONString();

        // header option is set to allow communication between two sapsailing servers, especially for
        // the master data import functionality
        return Response.ok(json).header("Access-Control-Allow-Origin", "*")
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboardGroup(@PathParam("name") String leaderboardGroupName) {
        Response response;
        LeaderboardGroup leaderboardGroup = getService().getLeaderboardGroupByName(leaderboardGroupName);
        if (leaderboardGroup == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard group with name '"
                            + StringEscapeUtils.escapeHtml(leaderboardGroupName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            if (getSecurityService().hasCurrentUserReadPermission(leaderboardGroup)) {
                TimePoint timePoint = MillisecondsTimePoint.now();
                JSONObject jsonLeaderboardGroup = new JSONObject();
                jsonLeaderboardGroup.put("name", leaderboardGroup.getName());
                jsonLeaderboardGroup.put("id", leaderboardGroup.getId().toString());
                jsonLeaderboardGroup.put("description", leaderboardGroup.getDescription());
                jsonLeaderboardGroup.put("timepoint", timePoint.toString());
                JSONArray jsonLeaderboardEntries = new JSONArray();
                jsonLeaderboardGroup.put("leaderboards", jsonLeaderboardEntries);
                for (Leaderboard leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (getSecurityService().hasCurrentUserReadPermission(leaderboard)) {

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
                            jsonLeaderboard.put("lastScoringUpdate",
                                    lastUpdateTimepoint != null ? lastUpdateTimepoint.asDate().toString() : null);
                        } else {
                            jsonLeaderboard.put("scoringComment", null);
                            jsonLeaderboard.put("lastScoringUpdate", null);
                        }

                        final List<Triple<String, Iterable<Fleet>, Iterable<? extends RaceColumn>>> seriesNameAndFleetsAndRaceColumnsOfSeries = new ArrayList<>();
                        final Map<String, Boolean> medalSeriesNames = new HashMap<>();
                        if (isRegattaLeaderboard) {
                            RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                            Regatta regatta = regattaLeaderboard.getRegatta();
                            jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());
                            jsonLeaderboard.put("regattaName", regatta.getName());
                            for (final Series series : regatta.getSeries()) {
                                List<Fleet> fleets = new ArrayList<>();
                                Util.addAll(series.getFleets(), fleets);
                                seriesNameAndFleetsAndRaceColumnsOfSeries
                                        .add(new Triple<String, Iterable<Fleet>, Iterable<? extends RaceColumn>>(
                                                series.getName(), fleets, series.getRaceColumns()));
                                medalSeriesNames.put(series.getName(), series.isMedal());
                            }
                        } else {
                            jsonLeaderboard.put("scoringScheme", leaderboard.getScoringScheme().getType());
                            jsonLeaderboard.put("regattaName", null);
                            // write a 'default' series to conform with our common regatta structure
                            seriesNameAndFleetsAndRaceColumnsOfSeries
                                    .add(new Triple<String, Iterable<Fleet>, Iterable<? extends RaceColumn>>(
                                            LeaderboardNameConstants.DEFAULT_SERIES_NAME,
                                            Collections.singleton(
                                                    leaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)),
                                            leaderboard.getRaceColumns()));
                        }
                        JSONArray jsonSeriesEntries = new JSONArray();
                        jsonLeaderboard.put("series", jsonSeriesEntries);
                        for (final Triple<String, Iterable<Fleet>, Iterable<? extends RaceColumn>> e : seriesNameAndFleetsAndRaceColumnsOfSeries) {
                            JSONObject jsonSeries = new JSONObject();
                            jsonSeriesEntries.add(jsonSeries);
                            jsonSeries.put("name", e.getA());
                            jsonSeries.put("isMedalSeries", medalSeriesNames.get(e.getA()));
                            JSONArray jsonFleetsEntries = new JSONArray();
                            jsonSeries.put("fleets", jsonFleetsEntries);
                            for (final Fleet fleet : e.getB()) {
                                if (fleet != null) {
                                    JSONObject jsonFleet = new JSONObject();
                                    jsonFleet.put("name", fleet.getName());
                                    jsonFleet.put("color",
                                            fleet.getColor() != null ? fleet.getColor().getAsHtml() : null);
                                    jsonFleet.put("ordering", fleet.getOrdering());
                                    jsonFleetsEntries.add(jsonFleet);
                                    JSONArray jsonRacesEntries = new JSONArray();
                                    jsonFleet.put("races", jsonRacesEntries);
                                    for (RaceColumn raceColumn : e.getC()) {
                                        JSONObject jsonRaceColumn = new JSONObject();
                                        jsonRaceColumn.put("name", raceColumn.getName());
                                        jsonRaceColumn.put("isMedalRace", raceColumn.isMedalRace());
                                        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                                        if (trackedRace != null) {
                                            jsonRaceColumn.put("isTracked", true);
                                            jsonRaceColumn.put("regattaName",
                                                    trackedRace.getTrackedRegatta().getRegatta().getName());
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
                    }
                }
                String json = jsonLeaderboardGroup.toJSONString();
                response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8")
                        .build();
            } else {
                response = Response.status(Status.FORBIDDEN).build();
            }
        }
        return response;
    }
}
