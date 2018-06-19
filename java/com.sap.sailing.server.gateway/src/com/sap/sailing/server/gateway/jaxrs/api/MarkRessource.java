package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapterFactory;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.operationaltransformation.UpdateSeries;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.common.impl.RGBColor;

@Path("/v1/mark")
public class MarkRessource extends AbstractSailingServerResource {
    @POST
    @Path("/addMarkToRegatta")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response addMarkToRegatta(String json) throws Exception {
        // SecurityUtils.getSubject().checkPermission(Permission.MANAGE_COURSE_LAYOUT.getStringPermission(Mode.CREATE));

        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);

        String markName = (String) requestObject.get("markName");

        UUID markId = UUID.randomUUID();
        MarkImpl mark = new MarkImpl(markId, markName);
        String regattaName = (String) requestObject.get("regattaName");
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
        // SecurityUtils.getSubject().checkPermission(Permission.MANAGE_MARK_POSITIONS.getStringPermission(Mode.CREATE));

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
        // SecurityUtils.getSubject().checkPermission(Permission.MANAGE_COURSE_LAYOUT.getStringPermission(Mode.CREATE));

        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);

        String leaderboardName = (String) requestObject.get("leaderboardName");
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
                controlPoints.add(new Pair<>(mark, passing));
            } else {
                String markNameA = (String) marksRaw.get(0);
                String markNameB = (String) marksRaw.get(1);
                Mark markA = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markNameA);
                Mark markB = getService().getBaseDomainFactory().getExistingMarkByIdAsString(markNameB);
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
        return Response.ok().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
    }

    @POST
    @Path("/updateOrCreateSeries")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json;charset=UTF-8")
    public Response updateOrCreateSeries(String json) throws ParseException, JsonDeserializationException {
        Object requestBody = JSONValue.parseWithException(json);
        JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
        String regattaName = (String) requestObject.get("regattaName");
        
        Regatta regatta = getService().getRegattaByName(regattaName);
        if (regatta != null) {
//            SecurityUtils.getSubject()
//            .checkPermission(Permission.REGATTA.getStringPermissionForObjects(Mode.UPDATE, regatta.getName()));
        }
        
        
        String seriesName = (String) requestObject.get("seriesName");
        String seriesNameNew = (String) requestObject.get("seriesNameNew");
        boolean isMedal = (boolean) requestObject.get("isMedal");
        boolean isFleetsCanRunInParallel = (boolean) requestObject.get("isFleetsCanRunInParallel");
        boolean startsWithZeroScore = (boolean) requestObject.get("startsWithZeroScore");
        boolean firstColumnIsNonDiscardableCarryForward = (boolean) requestObject.get("firstColumnIsNonDiscardableCarryForward");
        boolean hasSplitFleetContiguousScoring = (boolean) requestObject.get("hasSplitFleetContiguousScoring");
        
        Integer maximumNumberOfDiscards = null;
        if (requestObject.containsKey("maximumNumberOfDiscards")) {
            maximumNumberOfDiscards = (int) (long) requestObject.get("maximumNumberOfDiscards");
        }
        
        int[] resultDiscardingThresholds = null;
        if(requestObject.containsKey("resultDiscardingThresholds")) {
            JSONArray resultDiscardingThresholdsRaw = (JSONArray) requestObject.get("resultDiscardingThresholds");
            resultDiscardingThresholds = new int[resultDiscardingThresholdsRaw.size()];
            for (int i = 0; i < resultDiscardingThresholdsRaw.size(); i++) {
                resultDiscardingThresholds[i] = (int) (long) resultDiscardingThresholdsRaw.get(i);
            }
        }
        
        JSONArray fleetsRaw = (JSONArray) requestObject.get("fleets");
        List<FleetDTO> fleets = new ArrayList<>();
        for(Object fleetRaw:fleetsRaw) {
            JSONObject fleet = Helpers.toJSONObjectSafe(fleetRaw);
            String fleetName = (String) fleet.get("fleetName");
            int orderNo = (int) (long) fleet.get("orderNo");
            String htmlColor = (String) fleet.get("htmlColor");
            fleets.add(new FleetDTO(fleetName, orderNo, new RGBColor(htmlColor)));
        }
        getService().apply(new UpdateSeries(regatta.getRegattaIdentifier(), seriesName, seriesNameNew, isMedal,
                isFleetsCanRunInParallel, resultDiscardingThresholds, startsWithZeroScore,
                firstColumnIsNonDiscardableCarryForward, hasSplitFleetContiguousScoring, maximumNumberOfDiscards,
                fleets));

        return Response.ok().header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8").build();
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
