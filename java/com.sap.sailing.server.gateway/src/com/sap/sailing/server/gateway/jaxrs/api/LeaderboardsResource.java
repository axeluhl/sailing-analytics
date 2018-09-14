package com.sap.sailing.server.gateway.jaxrs.api;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogUseCompetitorsFromRaceLogEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.RegattaLogDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogCloseOpenEndedDeviceMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceBoatMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceCompetitorMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDeviceMarkMappingEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.impl.OpenEndedDeviceMappingFinder;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.AbstractRaceColumn;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.DeviceIdentifier;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.RaceColumnConstants;
import com.sap.sailing.domain.common.racelog.RaceLogServletConstants;
import com.sap.sailing.domain.common.racelog.tracking.DeviceMappingConstants;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotableForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.scalablevalue.impl.ScalableBearing;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.domain.common.sharding.ShardingType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.racelogtracking.impl.SmartphoneUUIDIdentifierImpl;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.regattalike.LeaderboardThatHasRegattaLike;
import com.sap.sailing.domain.sharding.ShardingContext;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.FlatGPSFixJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorAndBoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FlatGPSFixJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkJsonSerializerWithPosition;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Named;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.impl.UUIDHelper;
import com.sap.sse.security.shared.HasPermissions.DefaultModes;

@Path("/v1/leaderboards")
public class LeaderboardsResource extends AbstractLeaderboardsResource {
    private static final Logger logger = Logger.getLogger(LeaderboardsResource.class.getName());

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
        ShardingContext.setShardingConstraint(ShardingType.LEADERBOARDNAME, leaderboardName);
        
