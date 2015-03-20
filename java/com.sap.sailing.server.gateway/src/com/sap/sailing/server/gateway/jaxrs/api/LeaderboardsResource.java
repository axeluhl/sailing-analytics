package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.ws.http.HTTPException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.DefinedMarkFinder;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.OpenEndedDeviceMappingFinder;
import com.sap.sailing.domain.abstractlog.shared.analyzing.RegisteredCompetitorsAnalyzer;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.DynamicGPSFixTrackImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.GPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GPSFixJsonSerializer;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v1/leaderboards")
public class LeaderboardsResource extends AbstractSailingServerResource {
    private static final Logger logger = Logger.getLogger(LeaderboardsResource.class.getName());

    public enum ResultStates {
        Live, Preliminary, Final
    };

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getLeaderboards() {
        JSONArray jsonLeaderboards = new JSONArray();
        Map<String, Leaderboard> leaderboards = getService().getLeaderboards();
        for (String leaderboardName : leaderboards.keySet()) {
            if (!leaderboardName.equals(LeaderboardNameConstants.DEFAULT_LEADERBOARD_NAME))
                jsonLeaderboards.add(leaderboardName);
        }

        String json = jsonLeaderboards.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboard(@PathParam("name") String leaderboardName,
            @DefaultValue("Live") @QueryParam("resultState") ResultStates resultState,
            @DefaultValue("true") @QueryParam("useCache") boolean useCache,
            @DefaultValue("1000") @QueryParam("maxCompetitorsCount") int maxCompetitorsCount) {
        Response response;

        TimePoint requestTimePoint = MillisecondsTimePoint.now();
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + leaderboardName + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            try {
                TimePoint resultTimePoint = calculateTimePointForResultState(leaderboard, resultState);
                JSONObject jsonLeaderboard;
                if (resultTimePoint != null) {
                    Util.Triple<TimePoint, ResultStates, Integer> resultStateAndTimePoint = new Util.Triple<>(
                            resultTimePoint, resultState, maxCompetitorsCount);
                    jsonLeaderboard = getLeaderboardJson(leaderboard, resultStateAndTimePoint);
                } else {
                    jsonLeaderboard = createEmptyLeaderboardJson(leaderboard, resultState, requestTimePoint,
                            maxCompetitorsCount);
                }

                StringWriter sw = new StringWriter();
                jsonLeaderboard.writeJSONString(sw);

                String json = sw.getBuffer().toString();
                response = Response.ok(json, MediaType.APPLICATION_JSON).build();
            } catch (NoWindException | InterruptedException | ExecutionException | IOException e) {
                response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage())
                        .type(MediaType.TEXT_PLAIN).build();
            }
        }

