package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogCourseDesignChangedEventImpl;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogDefineMarkEventImpl;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorWithBoat;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.RGBColor;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

@Path("/v1/mark")
public class MarkResource extends AbstractSailingServerResource {
    private static final String TIME_MILLIS = "timeMillis";
    private static final String LAT_DEG = "latDeg";
    private static final String LON_DEG = "lonDeg";
    private static final String FLEET_NAME = "fleetName";
    private static final String RACE_COLUMN_NAME = "raceColumnName";
    private static final String LEADERBOARD_NAME = "leaderboardName";
    private static final String MARK_ID = "markId";
    private static final String REGATTA_NAME = "regattaName";
    private static final String MARK_NAME = "markName";
    private static final String MARK_SHORT_NAME = "shortName";
    private static final String MARK_COLOR = "color";
    private static final String MARK_PATTERN = "pattern";
    private static final String MARK_SHAPE = "shape";
    private static final String MARK_TYPE = "type";
    private static final String CONTROL_POINT_NAME = "controlPointName";
    private static final String CONTROL_POINT_SHORT_NAME = "controlPointShortName";
    private static final String ORIGINATING_MARK_TEMPLATE_ID = "originatingMarkTemplateId";
    private static final String ORIGINATING_MARK_PROPERTIES_ID = "originatingMarkPropertiesId";
    private static final String ORIGINATING_COURSE_TEMPLATE_ID = "originatingCourseTemplateId";