        try {
            Response response;
            TimePoint requestTimePoint = MillisecondsTimePoint.now();
            Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
            if (leaderboard == null) {
                response = Response.status(Status.NOT_FOUND)
                        .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                        .type(MediaType.TEXT_PLAIN).build();
            } else {
                try {
                    TimePoint timePoint = calculateTimePointForResultState(leaderboard, resultState);
                    JSONObject jsonLeaderboard;
                    jsonLeaderboard = getLeaderboardJson(resultState, maxCompetitorsCount, requestTimePoint, leaderboard, timePoint,
                            /* race column names */ null, /* race detail names */ null);
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
        } finally {
            ShardingContext.clearShardingConstraint(ShardingType.LEADERBOARDNAME);
        }
    }

    @Override
    protected JSONObject getLeaderboardJson(Leaderboard leaderboard,
            TimePoint resultTimePoint, ResultStates resultState, Integer maxCompetitorsCount, List<String> raceColumnNames,
            List<String> raceDetailNames)
            throws NoWindException, InterruptedException, ExecutionException {
        LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(
                resultTimePoint, Collections.<String> emptyList(), /* addOverallDetails */
                false, getService(), getService().getBaseDomainFactory(),
                /* fillTotalPointsUncorrected */false);
        JSONObject jsonLeaderboard = new JSONObject();
        writeCommonLeaderboardData(jsonLeaderboard, leaderboard, resultState, leaderboardDTO.getTimePoint(), maxCompetitorsCount);
        JSONArray jsonCompetitorEntries = new JSONArray();
        jsonLeaderboard.put("competitors", jsonCompetitorEntries);
        jsonLeaderboard.put("ShardingLeaderboardName", ShardingType.LEADERBOARDNAME.encodeIfNeeded(leaderboard.getName()));
        int counter = 1;
        for (CompetitorDTO competitor : leaderboardDTO.competitors) {
            LeaderboardRowDTO leaderboardRowDTO = leaderboardDTO.rows.get(competitor);
            if (maxCompetitorsCount != null && counter > maxCompetitorsCount) {
                break;
            }
            JSONObject jsonCompetitor = new JSONObject();
            writeCompetitorBaseData(jsonCompetitor, competitor, leaderboardDTO);
            jsonCompetitor.put("rank", counter);
            jsonCompetitor.put("carriedPoints", leaderboardRowDTO.carriedPoints);
            jsonCompetitor.put("netPoints", leaderboardRowDTO.netPoints);
            jsonCompetitorEntries.add(jsonCompetitor);
            JSONObject jsonRaceColumns = new JSONObject();
            jsonCompetitor.put("raceScores", jsonRaceColumns);
            for (RaceColumnDTO raceColumn : leaderboardDTO.getRaceList()) {
                List<CompetitorDTO> regattaRankedCompetitorsForColumn = leaderboardDTO.getCompetitorOrderingPerRaceColumnName().get(raceColumn.getName());
                JSONObject jsonEntry = new JSONObject();
                jsonRaceColumns.put(raceColumn.getName(), jsonEntry);
                LeaderboardEntryDTO leaderboardEntry = leaderboardRowDTO.fieldsByRaceColumnName.get(raceColumn.getName());
                final FleetDTO fleetOfCompetitor = leaderboardEntry.fleet;
                jsonEntry.put("fleet", fleetOfCompetitor == null ? "" : fleetOfCompetitor.getName());
                jsonEntry.put("totalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("uncorrectedTotalPoints", leaderboardEntry.totalPoints);
                jsonEntry.put("netPoints", leaderboardEntry.netPoints);
                MaxPointsReason maxPointsReason = leaderboardEntry.reasonForMaxPoints;
                jsonEntry.put("maxPointsReason", maxPointsReason != null ? maxPointsReason.toString() : null);
                jsonEntry.put("rank", regattaRankedCompetitorsForColumn.indexOf(competitor) + 1);
                List<CompetitorDTO> raceRankedCompetitorsInColumn = leaderboardDTO.getCompetitorsFromBestToWorst(raceColumn);
                jsonEntry.put("raceRank", raceRankedCompetitorsInColumn.indexOf(competitor) + 1);
                jsonEntry.put("isDiscarded", leaderboardEntry.discarded);
                jsonEntry.put("isCorrected", leaderboardEntry.hasScoreCorrection());
            }
            counter++;
        }
        return jsonLeaderboard;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{name}/device_mappings/start")
    public Response postCheckin(String checkinJson, @PathParam("name") String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (!isValidLeaderboard(leaderboard)) {
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
        String boatId = (String) requestObject.get(DeviceMappingConstants.JSON_BOAT_ID_AS_STRING);
        String markId = (String) requestObject.get(DeviceMappingConstants.JSON_MARK_ID_AS_STRING);
        String deviceUuid = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_UUID);
        Long fromMillis = (Long) requestObject.get(DeviceMappingConstants.JSON_FROM_MILLIS);

        // don't need the device type and push ID yet - important once we start add support for push notifications
        // String deviceType = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_TYPE);
        // String pushDeviceId = (String) requestObject.get(DeviceMappingConstants.JSON_PUSH_DEVICE_ID);

        if ((competitorId == null && boatId == null && markId == null) || deviceUuid == null || fromMillis == null) {
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
            final Competitor mappedToCompetitor = domainFactory.getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(competitorId);
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
        } else if (boatId != null) {
            // map to a boat
            final Boat mappedToBoat = domainFactory.getCompetitorAndBoatStore().getExistingBoatByIdAsString(boatId);
            mappedTo = mappedToBoat;
            if (mappedToBoat == null) {
                logger.warning("No boat found for id " + boatId);
                return Response.status(Status.BAD_REQUEST).entity("No boat found for id " + StringEscapeUtils.escapeHtml(boatId))
                        .type(MediaType.TEXT_PLAIN).build();
            }
            Iterable<Boat> registered = hasRegattaLike.getAllBoats();
            if (!Util.contains(registered, mappedToBoat)) {
                logger.warning("Boat found but not registered for leaderboard " + leaderboardName);
                return Response.status(Status.BAD_REQUEST)
                        .entity("Boat found but not registered for leaderboard " + StringEscapeUtils.escapeHtml(leaderboardName))
                        .type(MediaType.TEXT_PLAIN).build();
            }
            event = new RegattaLogDeviceBoatMappingEventImpl(now, now, author, UUID.randomUUID(), mappedToBoat, device,
                    from, /* to */ null);
        } else {
            // map to a mark
            final Mark mappedToMark = domainFactory.getExistingMarkById(UUIDHelper.tryUuidConversion(markId));
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{name}/device_mappings/end")
    public Response postCheckout(String json, @PathParam("name") String leaderboardName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (!isValidLeaderboard(leaderboard)) {
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
        String boatId = (String) requestObject.get(DeviceMappingConstants.JSON_BOAT_ID_AS_STRING);
        String markId = (String) requestObject.get(DeviceMappingConstants.JSON_MARK_ID_AS_STRING);
        String deviceUuid = (String) requestObject.get(DeviceMappingConstants.JSON_DEVICE_UUID);
        TimePoint closingTimePointInclusive = new MillisecondsTimePoint(toMillis);
        if (toMillis == null || deviceUuid == null || closingTimePointInclusive == null ||
                (competitorId == null && boatId == null && markId == null)) {
            logger.warning("Invalid JSON body in request");
            return Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        final NamedWithID mappedTo;
        if (competitorId != null) {
            final Competitor mappedToCompetitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(competitorId);
            mappedTo = mappedToCompetitor;
            if (mappedToCompetitor == null) {
                logger.warning("No competitor found for id " + competitorId);
                return Response.status(Status.BAD_REQUEST).entity("No competitor found for id " + competitorId)
                        .type(MediaType.TEXT_PLAIN).build();
            }
        } else if (boatId != null) {
            final Boat mappedToBoat = getService().getCompetitorAndBoatStore().getExistingBoatByIdAsString(boatId);
            mappedTo = mappedToBoat;
            if (mappedToBoat == null) {
                logger.warning("No boat found for id " + boatId);
                return Response.status(Status.BAD_REQUEST).entity("No boat found for id " + boatId)
                        .type(MediaType.TEXT_PLAIN).build();
            }
        } else {
            // map to mark
            DomainFactory domainFactory = getService().getDomainObjectFactory().getBaseDomainFactory();
            final Mark mappedToMark = domainFactory.getExistingMarkById(UUIDHelper.tryUuidConversion(markId));
            mappedTo = mappedToMark;
            if (mappedToMark == null) {
                logger.warning("No mark found for id " + markId);
                return Response.status(Status.BAD_REQUEST).entity("No mark found for id " + markId)
                        .type(MediaType.TEXT_PLAIN).build();
            }
            
        }
        final String mappedToTypeString;
        if (competitorId != null) {
            mappedToTypeString = "competitor";
        } else {
            mappedToTypeString = (markId != null) ? "mark" : "boat";
        }
        OpenEndedDeviceMappingFinder finder = new OpenEndedDeviceMappingFinder(isRegattaLike.getRegattaLog(), mappedTo, deviceUuid);
        Serializable deviceMappingEventId = finder.analyze();
        if (deviceMappingEventId == null) {
            logger.warning("No corresponding open " + mappedToTypeString + " to device mapping has been found");
            return Response.status(Status.BAD_REQUEST)
                    .entity("No corresponding open " + mappedToTypeString + " to device mapping has been found")
                    .type(MediaType.TEXT_PLAIN).build();
        }
        RegattaLogCloseOpenEndedDeviceMappingEventImpl event = new RegattaLogCloseOpenEndedDeviceMappingEventImpl(now,
                author, deviceMappingEventId, closingTimePointInclusive);
        isRegattaLike.getRegattaLog().add(event);
        logger.fine("Successfully checked out " + mappedToTypeString + mappedTo.getName());
        return Response.status(Status.OK).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{leaderboardName}/competitors/{competitorId}")
    public Response getCompetitor(@PathParam("leaderboardName") String leaderboardName,
            @PathParam("competitorId") String competitorIdAsString) {
        Response response;
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        Competitor competitor = getService().getCompetitorAndBoatStore().getExistingCompetitorByIdAsString(
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

    private static class LeaderboardAndRaceColumnAndFleetAndResponse {
        private final Leaderboard leaderboard;
        private final RaceColumn raceColumn;
        private final Fleet fleet;
        private final Response response;
        public LeaderboardAndRaceColumnAndFleetAndResponse(Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet, Response response) {
            super();
            this.leaderboard = leaderboard;
            this.raceColumn = raceColumn;
            this.fleet = fleet;
            this.response = response;
        }
        public Leaderboard getLeaderboard() {
            return leaderboard;
        }
        public RaceColumn getRaceColumn() {
            return raceColumn;
        }
        public Fleet getFleet() {
            return fleet;
        }
        public Response getResponse() {
            return response;
        }
    }
    
    private LeaderboardAndRaceColumnAndFleetAndResponse getLeaderboardAndRaceColumnAndFleet(String leaderboardName,
            String raceColumnName, String fleetName) {
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        final Response result;
        final RaceColumn raceColumn;
        final Fleet fleet;
        if (leaderboard == null) {
            result = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
            raceColumn = null;
            fleet = null;
        } else if (raceColumnName == null) {
            result = Response
                    .status(Status.BAD_REQUEST)
                    .entity("Specify a valid "+RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME+" parameter.")
                    .type(MediaType.TEXT_PLAIN).build();
            raceColumn = null;
            fleet = null;
        } else {
            raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn == null) {
                result = Response
                        .status(Status.NOT_FOUND)
                        .entity("Could not find a race column '" + StringEscapeUtils.escapeHtml(raceColumnName) + "' in leaderboard '"
                                + StringEscapeUtils.escapeHtml(leaderboardName) + "'.").type(MediaType.TEXT_PLAIN).build();
                fleet = null;
            } else if (fleetName == null) {
                result = Response
                        .status(Status.BAD_REQUEST)
                        .entity("Specify a valid "+RaceLogServletConstants.PARAMS_RACE_FLEET_NAME+" parameter.")
                        .type(MediaType.TEXT_PLAIN).build();
                fleet = null;
            } else {
                fleet = raceColumn.getFleetByName(fleetName);
                if (fleet == null) {
                    result = Response
                            .status(Status.NOT_FOUND)
                            .entity("Could not find fleet '" + StringEscapeUtils.escapeHtml(fleetName) + "' in leaderboard '" +
                                    StringEscapeUtils.escapeHtml(leaderboardName)
                                    + "' in race column "+raceColumnName+".").type(MediaType.TEXT_PLAIN).build();
                } else {
                    result = Response.ok().build(); // a simple OK as a default response; callers may of course override
                }
            }
        }
        return new LeaderboardAndRaceColumnAndFleetAndResponse(leaderboard, raceColumn, fleet, result);
    }
                
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/settrackingtimes")
    public Response setTrackingTimes(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName,
            @QueryParam("startoftracking") String startOfTrackingAsISO,
            @QueryParam("startoftrackingasmillis") Long startOfTrackingAsMillis,
            @QueryParam("endoftracking") String endOfTrackingAsISO,
            @QueryParam("endoftrackingasmillis") Long endOfTrackingAsMillis) throws InvalidDateException {
        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(DefaultModes.UPDATE, leaderboardName));
        final LeaderboardAndRaceColumnAndFleetAndResponse leaderboardAndRaceColumnAndFleetAndResponse = getLeaderboardAndRaceColumnAndFleet(leaderboardName, raceColumnName, fleetName);
        final Response result;
        if (leaderboardAndRaceColumnAndFleetAndResponse.getFleet() != null) {
            final RaceLog raceLog = leaderboardAndRaceColumnAndFleetAndResponse.getRaceColumn().getRaceLog(leaderboardAndRaceColumnAndFleetAndResponse.getFleet());
            final LogEventAuthorImpl author = new LogEventAuthorImpl(SecurityUtils.getSubject().getPrincipal().toString(), /* priority */ 0);
            JSONObject jsonResult = new JSONObject();
            if (startOfTrackingAsISO != null || startOfTrackingAsMillis != null) {
                final TimePoint startOfTracking = parseTimePoint(startOfTrackingAsISO, startOfTrackingAsMillis, null);
                raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
                jsonResult.put("startoftracking", startOfTracking == null ? null : startOfTracking.asMillis());
            }
            if (endOfTrackingAsISO != null || endOfTrackingAsMillis != null) {
                final TimePoint endOfTracking = parseTimePoint(endOfTrackingAsISO, endOfTrackingAsMillis, null);
                raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
                jsonResult.put("endoftracking", endOfTracking == null ? null : endOfTracking.asMillis());
            }           
            result = Response.ok(jsonResult.toJSONString()).build();
        } else {
            result = leaderboardAndRaceColumnAndFleetAndResponse.getResponse();
        }
        return result;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/starttracking")
    public Response startRaceLogTracking(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName,
            @QueryParam(RaceLogServletConstants.PARAMS_TRACK_WIND) Boolean trackWind,
            @QueryParam(RaceLogServletConstants.PARAMS_CORRECT_WIND_DIRECTION_BY_MAGNETIC_DECLINATION) Boolean correctWindDirectionByMagneticDeclination)
                    throws NotDenotedForRaceLogTrackingException, Exception {
        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(DefaultModes.UPDATE, leaderboardName));
        final LeaderboardAndRaceColumnAndFleetAndResponse leaderboardAndRaceColumnAndFleetAndResponse = getLeaderboardAndRaceColumnAndFleet(leaderboardName, raceColumnName, fleetName);
        final Response result;
        if (leaderboardAndRaceColumnAndFleetAndResponse.getFleet() != null) {
            JSONObject jsonResult = new JSONObject();
            final RaceLogTrackingAdapter adapter = getRaceLogTrackingAdapter();
            jsonResult.put("addeddenotation", adapter.denoteRaceForRaceLogTracking(getService(),
                    leaderboardAndRaceColumnAndFleetAndResponse.getLeaderboard(),
                    leaderboardAndRaceColumnAndFleetAndResponse.getRaceColumn(),
                    leaderboardAndRaceColumnAndFleetAndResponse.getFleet(), /* use default race name */ null));
            final RaceHandle raceHandle = adapter.startTracking(getService(), leaderboardAndRaceColumnAndFleetAndResponse.getLeaderboard(),
                    leaderboardAndRaceColumnAndFleetAndResponse.getRaceColumn(),
                    leaderboardAndRaceColumnAndFleetAndResponse.getFleet(),
                    trackWind == null ? true : trackWind,
                    correctWindDirectionByMagneticDeclination == null ? true : correctWindDirectionByMagneticDeclination);
            jsonResult.put("regatta", raceHandle.getRegatta().getName());
            result = Response.ok(jsonResult.toJSONString()).build();
        } else {
            result = leaderboardAndRaceColumnAndFleetAndResponse.getResponse();
        }
        return result;
    }
    
    /**
     * Sets an inferred course in the race log for the race identified by the {@code leaderboardName},
     * {@code raceColumnName} and {@code fleetName}, using a {@link RaceLogCourseDesignChangedEvent}. The
     * {@code RaceLogRaceTracker} will take the event and update a {@link TrackedRace}'s course accordingly.
     * <p>
     * 
     * <b>Note:</b> An existing course layout for the race identified will be replaced by the new, automatic course
     * layout. New marks will be added. Repeated execution for the same race, while possible, is not recommended as it
     * will lead to multiple marks with the same name.
     * <p>
     * 
     * Several "magical" and heuristic rules are applied to find a course that is better than no course. For this, the
     * method looks for tracks recorded in the {@link TrackedRace}. If no track is found for any of the competitors, no
     * course design is created and no corresponding race log event is added. If one or more tracks are found, the
     * tracks are analyzed.
     * <p>
     * 
     * <b>Start:</b> When a {@link TrackedRace#getStartOfRace() race start time} or at least a
     * {@link TrackedRace#getStartOfTracking() tracking start time} has been set for the race, the tracks are analyzed
     * around that time. A start line is defined as the course's first waypoint such that a majority of the tracks cross
     * that start line, with a "start boat" on the starboard side of the line when viewed in crossing direction, and
     * oriented such that it is perpendicular to the average course of those tracks that cross it.
     * <p>
     * 
     * <b>Finish:</b> When no {@link TrackedRace#getEndOfRace() end of race}, {@link TrackedRace#getFinishedTime()
     * finish time} nor a {@link TrackedRace#getEndOfTracking() tracking end time} has been set, a hypothetical finish
     * line is "guessed" as 12h after the start time, spanning the tracks at that time with the leader just crossing it.
     * <p>
     * 
     * <b>Other course marks:</b> If a wind direction is known from a source other than the race course-based
     * estimation, e.g., a maneuver-based wind estimation, a manual entry, or a sensor measurement, transitions between
     * upwind, downwind, and reaching sections will be separated by adding a {@link ControlPoint} with a corresponding
     * {@link Waypoint} such that the waypoint is passed at the leg type transition. This will allow for better analysis
     * of legs split by their type.
     * <p>
     * 
     * TODO The implementation of this method will improve over time. We'll start with start and finish line for now...
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/autocourse")
    public Response setAutoCourse(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName) throws MalformedURLException, IOException, InterruptedException {
        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(DefaultModes.UPDATE, leaderboardName));
        final LeaderboardAndRaceColumnAndFleetAndResponse leaderboardAndRaceColumnAndFleetAndResponse = getLeaderboardAndRaceColumnAndFleet(leaderboardName, raceColumnName, fleetName);
        final Response result;
        if (leaderboardAndRaceColumnAndFleetAndResponse.getFleet() != null) {
            final TrackedRace trackedRace = leaderboardAndRaceColumnAndFleetAndResponse.getRaceColumn().getTrackedRace(leaderboardAndRaceColumnAndFleetAndResponse.getFleet());
            if (trackedRace == null) {
                result = Response.status(Status.PRECONDITION_FAILED).entity("no tracked race").build();
            } else {
                final RaceLog raceLog = leaderboardAndRaceColumnAndFleetAndResponse.getRaceColumn().getRaceLog(leaderboardAndRaceColumnAndFleetAndResponse.getFleet());
                final Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
                final RegattaLog regattaLog = regatta.getRegattaLog();
                final Course autoCourse = createAutoCourse(trackedRace, regattaLog);
                final TimePoint now = MillisecondsTimePoint.now();
                final RaceLogCourseDesignChangedEvent courseDesignChangedEvent = new RaceLogCourseDesignChangedEventImpl(
                        /* createdAt */ now, /* logical time point */ now, getService().getServerAuthor(), UUID.randomUUID(),
                        raceLog.getCurrentPassId(), autoCourse, CourseDesignerMode.BY_MARKS /* ensures that the waypoint sequence is observed */);
                raceLog.add(courseDesignChangedEvent);
                JSONObject jsonResult = new JSONObject();
                JSONObject jsonRace = new JSONObject();
                jsonResult.put("race", jsonRace);
                jsonRace.put("regatta", regatta.getName());
                final RaceDefinition race = trackedRace.getRace();
                jsonRace.put("race", race.getName());
                jsonResult.put("course", new CourseJsonSerializer(
                        new CourseBaseJsonSerializer(
                                new WaypointJsonSerializer(
                                        new ControlPointJsonSerializer(
                                                new MarkJsonSerializer(), 
                                                new GateJsonSerializer(new MarkJsonSerializer()))))).serialize(autoCourse));
                result = Response.ok(jsonResult.toJSONString()).build();
            }
        } else {
            result = leaderboardAndRaceColumnAndFleetAndResponse.getResponse();
        }
        return result;
    }
    
    private Course createAutoCourse(TrackedRace trackedRace, RegattaLog regattaLog) {
        final List<Waypoint> waypoints = new ArrayList<>();
        final Waypoint startLine = inferStartLine(trackedRace, regattaLog);
        if (startLine != null) {
            waypoints.add(startLine);
        }
        final Waypoint finishLine = inferFinishLine(trackedRace, regattaLog, startLine);
        if (finishLine != null) {
            waypoints.add(finishLine);
        }
        final Course result = new CourseImpl("Auto-Course", waypoints);
        return result;
    }

    /**
     * Guesses a finish line from tracks found in the {@code trackedRace}. If this was possible,
     * mark definitions for start boat and pin end including "pinged" positions are added to the
     * {@code regattaLog}, and the {@link Waypoint} pointing to a new {@link ControlPoint} with
     * passing instruction {@link PassingInstruction#Line Line} will be created and returned. If no
     * such inference seems to make sense, the {@code regattaLog} remains unchanged, and {@code null}
     * is returned.
     */
    private Waypoint inferFinishLine(TrackedRace trackedRace, RegattaLog regattaLog, Waypoint startLine) {
        final TimePoint when = getEndTime(trackedRace);
        return createLineEnclosingTracks(trackedRace, regattaLog, when, /* extrapolate */ true, /* waypoint name */ "Finish");
    }

    private TimePoint getEndTime(TrackedRace trackedRace) {
        final TimePoint when;
        if (trackedRace.getEndOfRace() != null) {
            when = trackedRace.getEndOfRace();
        } else {
            if (trackedRace.getFinishedTime() != null) {
                when = trackedRace.getFinishedTime();
            } else {
                if (trackedRace.getEndOfTracking() != null) {
                    when = trackedRace.getEndOfTracking();
                } else {
                    if (getStartTime(trackedRace) != null) {
                        // assume 12h of sailing
                        when = getStartTime(trackedRace).plus(Duration.ONE_HOUR.times(12));
                    } else {
                        when = null;
                    }
                }
            }
        }
        return when;
    }

    /**
     * Guesses a start line from tracks found in the {@code trackedRace}. If this was possible,
     * mark definitions for start boat and pin end including "pinged" positions are added to the
     * {@code regattaLog}, and the {@link Waypoint} pointing to a new {@link ControlPoint} with
     * passing instruction {@link PassingInstruction#Line Line} will be created and returned. If no
     * such inference seems to make sense, the {@code regattaLog} remains unchanged, and {@code null}
     * is returned.
     */
    private Waypoint inferStartLine(TrackedRace trackedRace, RegattaLog regattaLog) {
        final TimePoint when = getStartTime(trackedRace);
        return createLineEnclosingTracks(trackedRace, regattaLog, when, /* extrapolate */ false, /* waypoint name */ "Start");
    }

    private static final Distance LINE_MARGIN = new MeterDistance(20);
    private Waypoint createLineEnclosingTracks(TrackedRace trackedRace, RegattaLog regattaLog, final TimePoint when, boolean extrapolate, String waypointName) {
        final Waypoint result;
        if (when != null) {
            final Iterable<Pair<Position, SpeedWithBearing>> positionsAndCogsAndSogs = getPositionsAndCogsAndSogs(trackedRace, when, extrapolate); 
            final Bearing averageCourse = getAverageCourse(positionsAndCogsAndSogs);
            final Bearing fromStartBoatToPinEnd = averageCourse.add(new DegreeBearingImpl(-90));
            final Pair<Position, Position> leftmostAndRightmostPositionsAtStart = getPositionsFarthestAheadAndFurthestBack(positionsAndCogsAndSogs,
                    averageCourse.add(new DegreeBearingImpl(90)));
            final Position farthestAhead = getPositionsFarthestAheadAndFurthestBack(positionsAndCogsAndSogs, averageCourse).getA();
            if (leftmostAndRightmostPositionsAtStart.getA() != null && leftmostAndRightmostPositionsAtStart.getB() != null &&
                    farthestAhead != null) {
                final Position startBoatPosition = leftmostAndRightmostPositionsAtStart.getB().translateGreatCircle(
                        fromStartBoatToPinEnd.reverse(), LINE_MARGIN);
                final Position pinEndPosition = leftmostAndRightmostPositionsAtStart.getA().translateGreatCircle(
                        fromStartBoatToPinEnd, LINE_MARGIN);
                final Mark startBoat = getService().getBaseDomainFactory().getOrCreateMark(UUID.randomUUID(), "Auto "+waypointName+" Boat");
                RegattaLogDefineMarkEventImpl defineStartBoatEvent = new RegattaLogDefineMarkEventImpl(MillisecondsTimePoint.now(),
                        getService().getServerAuthor(), when, UUID.randomUUID(), startBoat);
                regattaLog.add(defineStartBoatEvent);
                final Mark pinEnd = getService().getBaseDomainFactory().getOrCreateMark(UUID.randomUUID(), "Auto "+waypointName+" Pin End");
                RegattaLogDefineMarkEventImpl definePinEndEvent = new RegattaLogDefineMarkEventImpl(MillisecondsTimePoint.now(),
                        getService().getServerAuthor(), when, UUID.randomUUID(), pinEnd);
                regattaLog.add(definePinEndEvent);
                getRaceLogTrackingAdapter().pingMark(regattaLog, startBoat, new GPSFixImpl(startBoatPosition, when), getService());
                getRaceLogTrackingAdapter().pingMark(regattaLog, pinEnd, new GPSFixImpl(pinEndPosition, when), getService());
                final ControlPoint startLineControlPoint = getService().getBaseDomainFactory().getOrCreateControlPointWithTwoMarks(
                        UUID.randomUUID(), "Auto "+waypointName+" Line", pinEnd, startBoat);
                result = getService().getBaseDomainFactory().createWaypoint(startLineControlPoint, PassingInstruction.Line);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    private TimePoint getStartTime(TrackedRace trackedRace) {
        final TimePoint when;
        if (trackedRace.getStartOfRace() != null) {
            when = trackedRace.getStartOfRace();
        } else {
            when = trackedRace.getStartOfTracking();
        }
        return when;
    }

    private Pair<Position, Position> getPositionsFarthestAheadAndFurthestBack(Iterable<Pair<Position, SpeedWithBearing>> positionsAndCogsAndSogs,
            Bearing averageCourse) {
        Position base = null;
        Distance maxDistance = null;
        Distance minDistance = null;
        Position positionFarthestAhead = null;
        Position positionFurthestBack = null;
        for (final Pair<Position, SpeedWithBearing> i : positionsAndCogsAndSogs) {
            final Position position = i.getA();
            final Distance distanceFromBaseAlongAverageCourse;
            if (base == null) {
                base = position; // pick any one as the base position for projection
                distanceFromBaseAlongAverageCourse = Distance.NULL;
            } else {
                final Position projected = position.projectToLineThrough(base, averageCourse);
                distanceFromBaseAlongAverageCourse = projected.alongTrackDistance(base, averageCourse);
            }
            if (maxDistance == null || distanceFromBaseAlongAverageCourse.compareTo(maxDistance) > 0) {
                maxDistance = distanceFromBaseAlongAverageCourse;
                positionFarthestAhead = position;
            }
            if (minDistance == null || distanceFromBaseAlongAverageCourse.compareTo(minDistance) < 0) {
                minDistance = distanceFromBaseAlongAverageCourse;
                positionFurthestBack = position;
            }
        }
        return new Pair<>(positionFarthestAhead, positionFurthestBack);
    }

    private Iterable<Pair<Position, SpeedWithBearing>> getPositionsAndCogsAndSogs(TrackedRace trackedRace,
            TimePoint when, boolean extrapolate) {
        final List<Pair<Position, SpeedWithBearing>> result = new ArrayList<>();
        for (final Competitor c : trackedRace.getRace().getCompetitors()) {
            final GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(c);
            if (track != null) {
                final Position position = track.getEstimatedPosition(when, extrapolate);
                final SpeedWithBearing speedWithBearing = track.getEstimatedSpeed(when);
                if (position != null && speedWithBearing != null) {
                    result.add(new Pair<>(position, speedWithBearing));
                }
            }
        }
        return result;
    }

    private Bearing getAverageCourse(Iterable<Pair<Position, SpeedWithBearing>> positionsAndCogsAndSogs) {
        ScalableBearing bearingSum = null;
        int size = 0;
        for (final Pair<Position, SpeedWithBearing> i : positionsAndCogsAndSogs) {
            final ScalableBearing sb = new ScalableBearing(i.getB().getBearing());
            if (bearingSum == null) {
                bearingSum = sb;
            } else {
                bearingSum.add(sb);
            }
            size++;
        }
        return bearingSum == null ? null : bearingSum.divide(size);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{leaderboardName}/stoptracking")
    public Response stopTracking(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName) throws MalformedURLException, IOException, InterruptedException {
        SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(DefaultModes.UPDATE, leaderboardName));
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        final Response result;
        if (leaderboard == null) {
            result = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            JSONArray jsonResultArray = new JSONArray();
            for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                if (raceColumnName == null || raceColumn.getName().equals(raceColumnName)) {
                    for (final Fleet fleet : raceColumn.getFleets()) {
                        if (fleetName == null || fleet.getName().equals(fleetName)) {
                            JSONObject jsonResult = new JSONObject();
                            final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
                            if (trackedRace != null) {
                                final Regatta regatta = trackedRace.getTrackedRegatta().getRegatta();
                                final RaceDefinition race = trackedRace.getRace();
                                getService().stopTracking(regatta, race);
                                jsonResult.put("regatta", regatta.getName());
                                jsonResult.put("race", race.getName());
                                jsonResultArray.add(jsonResult);
                            }
                        }
                    }
                }
            }
            result = Response.ok(jsonResultArray.toJSONString()).build();
        }
        return result;
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

    /**
     * @return the actual or anticipated start order for the race identified by {@code raceColumnName}, {@code fleetName}.
     * Those competitors for which a start mark passing is already known are sorted by those start mark passings. All other
     * boats are ordered by their geometric distance from the start line or the windward distance from the start mark
     * if the start for some reason is defined by a single mark. See {@link #compareDistanceFromStartLine(Competitor, Competitor)}.
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{leaderboardName}/startorder")
    public Response getStartOrder(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_COLUMN_NAME) String raceColumnName,
            @QueryParam(RaceLogServletConstants.PARAMS_RACE_FLEET_NAME) String fleetName) {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        }

        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity("Could not find a race column '" + StringEscapeUtils.escapeHtml(raceColumnName) + "' in leaderboard '"
                            + StringEscapeUtils.escapeHtml(leaderboardName) + "'.").type(MediaType.TEXT_PLAIN).build();
        }
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity("Could not find a fleet '" + StringEscapeUtils.escapeHtml(fleetName) + "' in raceColumn '"
                            + StringEscapeUtils.escapeHtml(raceColumnName) + "'.").type(MediaType.TEXT_PLAIN).build();
        }
        final TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
        if (trackedRace == null) {
            return Response.status(Status.NOT_FOUND)
                    .entity("Could not find a tracked race for raceColumn '" + StringEscapeUtils.escapeHtml(raceColumnName) 
                     + " and fleet '" + StringEscapeUtils.escapeHtml(fleetName) + "'.").type(MediaType.TEXT_PLAIN)
                    .build();
        };
            
        Course course = trackedRace.getRace().getCourse();
        final List<Competitor> competitors = new ArrayList<>();
        final Waypoint firstWaypoint = course.getFirstWaypoint();
        final TimePoint timeToUseForStart;
        if (trackedRace.getStartOfRace() != null) {
            timeToUseForStart = trackedRace.getStartOfRace();
        } else {
            timeToUseForStart = MillisecondsTimePoint.now();
        }
        if (firstWaypoint != null) {
            final List<Position> startWaypointMarkPositions =
                StreamSupport.stream(firstWaypoint.getMarks().spliterator(), /* parallel */ false).
                map(mark->trackedRace.getTrack(mark)).filter(markTrack->markTrack!=null).
                map(markTrack->markTrack.getEstimatedPosition(timeToUseForStart, /* extrapolate */ false)).
                collect(Collectors.toList());
            Util.addAll(trackedRace.getRace().getCompetitors(), competitors);
            competitors.sort((a, b)->{
                final MarkPassing aStartMarkPassing = trackedRace.getMarkPassing(a, firstWaypoint);
                final MarkPassing bStartMarkPassing = trackedRace.getMarkPassing(b, firstWaypoint);
                final int result;
                if (aStartMarkPassing == null) {
                    if (bStartMarkPassing == null) {
                        result = compareDistanceFromStartLine(trackedRace, startWaypointMarkPositions, timeToUseForStart, a, b);
                    } else {
                        result = 1; // b consider less than a because it has a start mark passing and therefore is assumed to have started before a
                    }
                } else if (bStartMarkPassing == null) {
                    result = -1; // a consider less than b because it has a start mark passing and therefore is assumed to have started before b
                } else {
                    // both have a start mark passing; compare time points
                    result = aStartMarkPassing.getTimePoint().compareTo(bStartMarkPassing.getTimePoint());
                }
                return result;
            });
        }
        CompetitorAndBoatJsonSerializer serializer = CompetitorAndBoatJsonSerializer.create();
        JSONArray result = new JSONArray();
        for (final Competitor c : competitors) {
            Boat boat = trackedRace.getBoatOfCompetitor(c);
            JSONObject jsonCompetitor = serializer.serialize(new Pair<>(c, boat));
            result.add(jsonCompetitor);
        }
        String json = result.toJSONString();
        return Response.ok(json).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    private int compareDistanceFromStartLine(TrackedRace trackedRace, Iterable<Position> startWaypointMarkPositions,
            TimePoint timeToUseForStart, Competitor a, Competitor b) {
        final Position aPos = trackedRace.getTrack(a).getEstimatedPosition(timeToUseForStart, /* extrapolate */ true);
        final Position bPos = trackedRace.getTrack(b).getEstimatedPosition(timeToUseForStart, /* extrapolate */ true);
        final Distance aDist = aPos==null?null:getDistanceFromStartWaypoint(aPos, startWaypointMarkPositions);
        final Distance bDist = bPos==null?null:getDistanceFromStartWaypoint(bPos, startWaypointMarkPositions);
        return Comparator.<Distance>nullsLast(Comparator.naturalOrder()).compare(aDist, bDist);
    }

    private Distance getDistanceFromStartWaypoint(Position pos, Iterable<Position> startWaypointMarkPositions) {
        final Distance result;
        if (Util.isEmpty(startWaypointMarkPositions)) {
            result = null;
        } else if (Util.size(startWaypointMarkPositions) == 1) {
            // single mark start; strange, but possible:
            result = startWaypointMarkPositions.iterator().next().getDistance(pos);
        } else {
            final Position first = Util.get(startWaypointMarkPositions, 0);
            final Position second = Util.get(startWaypointMarkPositions, 1);
            result = pos.getDistanceToLine(first, second).abs();
        }
        return result;
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
                if (availableMark.getId().toString().equals(markId)) {
                    mark = availableMark;
                    break;
                }
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
            raceColumnAsJson.put(RaceColumnConstants.FACTOR, leaderboard.getScoringScheme().getScoreFactor(rc));
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
            SecurityUtils.getSubject().checkPermission(Permission.LEADERBOARD.getStringPermissionForObjects(DefaultModes.UPDATE, leaderboard.getName()));
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

    @POST
    @Path("{leaderboardName}/denoteForTracking")
    public Response denoteForTracking(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam("raceColumnName") String raceColumnName, @QueryParam("fleetName") String fleetName,
            @QueryParam("raceName") String raceName) throws NotFoundException, NotDenotableForRaceLogTrackingException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        SecurityUtils.getSubject().checkPermission(Permission.MANAGE_TRACKED_RACES.getStringPermission(DefaultModes.UPDATE));
        if (leaderboard == null) {
            throw new NotFoundException("leaderboard with name " + leaderboardName + " not found");
        }
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            throw new NotFoundException("raceColumn with name " + raceColumnName + " not found");
        }
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            throw new NotFoundException("fleet with name " + fleetName + " not found");
        }

        boolean result = getRaceLogTrackingAdapter().denoteRaceForRaceLogTracking(getService(), leaderboard, raceColumn,
                fleet, raceName);
        return (result ? Response.ok() : Response.notModified()).build();
    }

    @POST
    @Path("{leaderboardName}/enableRaceLogForCompetitorRegistration")
    public Response enableRaceLogForCompetitorRegistration(@PathParam("leaderboardName") String leaderboardName,
            @QueryParam("raceColumnName") String raceColumnName, @QueryParam("fleetName") String fleetName,
            @QueryParam("raceName") String raceName) throws NotFoundException, NotDenotableForRaceLogTrackingException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        SecurityUtils.getSubject().checkPermission(Permission.MANAGE_ALL_COMPETITORS.getStringPermission(DefaultModes.UPDATE));
        if (leaderboard == null) {
            throw new NotFoundException("leaderboard with name " + leaderboardName + " not found");
        }
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            throw new NotFoundException("raceColumn with name " + raceColumnName + " not found");
        }
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            throw new NotFoundException("fleet with name " + fleetName + " not found");
        }

        final AbstractLogEventAuthor raceLogEventAuthorForRaceColumn = new LogEventAuthorImpl(
                AbstractRaceColumn.class.getName(), 0);

        TimePoint now = MillisecondsTimePoint.now();
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog == null) {
            throw new IllegalStateException("Racelog for fleet is null");
        }
        int passId = raceLog.getCurrentPassId();
        raceLog.add(new RaceLogUseCompetitorsFromRaceLogEventImpl(now, raceLogEventAuthorForRaceColumn, now,
                UUID.randomUUID(), passId));
        return (raceColumn.isCompetitorRegistrationInRacelogEnabled(fleet) ? Response.ok() : Response.notModified())
                .build();
    }

}
