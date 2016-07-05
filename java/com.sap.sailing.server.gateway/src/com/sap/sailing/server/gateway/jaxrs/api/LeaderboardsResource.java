package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.OpenEndedDeviceMappingFinder;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.RaceColumnConstants;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.security.Permission.Mode;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.FlatGPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FlatGPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkJsonSerializerWithPosition;
import com.sap.sse.common.Named;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
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
            jsonLeaderboards.add(leaderboardName);
        }
        String json = jsonLeaderboards.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{name}")
    public Response getLeaderboard(@PathParam("name") String leaderboardName,
            @DefaultValue("Live") @QueryParam("resultState") ResultStates resultState,
            @QueryParam("maxCompetitorsCount") Integer maxCompetitorsCount) {
        Response response;

        TimePoint requestTimePoint = MillisecondsTimePoint.now();
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
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
                response = Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
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
                /* fillTotalPointsUncorrected */false);

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
            jsonCompetitor.put("netPoints", leaderboardRowDTO.netPoints);
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
                jsonEntry.put("totalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("uncorrectedTotalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("netPoints", leaderboardEntry.netPoints);
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
        HasRegattaLike hasRegattaLike = (HasRegattaLike) leaderboard;
        DomainFactory domainFactory = getService().getDomainObjectFactory().getBaseDomainFactory();
        AbstractLogEventAuthor author = new LogEventAuthorImpl(AbstractLogEventAuthor.NAME_COMPATIBILITY,
                AbstractLogEventAuthor.PRIORITY_COMPATIBILITY);
        RegattaLogDeviceMappingEventImpl<? extends Named> event;
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
        final TimePoint now = MillisecondsTimePoint.now();
        String competitorId = (String) requestObject.get(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING);
        String markId = (String) requestObject.get(DeviceMappingConstants.JSON_MARK_ID_AS_STRING);
        String deviceUuid = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_UUID);
        Long fromMillis = (Long) requestObject.get(DeviceMappingConstants.JSON_FROM_MILLIS);

        // don't need the device type and push ID yet - important once we start add support for push notifications
        // String deviceType = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_TYPE);
        // String pushDeviceId = (String) requestObject.get(DeviceMappingConstants.JSON_PUSH_DEVICE_ID);

        if ((competitorId == null && markId == null) || deviceUuid == null || fromMillis == null) {
            // || deviceType == null
            logger.warning("Invalid JSON body in request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        DeviceIdentifier device = new SmartphoneUUIDIdentifierImpl(UUID.fromString(deviceUuid));
        TimePoint from = new MillisecondsTimePoint(fromMillis);
        // TODO: use device type and pushDeviceId
        final Named mappedTo;
        if (competitorId != null) {
            // map to a competitor
            final Competitor mappedToCompetitor = domainFactory.getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
            mappedTo = mappedToCompetitor;
            if (mappedToCompetitor == null) {
                logger.warning("No competitor found for id " + competitorId);
                return Response.status(Status.BAD_REQUEST).entity("No competitor found for id " + StringEscapeUtils.escapeHtml(competitorId))
                        .type(MediaType.TEXT_PLAIN).build();
            }
            Set<Competitor> registered = (Set<Competitor>) hasRegattaLike.getAllCompetitors();
            if (!registered.contains(mappedToCompetitor)) {
                logger.warning("Competitor found but not registered on a race of " + leaderboardName);
                return Response.status(Status.BAD_REQUEST)
                        .entity("Competitor found but not registered on a race of " + StringEscapeUtils.escapeHtml(leaderboardName))
                        .type(MediaType.TEXT_PLAIN).build();
            }
            event = new RegattaLogDeviceCompetitorMappingEventImpl(now, now, author, UUID.randomUUID(), mappedToCompetitor, device,
                    from, /* to */ null);
        } else {
            // map to a mark
            final Mark mappedToMark = domainFactory.getExistingMarkById(Helpers.tryUuidConversion(markId));
            mappedTo = mappedToMark;
            if (mappedToMark == null) {
                logger.warning("No mark found for id " + markId);
                return Response.status(Status.BAD_REQUEST).entity("No mark found for id " + StringEscapeUtils.escapeHtml(markId))
                        .type(MediaType.TEXT_PLAIN).build();
            }
            event = new RegattaLogDeviceMarkMappingEventImpl(now, now, author, UUID.randomUUID(), mappedToMark, device,
                    from, /* to */ null);
        }
        hasRegattaLike.getRegattaLike().getRegattaLog().add(event);
        logger.fine("Successfully checked in "+((markId!=null)?"mark ":"competitor ") + mappedTo.getName());
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
        final TimePoint now = MillisecondsTimePoint.now();
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
        Long toMillis = (Long) requestObject.get(DeviceMappingConstants.JSON_TO_MILLIS);
        String competitorId = (String) requestObject.get(DeviceMappingConstants.JSON_COMPETITOR_ID_AS_STRING);
        String markId = (String) requestObject.get(DeviceMappingConstants.JSON_MARK_ID_AS_STRING);
        String deviceUuid = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_UUID);
        TimePoint closingTimePoint = new MillisecondsTimePoint(toMillis);
        if (toMillis == null || deviceUuid == null || closingTimePoint == null ||
                (competitorId == null && markId == null)) {
            logger.warning("Invalid JSON body in request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        final NamedWithID mappedTo;
        if (competitorId != null) {
            final Competitor mappedToCompetitor = getService().getCompetitorStore().getExistingCompetitorByIdAsString(competitorId);
            mappedTo = mappedToCompetitor;
            if (mappedToCompetitor == null) {
                logger.warning("No competitor found for id " + competitorId);
                return Response.status(Status.BAD_REQUEST).entity("No competitor found for id " + competitorId)
                        .type(MediaType.TEXT_PLAIN).build();
            }
        } else {
            // map to mark
            DomainFactory domainFactory = getService().getDomainObjectFactory().getBaseDomainFactory();
            final Mark mappedToMark = domainFactory.getExistingMarkById(Helpers.tryUuidConversion(markId));
            mappedTo = mappedToMark;
            if (mappedToMark == null) {
                logger.warning("No mark found for id " + markId);
                return Response.status(Status.BAD_REQUEST).entity("No mark found for id " + markId)
                        .type(MediaType.TEXT_PLAIN).build();
            }
            
        }
        OpenEndedDeviceMappingFinder finder = new OpenEndedDeviceMappingFinder(isRegattaLike.getRegattaLog(), mappedTo, deviceUuid);
        Serializable deviceMappingEventId = finder.analyze();
        if (deviceMappingEventId == null) {
            logger.warning("No corresponding open mark to device mapping has been found");
            return Response.status(Status.BAD_REQUEST)
                    .entity("No corresponding open mark to device mapping has been found")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        RegattaLogCloseOpenEndedDeviceMappingEventImpl event = new RegattaLogCloseOpenEndedDeviceMappingEventImpl(now,
                author, deviceMappingEventId, closingTimePoint);
        isRegattaLike.getRegattaLog().add(event);
        logger.fine("Successfully checked out "+((markId!=null)?"mark ":"competitor ") + mappedTo.getName());
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
                    .entity("Could not find a competitor with id '" + StringEscapeUtils.escapeHtml(competitorIdAsString) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            JSONObject json = CompetitorsResource.getCompetitorJSON(competitor);
            json.put("displayName", leaderboard.getDisplayName(competitor));
            response = Response.ok(json.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
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
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        if (!(leaderboard instanceof HasRegattaLike)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Leaderboard with name '" + leaderboardName + "'does not contain a RegattaLog'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        final Set<Mark> marks = new HashSet<Mark>();
        if (raceColumnName == null) {
            if (fleetName != null) {
                return Response
                        .status(Status.BAD_REQUEST)
                        .entity("Either specify neither raceColumnName nor fleetName, only raceColumnName, or raceColumnName and fleetName but not only fleetName")
                        .type(MediaType.TEXT_PLAIN).build();
            } else {
                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                    Util.addAll(raceColumn.getAvailableMarks(), marks);
                }
            }
        } else {
            RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn == null) {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity("Could not find a race column '" + StringEscapeUtils.escapeHtml(raceColumnName) + "' in leaderboard '"
                                + StringEscapeUtils.escapeHtml(leaderboardName) + "'.").type(MediaType.TEXT_PLAIN).build();
            } else if (fleetName != null) {
                Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet == null) {
                    return Response
                            .status(Status.NOT_FOUND)
                            .entity("Could not find fleet '" + StringEscapeUtils.escapeHtml(fleetName) + "' in leaderboard '" +
                                    StringEscapeUtils.escapeHtml(leaderboardName)
                                    + "'.").type(MediaType.TEXT_PLAIN).build();
                } else {
                    Util.addAll(raceColumn.getAvailableMarks(fleet), marks);
                }
            } else {
                // Return all marks for a certain race column
                // if all races have a tracked race return all marks part of at least one tracked race
                // if at least one race doesn't have a tracked race, return also the marks defined in the RegattaLog
                Util.addAll(raceColumn.getAvailableMarks(), marks);
            }
        }
        JSONArray array = new JSONArray();
        for (Mark mark : marks) {
            final TimePoint now = MillisecondsTimePoint.now();
            Position lastKnownPosition = getService().getMarkPosition(mark,
                    (LeaderboardThatHasRegattaLike) leaderboard, now);
            array.add(markWithPositionSerializer.serialize(new Pair<>(mark, lastKnownPosition)));
        }
        JSONObject result = new JSONObject();
        result.put("marks", array);
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/marks/{markId}")
    public Response getMark(@PathParam("leaderboardName") String leaderboardName,
            @PathParam("markId") String markId) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        if (!(leaderboard instanceof HasRegattaLike)) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Leaderboard with name '" + leaderboardName + "'does not contain a RegattaLog'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        Mark mark = null;
        for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (final Mark availableMark : raceColumn.getAvailableMarks()) {
                mark = availableMark;
                break;
            }
        }
        if (mark == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find a mark with ID '" + StringEscapeUtils.escapeHtml(markId) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        final TimePoint now = MillisecondsTimePoint.now();
        Position lastKnownPosition = getService().getMarkPosition(mark, (LeaderboardThatHasRegattaLike) leaderboard, now);
        final JSONObject result = markWithPositionSerializer.serialize(new Pair<>(mark, lastKnownPosition));
        return Response.ok(result.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    private final MarkJsonSerializer markSerializer = new MarkJsonSerializer();
    private final MarkJsonSerializerWithPosition markWithPositionSerializer = new MarkJsonSerializerWithPosition(
            markSerializer, new FlatGPSFixJsonSerializer());
    private final FlatGPSFixJsonDeserializer fixDeserializer = new FlatGPSFixJsonDeserializer();

    /**
     * Mockito requires this to be public in order to be able to mock it :-(
     */
    public RaceLogTrackingAdapter getRaceLogTrackingAdapter() {
        return getService(RaceLogTrackingAdapterFactory.class).getAdapter(getService().getBaseDomainFactory());
    }

    /**
     * Expects one GPS Fix in the format understood by {@link #fixDeserializer} in the POST message body, parses that fix,
     * adds the fix to the {@link RacingEventService#getGPSFixStore() GPSFixStore} and creates mappings for each fix in the RegattaLog.
     */
    @POST
    @Path("{leaderboardName}/marks/{markId}/gps_fixes")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pingMark(String json, @PathParam("leaderboardName") String leaderboardName,
            @PathParam("markId") String markId) throws HTTPException {
        logger.fine("Post issued to " + this.getClass().getName());
        final RacingEventService service = getService();
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        RegattaLog regattaLog = null;
        if (leaderboard instanceof HasRegattaLike) {
            regattaLog = ((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog();
        } else {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Leaderboard '" + StringEscapeUtils.escapeHtml(leaderboardName) + "' does not have an attached RegattaLog.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        final Mark mark = service.getBaseDomainFactory().getExistingMarkByIdAsString(markId);
        if (mark == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find a mark with ID '" + StringEscapeUtils.escapeHtml(markId) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        // grab the position as found in TrackedRaces attached to the leaderboard
        final GPSFix fix;
        try {
            Object requestBody = JSONValue.parseWithException(json);
            JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
            logger.fine("JSON requestObject is: " + requestObject.toString());
            fix = fixDeserializer.deserialize(requestObject);
        } catch (ParseException | JsonDeserializationException | NumberFormatException e) {
            logger.warning(String.format("Exception while parsing post request:\n%s", e.toString()));
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        final RaceLogTrackingAdapter adapter = getRaceLogTrackingAdapter();
        adapter.pingMark(regattaLog, mark, fix, service);
        return Response.ok().build();
    }
    
    /**
     * @param raceColumnName optional; if omitted, all race column factors in the leaderboard will be reported, otherwise only the one requested
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/racecolumnfactors")
    public Response getRaceColumnFactors(@PathParam("leaderboardName") String leaderboardName, @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName) {
        final Response response;
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            final Iterable<RaceColumn> raceColumns;
            final RaceColumn raceColumn;
            if (raceColumnName == null) {
                raceColumns = leaderboard.getRaceColumns();
                raceColumn = null;
            } else {
                raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
                raceColumns = Collections.singleton(raceColumn);
            }
            if (raceColumnName != null && raceColumn == null) {
                response = Response.status(Status.NOT_FOUND)
                        .entity("Could not find a race column named '"+StringEscapeUtils.escapeHtml(raceColumnName)+"' in leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                        .type(MediaType.TEXT_PLAIN).build();
            } else {
                final JSONObject json = getJsonForColumnFactors(leaderboard, raceColumns);
                response = Response.ok(json.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
            }
        }
        return response;
    }

    private JSONObject getJsonForColumnFactors(final Leaderboard leaderboard, final Iterable<RaceColumn> raceColumns) {
        final JSONObject json = new JSONObject();
        json.put(RaceColumnConstants.LEADERBOARD_NAME, leaderboard.getName());
        json.put(RaceColumnConstants.LEADERBOARD_DISPLAY_NAME, leaderboard.getDisplayName());
        final JSONArray raceColumnsAsJson = new JSONArray();
        json.put(RaceColumnConstants.RACE_COLUMNS, raceColumnsAsJson);
        for (final RaceColumn rc : raceColumns) {
            final JSONObject raceColumnAsJson = new JSONObject();
            raceColumnsAsJson.add(raceColumnAsJson);
            raceColumnAsJson.put(RaceColumnConstants.RACE_COLUMN_NAME, rc.getName());
            raceColumnAsJson.put(RaceColumnConstants.EXPLICIT_FACTOR, rc.getExplicitFactor());
            raceColumnAsJson.put(RaceColumnConstants.FACTOR, rc.getFactor());
        }
        return json;
    }
    
    /**
     * @param raceColumnName
     *            mandatory
     * @param explicitFactor
     *            may be {@code null} which means resetting the explicit factor and letting the leaderboard determine
     *            the column factor implicitly.
     * @see RaceColumn#setFactor(Double)
     * @return a document that contains the leaderboard "header" data and the factor data for the race column specified,
     *         after the change. This may be useful to validate the impact the change had on the resulting column factor
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/racecolumnfactors")
    public Response setExplicitRaceColumnFactor(@PathParam("leaderboardName") String leaderboardName, @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam("explicit_factor") Double explicitFactor) {
        final Response response;
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(Mode.UPDATE, leaderboard.getName()));
            final RaceColumn raceColumn;
            raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn == null) {
                response = Response.status(Status.NOT_FOUND)
                        .entity("Could not find a race column named '"+StringEscapeUtils.escapeHtml(raceColumnName)+"' in leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                        .type(MediaType.TEXT_PLAIN).build();
            } else {
                raceColumn.setFactor(explicitFactor);
                final JSONObject json = getJsonForColumnFactors(leaderboard, Collections.singleton(raceColumn));
                response = Response.ok(json.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
            }
        }
        return response;
    }
}