    private static final Logger LOG = Logger.getLogger(MarkResource.class.getName());
    @POST
    @Path("/addMarkToRegatta")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addMarkToRegatta(String json) throws Exception {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        String markName = (String) requestObject.get(MARK_NAME);
        String markShortName = (String) requestObject.get(MARK_SHORT_NAME);
        final String originatingMarkTemplateIdAsString = (String) requestObject.get(ORIGINATING_MARK_TEMPLATE_ID);
        final String originatingMarkPropertiesIdAsString = (String) requestObject.get(ORIGINATING_MARK_PROPERTIES_ID);
        final String markColorStr = (String) requestObject.get(MARK_COLOR);
        final String markPattern = (String) requestObject.get(MARK_PATTERN);
        final String markShape = (String) requestObject.get(MARK_SHAPE);
        final String markTypeStr = (String) requestObject.get(MARK_TYPE);
        MarkType markType = null;
        try {
            markType = MarkType.valueOf(markTypeStr);
        } catch (IllegalArgumentException | NullPointerException e) {
            // ignore since mark type value is optional
            LOG.warning("Invalid mark type '" + markTypeStr + "' received via REST endpoint was ignored.");
        }
        Color markColor = null;
        if (markColorStr != null && !markColorStr.isEmpty()) {
            try {
            markColor = new RGBColor(markColorStr);
            } catch (IllegalArgumentException e) {
                // ignore since mark type value is optional
                LOG.warning("Invalid color '" + markColorStr + "' received via REST endpoint was ignored.");
            }
        }
        UUID markId = UUID.randomUUID();
        UUID originatingMarkTemplateId = originatingMarkTemplateIdAsString != null
                ? UUID.fromString(originatingMarkTemplateIdAsString)
                : null;
        UUID originatingMarkPropertiesId = originatingMarkPropertiesIdAsString != null
                ? UUID.fromString(originatingMarkPropertiesIdAsString)
                : null;
        final Mark mark = getService().getBaseDomainFactory().getOrCreateMark(markId, markName, markShortName, markType,
                markColor, markShape, markPattern, originatingMarkTemplateId, originatingMarkPropertiesId);
        String regattaName = (String) requestObject.get(REGATTA_NAME);
        Regatta regatta = getService().getRegattaByName(regattaName);
        Response response;
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND)
                    .entity("Could not find a regatta with name '" + StringEscapeUtils.escapeHtml(regattaName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            getSecurityService().checkCurrentUserUpdatePermission(regatta);
            RegattaLog regattaLog = getRegattaLogInternal(regattaName);
            RegattaLogDefineMarkEventImpl event = new RegattaLogDefineMarkEventImpl(MillisecondsTimePoint.now(),
                    getService().getServerAuthor(), MillisecondsTimePoint.now(), UUID.randomUUID(), mark);
            regattaLog.add(event);
            JSONObject answer = new JSONObject();
            answer.put(MARK_ID, markId.toString());
            response = Response.ok(answer.toJSONString())
                    .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }

    @POST
    @Path("/addMarkFix")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addMarkFix(String json)
            throws DoesNotHaveRegattaLogException, ParseException, JsonDeserializationException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        String leaderboardName = (String) requestObject.get(LEADERBOARD_NAME);
        String raceColumnName = (String) requestObject.get(RACE_COLUMN_NAME);
        String fleetName = (String) requestObject.get(FLEET_NAME);
        String markId = (String) requestObject.get(MARK_ID);
        String lonDeg = (String) requestObject.get(LON_DEG);
        String latDeg = (String) requestObject.get(LAT_DEG);
        String timeMillis = (String) requestObject.get(TIME_MILLIS);
        final RaceLogTrackingAdapter raceLogTrackingAdapter = getRaceLogTrackingAdapter();
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
            // check leaderboard update permission
            SecurityUtils.getSubject()
                    .checkPermission(leaderboard.getIdentifier().getStringPermission(DefaultActions.UPDATE));
            // if leaderboard is regatta leaderboard, check regatta update permission
            if (leaderboard instanceof RegattaLeaderboard) {
                final Regatta regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
                SecurityUtils.getSubject()
                        .checkPermission(regatta.getIdentifier().getStringPermission(DefaultActions.UPDATE));
            }
            getSecurityService().checkCurrentUserUpdatePermission(leaderboard);
            final RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
            if (raceColumn != null) {
                final RegattaLog regattaLog = raceColumn.getRegattaLog();
                final Fleet fleet = raceColumn.getFleetByName(fleetName);
                if (fleet != null) {
                    for (final Mark mark : raceColumn.getAvailableMarks(fleet)) {
                        if (mark.getId().toString().equals(markId)) {
                            GPSFix gpsFix = GPSFixImpl.create(Double.parseDouble(lonDeg), Double.parseDouble(latDeg),
                                    Long.parseLong(timeMillis));
                            raceLogTrackingAdapter.pingMark(regattaLog, mark, gpsFix, getService());
                            return Response.ok().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8")
                                    .build();
                        }
                    }
                }
            }
        }
        return Response.notModified().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @POST
    @Path("/addCourseDefinitionToRaceLog")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addCourseDefinitionToRaceLog(String json)
            throws DoesNotHaveRegattaLogException, NotFoundException, ParseException, JsonDeserializationException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        String leaderboardName = (String) requestObject.get(LEADERBOARD_NAME);
        String originatingCourseTemplateIdAsString = (String) requestObject.get(ORIGINATING_COURSE_TEMPLATE_ID);
        UUID originatingCourseTemplateId = originatingCourseTemplateIdAsString != null ? UUID.fromString(originatingCourseTemplateIdAsString) : null;
        SecurityUtils.getSubject().checkPermission(
                SecuredDomainType.LEADERBOARD.getStringPermissionForTypeRelativeIdentifier(DefaultActions.UPDATE,
                        Leaderboard.getTypeRelativeObjectIdentifier(leaderboardName)));
        String raceColumnName = (String) requestObject.get(RACE_COLUMN_NAME);
        String fleetName = (String) requestObject.get(FLEET_NAME);
        RaceLog raceLog = getRaceLog(leaderboardName, raceColumnName, fleetName);
        String courseName = "Course of " + raceColumnName;
        if (!LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleetName)) {
            courseName += " - " + fleetName;
        }
        CourseBase lastPublishedCourse = new LastPublishedCourseDesignFinder(raceLog,
                /* onlyCoursesWithValidWaypointList */ false).analyze();
        if (lastPublishedCourse == null) {
            lastPublishedCourse = new CourseDataImpl(courseName);
        }
        JSONArray controlPointsRaw = (JSONArray) requestObject.get("controlPoints");
        List<Pair<ControlPoint, PassingInstruction>> controlPoints = new ArrayList<>();
        for (Object controlPointUnsafe : controlPointsRaw) {
            JSONObject controlPointRaw = Helpers.toJSONObjectSafe(controlPointUnsafe);
            final String passingInstructionString = (String) controlPointRaw.get("passingInstruction");
            PassingInstruction passing = PassingInstruction.valueOfIgnoringCase(passingInstructionString);
            JSONArray marksRaw = (JSONArray) controlPointRaw.get("marks");
            if (marksRaw.size() == 1) {
                String markName = (String) marksRaw.get(0);
                Mark mark = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markName);
                if (mark == null) {
                    throw new IllegalStateException("Could not resolve mark " + markName);
                }
                controlPoints.add(new Pair<>(mark, passing));
            } else {
                String markNameA = (String) marksRaw.get(0);
                String markNameB = (String) marksRaw.get(1);
                Mark markA = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markNameA);
                Mark markB = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markNameB);
                if (markA == null) {
                    throw new IllegalStateException("Could not resolve mark " + markA);
                }
                if (markB == null) {
                    throw new IllegalStateException("Could not resolve mark " + markB);
                }
                String controlPointName = (String) controlPointRaw.get(CONTROL_POINT_NAME);
                if (controlPointName == null || controlPointName.isEmpty()) {
                    controlPointName = markA.getName() + "-" + markB.getName();
                }

                String shortName = (String) controlPointRaw.get(CONTROL_POINT_SHORT_NAME);
                controlPoints.add(new Pair<>(new ControlPointWithTwoMarksImpl(markA, markB, controlPointName,
                        shortName == null ? controlPointName : shortName), passing));
            }
        }
        Course course = new CourseImpl(courseName, lastPublishedCourse.getWaypoints(), originatingCourseTemplateId);
        try {
            course.update(controlPoints, lastPublishedCourse.getAssociatedRoles(), getService().getBaseDomainFactory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        RaceLogEvent event = new RaceLogCourseDesignChangedEventImpl(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), raceLog.getCurrentPassId(), course, CourseDesignerMode.ADMIN_CONSOLE);
        raceLog.add(event);
        CourseBase updatedPublishedCourse = new LastPublishedCourseDesignFinder(raceLog,
                /* onlyCoursesWithValidWaypointList */ false).analyze();
        JSONObject jsonResult = new JSONObject();
        jsonResult.put("course",
                new CourseJsonSerializer(new CourseBaseJsonSerializer(
                        new WaypointJsonSerializer(new ControlPointJsonSerializer(new MarkJsonSerializer(),
                                new GateJsonSerializer(new MarkJsonSerializer()))))).serialize(updatedPublishedCourse));
        return Response.ok(jsonResult.toJSONString())
                .header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @POST
    @Path("/addCompetitorToRace")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addCompetitorToRace(String json) throws DoesNotHaveRegattaLogException, NotFoundException,
            ParseException, JsonDeserializationException, CompetitorRegistrationOnRaceLogDisabledException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);

        String leaderboardName = (String) requestObject.get(LEADERBOARD_NAME);
        Leaderboard leaderBoard = getService().getLeaderboardByName(leaderboardName);
        Response response;
        if (leaderBoard == null) {
            response = Response.status(Status.NOT_FOUND).entity(
                    "Could not find a leaderboard with name '" + StringEscapeUtils.escapeHtml(leaderboardName) + "'.")
                    .type(MediaType.TEXT_PLAIN).build();
        } else {
            SecurityUtils.getSubject().checkPermission(
                    SecuredDomainType.LEADERBOARD.getStringPermissionForTypeRelativeIdentifier(DefaultActions.UPDATE,
                            Leaderboard.getTypeRelativeObjectIdentifier(leaderboardName)));
            String raceColumnName = (String) requestObject.get(RACE_COLUMN_NAME);
            String fleetName = (String) requestObject.get(FLEET_NAME);

            JSONArray competitorsRaw = (JSONArray) requestObject.get("competitors");
            Map<Competitor, Boat> competitorsToRegister = new HashMap<>();
            for (int i = 0; i < competitorsRaw.size(); i++) {
                JSONObject competitorRaw = (JSONObject) competitorsRaw.get(i);
                String competitorId = (String) competitorRaw.get("competitorId");
                String boatId = (String) competitorRaw.get("boatId");
                Competitor competitor = getService().getCompetitorAndBoatStore()
                        .getExistingCompetitorByIdAsString(competitorId);
                Boat boat = getService().getCompetitorAndBoatStore().getExistingBoatByIdAsString(boatId);
                competitorsToRegister.put(competitor, boat);
            }
            RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
            Fleet fleet = getFleetByName(raceColumn, fleetName);
            Map<Competitor, Boat> competitorsRegisteredInRaceLog = new HashMap<>();
            for (final Entry<Competitor, Boat> e : raceColumn.getCompetitorsRegisteredInRacelog(fleet).entrySet()) {
                competitorsRegisteredInRaceLog.put((CompetitorWithBoat) e.getKey(), e.getValue());
            }
            final Iterable<Competitor> competitorSetToRemove = filterCompetitorDuplicates(competitorsToRegister,
                    competitorsRegisteredInRaceLog);
            raceColumn.deregisterCompetitors(competitorSetToRemove, fleet);
            // we assume that the competitors id of type Competitor here, so we need to find the corresponding boat
            for (Entry<Competitor, Boat> competitorToRegister : competitorsToRegister.entrySet()) {
                raceColumn.registerCompetitor(competitorToRegister.getKey(), competitorToRegister.getValue(), fleet);
            }
            response = Response.ok().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
        }
        return response;
    }

    private Iterable<Competitor> filterCompetitorDuplicates(Map<Competitor, Boat> competitorToBoatMappingsToRegister,
            Map<Competitor, Boat> competitorToBoatMappingsRegistered) {
        final Set<Competitor> competitorsToUnregister = new HashSet<>();
        Util.addAll(competitorToBoatMappingsRegistered.keySet(), competitorsToUnregister);
        for (final Entry<Competitor, Boat> e : competitorToBoatMappingsRegistered.entrySet()) {
            Competitor competitor = e.getKey();
            if (competitorToBoatMappingsToRegister.get(competitor) == e.getValue()) {
                // User wants to map competitor to boat, and that mapping already exists; neither add nor remove this
                // registration but leave as is:
                competitorToBoatMappingsToRegister.remove(competitor);
                competitorsToUnregister.remove(competitor);
            }
        }
        return competitorsToUnregister;
    }

    private RaceLog getRaceLog(String leaderboardName, String raceColumnName, String fleetName)
            throws NotFoundException {
        RaceColumn raceColumn = getRaceColumn(leaderboardName, raceColumnName);
        Fleet fleet = getFleetByName(raceColumn, fleetName);
        return raceColumn.getRaceLog(fleet);
    }

    private Fleet getFleetByName(RaceColumn raceColumn, String fleetName) throws NotFoundException {
        Fleet fleet = raceColumn.getFleetByName(fleetName);
        if (fleet == null) {
            throw new NotFoundException("fleet with name " + fleetName + " not found");
        }
        return fleet;
    }

    private RaceColumn getRaceColumn(String leaderboardName, String raceColumnName) throws NotFoundException {
        Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            throw new NotFoundException("leaderboard with name " + leaderboardName + " not found");
        }
        RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
        if (raceColumn == null) {
            throw new NotFoundException("raceColumn with name " + raceColumnName + " not found");
        }
        return raceColumn;
    }

    private RegattaLog getRegattaLogInternal(String leaderboardName) throws DoesNotHaveRegattaLogException {
        Leaderboard l = getService().getLeaderboardByName(leaderboardName);
        if (!(l instanceof HasRegattaLike)) {
            throw new DoesNotHaveRegattaLogException();
        }
        return ((HasRegattaLike) l).getRegattaLike().getRegattaLog();
    }

}