        return response;
    }

    private JSONObject getLeaderboardJson(Leaderboard leaderboard,
            Util.Triple<TimePoint, ResultStates, Integer> timePointAndResultStateAndMaxCompetitorsCount)
            throws NoWindException, InterruptedException, ExecutionException {
        LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(
                timePointAndResultStateAndMaxCompetitorsCount.getA(), Collections.<String> emptyList(), /* addOverallDetails */
                false, getService(), getService().getBaseDomainFactory(),
                /* fillNetPointsUncorrected */false);

        TimePoint resultTimePoint = timePointAndResultStateAndMaxCompetitorsCount.getA();
        ResultStates resultState = timePointAndResultStateAndMaxCompetitorsCount.getB();
        Integer maxCompetitorsCount = timePointAndResultStateAndMaxCompetitorsCount.getC();
        JSONObject jsonLeaderboard = new JSONObject();

        writeCommonLeaderboardData(jsonLeaderboard, leaderboardDTO, resultState, resultTimePoint, maxCompetitorsCount);

        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        int counter = 1;
        for (CompetitorDTO competitor : leaderboardDTO.competitors) {
            LeaderboardRowDTO leaderboardRowDTO = leaderboardDTO.rows.get(competitor);

            if (maxCompetitorsCount != null && counter > maxCompetitorsCount) {
                break;
            }
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboardDTO.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName == null ? competitor.getName() : displayName);
            jsonCompetitor.put("id", competitor.getIdAsString());
            jsonCompetitor.put("sailID", competitor.getSailID());
            jsonCompetitor.put("nationality", competitor.getThreeLetterIocCountryCode());
            jsonCompetitor.put("countryCode", competitor.getTwoLetterIsoCountryCode());

            jsonCompetitor.put("rank", counter);
            jsonCompetitor.put("carriedPoints", leaderboardRowDTO.carriedPoints);
            jsonCompetitor.put("totalPoints", leaderboardRowDTO.totalPoints);
            jsonCompetitorEntries.add(jsonCompetitor);
            JSONObject jsonRaceColumns = new JSONObject();
            jsonCompetitor.put("raceScores", jsonRaceColumns);
            for (RaceColumnDTO raceColumn : leaderboardDTO.getRaceList()) {
                List<CompetitorDTO> regattaRankedCompetitorsForColumn = leaderboardDTO
                        .getCompetitorOrderingPerRaceColumnName().get(raceColumn.getName());
                JSONObject jsonEntry = new JSONObject();
                jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                LeaderboardEntryDTO leaderboardEntry = leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumn
                        .getName());

                final FleetDTO fleetOfCompetitor = leaderboardEntry.fleet;
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                jsonEntry.put("netPoints", leaderboardEntry.netPointsCorrected);
                jsonEntry.put("uncorrectedNetPoints", leaderboardEntry.netPoints);
                jsonEntry.put("totalPoints", leaderboardEntry.totalPoints);
                MaxPointsReason maxPointsReason = leaderboardEntry.reasonForMaxPoints;
                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString() : null);
                jsonEntry.put("rank", regattaRankedCompetitorsForColumn.indexOf(competitor) + 1);
                List<CompetitorDTO> raceRankedCompetitorsInColumn = leaderboardDTO
                        .getCompetitorsFromBestToWorst(raceColumn);
                jsonEntry.put("raceRank", raceRankedCompetitorsInColumn.indexOf(competitor) + 1);
                jsonEntry.put("isDiscarded", leaderboardEntry.discarded);
                jsonEntry.put("isCorrected", leaderboardEntry.hasScoreCorrection());
            }
            counter++;
        }
        return jsonLeaderboard;
    }

    private JSONObject createEmptyLeaderboardJson(Leaderboard leaderboard, ResultStates resultState,
            TimePoint requestTimePoint, Integer maxCompetitorsCount) throws NoWindException {
        JSONObject jsonLeaderboard = new JSONObject();

        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, null, maxCompetitorsCount);

        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        for (Competitor competitor : leaderboard.getCompetitors()) {
            JSONObject jsonCompetitor = new JSONObject();
            jsonCompetitor.put("name", competitor.getName());
            final String displayName = leaderboard.getDisplayName(competitor);
            jsonCompetitor.put("displayName", displayName == null ? competitor.getName() : displayName);
            jsonCompetitor.put("id", competitor.getId().toString());
            jsonCompetitor.put("sailID", competitor.getBoat().getSailID());
            Nationality nationality = competitor.getTeam().getNationality();
            jsonCompetitor.put("nationality", nationality != null ? nationality.getThreeLetterIOCAcronym() : null);
            jsonCompetitor.put("countryCode", nationality != null ? (nationality.getCountryCode() != null ? nationality
                    .getCountryCode().getTwoLetterISOCode() : null) : null);

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
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                jsonEntry.put("netPoints", null);
                jsonEntry.put("totalPoints", null);
                jsonEntry.put("maxPointsReason", "");
                jsonEntry.put("rank", 0);
                jsonEntry.put("isDiscarded", false);
                jsonEntry.put("isCorrected", false);
            }
        }
        return jsonLeaderboard;
    }

    private void writeCommonLeaderboardData(JSONObject jsonLeaderboard, LeaderboardDTO leaderboard,
            ResultStates resultState, TimePoint resultTimePoint, Integer maxCompetitorsCount) {
        jsonLeaderboard.put("name", leaderboard.name);

        jsonLeaderboard.put("resultTimepoint", resultTimePoint != null ? resultTimePoint.asMillis() : null);
        jsonLeaderboard.put("resultState", resultState.name());
        jsonLeaderboard.put("maxCompetitorsCount", maxCompetitorsCount);
        jsonLeaderboard.put("higherScoreIsBetter", leaderboard.isHigherScoreBetter());
        jsonLeaderboard.put("scoringComment", leaderboard.getComment());
        Date lastUpdateTimepoint = leaderboard.getTimePointOfLastCorrectionsValidity();
        jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.getTime() : null);

        JSONArray jsonColumnNames = new JSONArray();
        jsonLeaderboard.put("columnNames", jsonColumnNames);
        for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
            jsonColumnNames.add(raceColumn.getName());
        }
    }

    private void writeCommonLeaderboardData(JSONObject jsonLeaderboard, Leaderboard leaderboard,
            ResultStates resultState, TimePoint resultTimePoint, Integer maxCompetitorsCount) {
        jsonLeaderboard.put("name", leaderboard.getName());

        jsonLeaderboard.put("resultTimepoint", resultTimePoint != null ? resultTimePoint.asMillis() : null);
        jsonLeaderboard.put("resultState", resultState.name());
        jsonLeaderboard.put("maxCompetitorsCount", maxCompetitorsCount);

        SettableScoreCorrection scoreCorrection = leaderboard.getScoreCorrection();
        if (scoreCorrection != null) {
            jsonLeaderboard.put("scoringComment", scoreCorrection.getComment());
            TimePoint lastUpdateTimepoint = scoreCorrection.getTimePointOfLastCorrectionsValidity();
            jsonLeaderboard.put("lastScoringUpdate", lastUpdateTimepoint != null ? lastUpdateTimepoint.asMillis()
                    : null);
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

    private TimePoint calculateTimePointForResultState(Leaderboard leaderboard, ResultStates resultState) {
        TimePoint result = null;
        switch (resultState) {
        case Live:
            result = leaderboard.getTimePointOfLatestModification();
            if (result == null) {
                result = MillisecondsTimePoint.now();
            }
            break;
        case Preliminary:
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{name}/device_mappings/start")
    public Response postCheckin(String checkinJson, @PathParam("name") String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (!leaderboardIsValid(leaderboard)) {
            logger.warning("Leaderboard does not exist or does not hold a RegattaLog");
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Leaderboard does not exist or does not hold a RegattaLog").type(MediaType.TEXT_PLAIN)
                    .build();
        }
        IsRegattaLike isRegattaLike = ((HasRegattaLike) leaderboard).getRegattaLike();
        DomainFactory domainFactory = getService().getDomainObjectFactory().getBaseDomainFactory();
        AbstractLogEventAuthor author = new LogEventAuthorImpl(AbstractLogEventAuthor.NAME_COMPATIBILITY,
                AbstractLogEventAuthor.PRIORITY_COMPATIBILITY);
        RegattaLogDeviceCompetitorMappingEventImpl event;
        JSONObject requestObject;
        try {
            logger.fine("Post issued to " + this.getClass().getName());
            Object requestBody = JSONValue.parseWithException(checkinJson);
            requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.fine("JSON requestObject is: " + requestObject.toString());
        } catch (ParseException | JsonDeserializationException e) {
            logger.log(Level.WARNING, "Exception while parsing post request", e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        String competitorId = (String) requestObject.get(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING);
        String deviceUuid = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_UUID);
        Long fromMillis = (Long) requestObject.get(DeviceMappingConstants.JSON_FROM_MILLIS);

        // don't need the device type and push ID yet - important once we start add support for push notifications
        // String deviceType = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_TYPE);
        // String pushDeviceId = (String) requestObject.get(DeviceMappingConstants.JSON_PUSH_DEVICE_ID);

        if (competitorId == null || deviceUuid == null || fromMillis == null) {
            // || deviceType == null
            logger.warning("Invalid JSON body in request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        // TODO: use device type and pushDeviceId
        Competitor mappedTo = domainFactory.getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
        if (mappedTo == null) {
            logger.warning("No competitor found for id " + competitorId);
            return Response.status(Status.BAD_REQUEST).entity("No competitor found for id " + competitorId)
                    .type(MediaType.TEXT_PLAIN).build();
        }
        // add registration if necessary
        Set<Competitor> registered = new RegisteredCompetitorsAnalyzer<>(isRegattaLike.getRegattaLog()).analyze();
        if (!registered.contains(mappedTo)) {
            isRegattaLike.getRegattaLog().add(
                    new RegattaLogRegisterCompetitorEventImpl(now, author, now, UUID.randomUUID(), mappedTo));
        }
        DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(deviceUuid));
        TimePoint from = new MillisecondsTimePoint(fromMillis);
        event = new RegattaLogDeviceCompetitorMappingEventImpl(now, author, now, UUID.randomUUID(), mappedTo, device,
                from, null);
        isRegattaLike.getRegattaLog().add(event);
        logger.fine("Successfully checked in competitor " + mappedTo.getName());
        return Response.status(Status.OK).build();
    }

    private boolean leaderboardIsValid(Leaderboard leaderboard) {
        if (leaderboard == null) {
            logger.warning("Could not find a leaderboard with the given name");
            return false;
        }

        if (!(leaderboard instanceof HasRegattaLike)) {
            logger.warning("Specified Leaderboard does not have a RegattaLike child (is not a RegattaLeaderboard/FlexibleLeaderboard)");
            return false;
        }

        return true;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{name}/device_mappings/end")
    public Response postCheckout(String json, @PathParam("name") String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);

        if (!leaderboardIsValid(leaderboard)) {
            logger.warning("Leaderboard does not exist or does not hold a RegattaLog");
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Leaderboard does not exist or does not hold a RegattaLog").type(MediaType.TEXT_PLAIN)
                    .build();
        }

        IsRegattaLike isRegattaLike = ((HasRegattaLike) leaderboard).getRegattaLike();

        AbstractLogEventAuthor author = new LogEventAuthorImpl(AbstractLogEventAuthor.NAME_COMPATIBILITY,
                AbstractLogEventAuthor.PRIORITY_COMPATIBILITY);

        MillisecondsTimePoint now = MillisecondsTimePoint.now();

        logger.fine("Post issued to " + this.getClass().getName());
        Object requestBody;
        JSONObject requestObject;
        try {
            requestBody = JSONValue.parseWithException(json);
            requestObject = Helpers.toJSONObjectSafe(requestBody);
        } catch (ParseException | JsonDeserializationException e) {
            logger.warning(String.format("Exception while parsing post request:\n%s", e.toString()));
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        logger.fine("JSON requestObject is: " + requestObject.toString());
        Long toMillis = (Long) requestObject.get("toMillis");
        String competitorId = (String) requestObject.get("competitorId");
        String deviceUuid = (String) requestObject.get("deviceUuid");
        TimePoint closingTimePoint = new MillisecondsTimePoint(toMillis);

        if (toMillis == null || deviceUuid == null || closingTimePoint == null) {
            logger.warning("Invalid JSON body in request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        Competitor mappedTo = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
        if (mappedTo == null) {
            logger.warning("No competitor found for id " + competitorId);
            return Response.status(Status.BAD_REQUEST).entity("No competitor found for id " + competitorId)
                    .type(MediaType.TEXT_PLAIN).build();
        }

        OpenEndedDeviceMappingFinder finder = new OpenEndedDeviceMappingFinder(isRegattaLike.getRegattaLog(), mappedTo,
                deviceUuid);
        Serializable deviceMappingEventId = finder.analyze();

        if (deviceMappingEventId == null) {
            logger.warning("No corresponding open competitor to device mapping has been found");
            return Response.status(Status.BAD_REQUEST)
                    .entity("No corresponding open competitor to device mapping has been found")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        RegattaLogCloseOpenEndedDeviceMappingEventImpl event = new RegattaLogCloseOpenEndedDeviceMappingEventImpl(now,
                author, now, UUID.randomUUID(), deviceMappingEventId, closingTimePoint);

        isRegattaLike.getRegattaLog().add(event);
        logger.fine("Successfully checked out competitor " + mappedTo.getName());
        return Response.status(Status.OK).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{leaderboardName}/competitors/{competitorId}")
    public Response getCompetitor(@PathParam("leaderboardName") String leaderboardName,
            @PathParam("competitorId") String competitorIdAsString) {
        Response response;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        Competitor competitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(
                competitorIdAsString);

        if (competitor == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a competitor with id '" + competitorIdAsString + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + leaderboardName + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            JSONObject json = CompetitorsResource.getCompetitorJSON(competitor);
            json.put("displayName", leaderboard.getDisplayName(competitor));
            response = Response.ok(json.toJSONString(), MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/marks")
    public Response getMarksForRace(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName) {
        // TODO also look for defined marks in RegattaLog?

        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + leaderboardName + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        Collection<Mark> marks = new HashSet<Mark>();
        if (raceColumnName == null && fleetName != null) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .entity("Either specify neither raceColumnName nor fleetName, only raceColumnName, or raceColumnName and fleetName but not only fleetName")
                    .type(MediaType.TEXT_PLAIN).build();
        } else if (raceColumnName == null && fleetName == null) {
            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                for (Fleet fleet : raceColumn.getFleets()) {
                    RaceLog raceLog = raceColumn.getRaceLog(fleet);
                    TrackedRace trackedRace = raceColumn.getTrackedRace(fleet); // might not yet be attached
                    marks.addAll(new DefinedMarkFinder(raceLog).analyze());
                    if (trackedRace != null) {
                        Util.addAll(trackedRace.getMarks(), marks);
                    }
                }
            }
        } else if (raceColumnName != null) {

            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn == null) {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity("Could not find a race column '" + raceColumnName + "' in leaderboard '"
                                + leaderboardName + "'.").type(MediaType.TEXT_PLAIN).build();
            }

            if (fleetName != null) {
                Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet == null) {
                    return Response
                            .status(Status.NOT_FOUND)
                            .entity("Could not find fleet '" + fleetName + "' in leaderboard '" + leaderboardName
                                    + "'.").type(MediaType.TEXT_PLAIN).build();
                }

                marks = getMarksForFleet(raceColumn, fleet);
            } else {
                // Return all marks for a certain race column
                for (Fleet fleet : raceColumn.getFleets()) {
                    Util.addAll(getMarksForFleet(raceColumn, fleet), marks);
                }
            }
        }

        JSONArray array = new JSONArray();
        for (Mark mark : marks) {
            array.add(markSerializer.serialize(mark));
        }

        return Response.ok(array.toJSONString(), MediaType.APPLICATION_JSON).build();
    }

    private Collection<Mark> getMarksForFleet(RaceColumn raceColumn, Fleet fleet) {
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        TrackedRace trackedRace = raceColumn.getTrackedRace(fleet); // might not yet be attached

        List<Mark> marks = new ArrayList<>();

        marks.addAll(new DefinedMarkFinder(raceLog).analyze());
        if (trackedRace != null) {
            Util.addAll(trackedRace.getMarks(), marks);
        }

        return marks;
    }

    private final MarkJsonSerializer markSerializer = new MarkJsonSerializer();

    private final GPSFixJsonDeserializer fixDeserializer = new GPSFixJsonDeserializer();

    public RaceLogTrackingAdapter getRaceLogTrackingAdapter() {
        return getService(RaceLogTrackingAdapterFactory.class).getAdapter(getService().getBaseDomainFactory());
    }

    @POST
    @Path("{leaderboardName}/marks/{markId}/gps_fixes")
    @Consumes(MediaType.APPLICATION_JSON)
    /**
     * Add the fixes to the GPSFixStore and create mappings for each fix in the RegattaLog.
     * @param json
     * @param leaderboardName
     * @param markId
     * @return
     * @throws HTTPException
     */
    public Response pingMark(String json, @PathParam("leaderboardName") String leaderboardName,
            @PathParam("markId") String markId) throws HTTPException {
        logger.fine("Post issued to " + this.getClass().getName());

        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + leaderboardName + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        RegattaLog regattaLog = null;
        if (leaderboard instanceof HasRegattaLike) {
            regattaLog = ((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog();
        } else {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Leaderboard '" + leaderboardName + "' does not have an attached RegattaLog.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        Mark mark = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markId);
        if (mark == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find a mark with ID '" + markId + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        GPSFix lastKnownFix = getLastKnownFix(leaderboard, mark, regattaLog);

        GPSFix fix = null;
        try {
            Object requestBody = JSONValue.parseWithException(json);
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.fine("JSON requestObject is: " + requestObject.toString());
            fix = fixDeserializer.deserialize(requestObject);
        } catch (ParseException | JsonDeserializationException e) {
            logger.warning(String.format("Exception while parsing post request:\n%s", e.toString()));
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        RaceLogTrackingAdapter adapter = getRaceLogTrackingAdapter();
        RacingEventService service = getService();

        try {
            adapter.pingMark(regattaLog, mark, fix, service);
            logger.log(Level.INFO, "Pinged mark " + mark.getName());
        } catch (NoCorrespondingServiceRegisteredException e) {
            logger.log(Level.WARNING, "Could not ping mark " + mark.getName());
        }

        if (lastKnownFix != null) {
            GPSFixJsonSerializer serializer = new GPSFixJsonSerializer();
            JSONObject lastKnownFixJson = serializer.serialize(lastKnownFix);
            String fixJson = lastKnownFixJson.toJSONString();
            return Response.ok(fixJson, MediaType.APPLICATION_JSON).build();
        } else {
            return Response.ok().build();
        }
    }

    private GPSFix getLastKnownFix(Leaderboard leaderboard, Mark mark, RegattaLog regattaLog) {
        DynamicGPSFixTrack<Mark, GPSFix> track = new DynamicGPSFixTrackImpl<Mark>(mark, 0);
        try {
            getService().getGPSFixStore().loadMarkTrack(track, regattaLog, mark);
        } catch (Exception e) {
            return null;
        }

        GPSFix lastKnownFix = track.getLastFixAtOrBefore(MillisecondsTimePoint.now());
        return lastKnownFix;
    }
}
