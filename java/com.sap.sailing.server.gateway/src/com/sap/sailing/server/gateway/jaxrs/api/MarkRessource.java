package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
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
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;

@Path("/v1/mark")
public class MarkRessource extends AbstractSailingServerResource {
    @POST
    @Path("/addMarkToRegatta")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addMarkToRegatta(String json) throws Exception {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        String markName = (String) requestObject.get("markName");
        UUID markId = UUID.randomUUID();
        Mark mark = getService().getBaseDomainFactory().getOrCreateMark(markId, markName);
        String regattaName = (String) requestObject.get("regattaName");
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.REGATTA.getStringPermissionForObjects(DefaultActions.UPDATE, regattaName));
        RegattaLog regattaLog = getRegattaLogInternal(regattaName);
        RegattaLogDefineMarkEventImpl event = new RegattaLogDefineMarkEventImpl(MillisecondsTimePoint.now(),
                getService().getServerAuthor(), MillisecondsTimePoint.now(), UUID.randomUUID(), mark);
        regattaLog.add(event);
        JSONObject answer = new JSONObject();
        answer.put("markId", markId.toString());
        return Response.ok(answer.toJSONString()).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8")
                .build();
    }

    @POST
    @Path("/addMarkFix")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addMarkFix(String json)
            throws DoesNotHaveRegattaLogException, ParseException, JsonDeserializationException {
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.MANAGE_MARK_POSITIONS.getStringPermission(DefaultActions.CREATE));

        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);

        String leaderboardName = (String) requestObject.get("leaderboardName");
        String raceColumnName = (String) requestObject.get("raceColumnName");
        String fleetName = (String) requestObject.get("fleetName");
        String markId = (String) requestObject.get("markId");
        String lonDeg = (String) requestObject.get("lonDeg");
        String latDeg = (String) requestObject.get("latDeg");
        String timeMillis = (String) requestObject.get("timeMillis");

        RaceLogTrackingAdapter raceLogTrackingAdapter = RaceLogTrackingAdapterFactory.INSTANCE
                .getAdapter(getService().getBaseDomainFactory());
        final Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
        if (leaderboard != null) {
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
        String leaderboardName = (String) requestObject.get("leaderboardName");
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        String raceColumnName = (String) requestObject.get("raceColumnName");
        String fleetName = (String) requestObject.get("fleetName");
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
                controlPoints.add(new Pair<>(
                        new ControlPointWithTwoMarksImpl(markA, markB, markA.getName() + "-" + markB.getName()),
                        passing));
            }
        }
        Course course = new CourseImpl(courseName, lastPublishedCourse.getWaypoints());
        try {
            course.update(controlPoints, getService().getBaseDomainFactory());
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

        String leaderboardName = (String) requestObject.get("leaderboardName");
        SecurityUtils.getSubject().checkPermission(SecuredDomainType.LEADERBOARD.getStringPermissionForObjects(DefaultActions.UPDATE, leaderboardName));
        String raceColumnName = (String) requestObject.get("raceColumnName");
        String fleetName = (String) requestObject.get("fleetName");

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
        return Response.ok().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
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
