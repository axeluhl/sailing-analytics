package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.gateway.jaxrs.UnitSerializationUtil;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.SeriesJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@Path("/v1/regattas")
public class RegattasResource extends AbstractSailingServerResource {

    private Response getBadRegattaErrorResponse(String regattaName) {
        return  Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
    }

    private Response getBadRaceErrorResponse(String regattaName, String raceName) {
        return Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "' in regatta '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getRegattas() {
        RegattaJsonSerializer regattaJsonSerializer = new RegattaJsonSerializer(); 
        
        JSONArray regattasJson = new JSONArray();
        for (Regatta regatta : getService().getAllRegattas()) {
            regattasJson.add(regattaJsonSerializer.serialize(regatta));
        }
        String json = regattasJson.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}")
    public Response getRegatta(@PathParam("regattaname") String regattaName) {
        Response response;
        Regatta regatta = getService().getRegatta(new RegattaName(regattaName));
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            SeriesJsonSerializer seriesJsonSerializer = new SeriesJsonSerializer(new FleetJsonSerializer(new ColorJsonSerializer()));
            JsonSerializer<Regatta> regattaSerializer = new RegattaJsonSerializer(seriesJsonSerializer, null);
            JSONObject serializedRegatta = regattaSerializer.serialize(regatta);
            
            String json = serializedRegatta.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    /**
     * Gets all entries for a regatta.
     * @param regattaName the name of the regatta
     * @return
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/entries")
    public Response getEntries(@PathParam("regattaname") String regattaName) {
        Response response;
        Regatta regatta = getService().getRegatta(new RegattaName(regattaName));
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            NationalityJsonSerializer nationalityJsonSerializer = new NationalityJsonSerializer();
            CompetitorJsonSerializer competitorJsonSerializer = new CompetitorJsonSerializer(new TeamJsonSerializer(
                    new PersonJsonSerializer(nationalityJsonSerializer)), new BoatJsonSerializer(
                    new BoatClassJsonSerializer()));
            JsonSerializer<Regatta> regattaSerializer = new RegattaJsonSerializer(null, competitorJsonSerializer);
            JSONObject serializedRegatta = regattaSerializer.serialize(regatta);

            String json = serializedRegatta.toJSONString();
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
    
    /**
     * Gets all GPS positions of the competitors for a given race.
     * 
     * @param regattaName
     *            the name of the regatta
     * @param tack
     *            whether or not to include the tack in the output for each fix. Determining tack requires an expensive
     *            wind calculation for the competitor's position for each fix's time point. If this value is not
     *            absolutely required, <code>false</code> should be provided here which is also the default.
     * @return
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/competitors/positions")
    public Response getCompetitorPositions(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName,
        @QueryParam("fromtime") String fromtime, @QueryParam("fromtimeasmillis") Long fromtimeasmillis,
        @QueryParam("totime") String totime, @QueryParam("totimeasmillis") Long totimeasmillis, @QueryParam("withtack") Boolean withTack,
        @QueryParam("competitorId") Set<String> competitorIds) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = getBadRaceErrorResponse(regattaName, raceName);
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);

                TimePoint from;
                TimePoint to;
                try {
                    from = parseTimePoint(fromtime, fromtimeasmillis,
                            trackedRace.getStartOfRace() == null ? new MillisecondsTimePoint(0) :
                                /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis()-24*3600*1000));
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'from' time.").type(MediaType.TEXT_PLAIN).build();
                }
                try {
                    to = parseTimePoint(totime, totimeasmillis, MillisecondsTimePoint.now());
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'to' time.").type(MediaType.TEXT_PLAIN).build();
                }

                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", trackedRace.getRace().getName());
                jsonRace.put("regatta", regatta.getName());
                JSONArray jsonCompetitors = new JSONArray();
                for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                    if (competitorIds == null || competitorIds.isEmpty() || competitorIds.contains(competitor.getId().toString())) {
                        JSONObject jsonCompetitor = new JSONObject();
                        jsonCompetitor.put("id", competitor.getId() != null ? competitor.getId().toString() : null);
                        jsonCompetitor.put("name", competitor.getName());
                        jsonCompetitor.put("sailNumber", competitor.getBoat().getSailID());
                        jsonCompetitor.put("color", competitor.getColor() != null ? competitor.getColor().getAsHtml() : null);
                        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                        JSONArray jsonFixes = new JSONArray();
                        track.lockForRead();
                        try {
                            Iterator<GPSFixMoving> fixIter;
                            if (from == null) {
                                fixIter = track.getFixes().iterator();
                            } else {
                                fixIter = track.getFixesIterator(from, /* inclusive */true);
                            }
                            while (fixIter.hasNext()) {
                                GPSFixMoving fix = fixIter.next();
                                if (to != null && fix.getTimePoint() != null && to.compareTo(fix.getTimePoint()) < 0) {
                                    break;
                                }
                                JSONObject jsonFix = new JSONObject();
                                jsonFix.put("timepoint-ms", fix.getTimePoint().asMillis());
                                jsonFix.put("lat-deg", UnitSerializationUtil.latLngDecimalFormatter.format(fix
                                        .getPosition().getLatDeg()));
                                jsonFix.put("lng-deg", UnitSerializationUtil.latLngDecimalFormatter.format(fix
                                        .getPosition().getLngDeg()));
                                jsonFix.put("truebearing-deg", fix.getSpeed().getBearing().getDegrees());
                                jsonFix.put("speed-kts",
                                        UnitSerializationUtil.knotsDecimalFormatter.format(fix.getSpeed().getKnots()));
                                if (withTack != null && withTack) {
                                    String tackName;
                                    try {
                                        final Tack tack = trackedRace.getTack(competitor, fix.getTimePoint());
                                        if (tack != null) {
                                            tackName = tack.name();
                                            jsonFix.put("tack", tackName);
                                        }
                                    } catch (NoWindException e) {
                                        // don't output tack
                                    }
                                }
                                jsonFixes.add(jsonFix);
                            }
                        } finally {
                            track.unlockAfterRead();
                        }
                        jsonCompetitor.put("track", jsonFixes);
                        jsonCompetitors.add(jsonCompetitor);
                    }
                }
                jsonRace.put("competitors", jsonCompetitors);

                String json = jsonRace.toJSONString();
                response = Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
    
    /**
     * Gets all GPS positions of the course marks for a given race.
     * @param regattaName the name of the regatta
     * @return
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/marks/positions")
    public Response getMarkPositions(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName,
            @QueryParam("fromtime") String fromtime, @QueryParam("fromtimeasmillis") Long fromtimeasmillis,
            @QueryParam("totime") String totime, @QueryParam("totimeasmillis") Long totimeasmillis) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = getBadRaceErrorResponse(regattaName, raceName);
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);

                TimePoint from;
                TimePoint to;
                try {
                    from = parseTimePoint(fromtime, fromtimeasmillis,
                            trackedRace.getStartOfRace() == null ? new MillisecondsTimePoint(0) :
                                /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis()-24*3600*1000));
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'from' time.").type(MediaType.TEXT_PLAIN).build();
                }
                try {
                    to = parseTimePoint(totime, totimeasmillis, MillisecondsTimePoint.now());
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'to' time.").type(MediaType.TEXT_PLAIN).build();
                }

                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", trackedRace.getRace().getName());
                jsonRace.put("regatta", regatta.getName());
                JSONArray jsonMarks = new JSONArray();
                Set<Mark> marks = new HashSet<Mark>();
                Course course = trackedRace.getRace().getCourse();
                for (Waypoint waypoint : course.getWaypoints()) {
                    for (Mark mark : waypoint.getMarks()) {
                        marks.add(mark);
                    }
                }
                
                for (Mark mark : marks) {
                    JSONObject jsonMark = new JSONObject();
                    jsonMark.put("name", mark.getName());
                    jsonMark.put("id", mark.getId() != null ? mark.getId().toString() : null);
                    GPSFixTrack<Mark, GPSFix> track = trackedRace.getOrCreateTrack(mark);
                    JSONArray jsonFixes = new JSONArray();
                    track.lockForRead();
                    try {
                        Iterator<GPSFix> fixIter;
                        if (from == null) {
                            fixIter = track.getFixes().iterator();
                        } else {
                            fixIter = track.getFixesIterator(from, /* inclusive */true);
                        }
                        while (fixIter.hasNext()) {
                            GPSFix fix = fixIter.next();
                            if (to != null && fix.getTimePoint() != null && to.compareTo(fix.getTimePoint()) < 0) {
                                break;
                            }
                            JSONObject jsonFix = new JSONObject();
                            jsonFix.put("timepoint-ms", fix.getTimePoint().asMillis());
                            jsonFix.put("lat-deg", UnitSerializationUtil.latLngDecimalFormatter.format(fix.getPosition().getLatDeg()));
                            jsonFix.put("lng-deg", UnitSerializationUtil.latLngDecimalFormatter.format(fix.getPosition().getLngDeg()));
                            jsonFixes.add(jsonFix);
                        }
                    } finally {
                        track.unlockAfterRead();
                    }
                    jsonMark.put("track", jsonFixes);
                    jsonMarks.add(jsonMark);
                }
                jsonRace.put("marks", jsonMarks);

                String json = jsonRace.toJSONString();
                response = Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
 
    /**
     * Gets the course of the race.
     * @param regattaName the name of the regatta
     * @return
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/course")
    public Response getCourse(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = getBadRaceErrorResponse(regattaName, raceName);
            } else {     
                Course course = race.getCourse();
                CourseBaseJsonSerializer serializer = new CourseBaseJsonSerializer(
                        new WaypointJsonSerializer(
                                new ControlPointJsonSerializer(
                                        new MarkJsonSerializer(),
                                        new GateJsonSerializer(new MarkJsonSerializer()))));
                
                JSONObject jsonCourse = serializer.serialize(course);
                String json = jsonCourse.toJSONString();
                response = Response.ok(json, MediaType.APPLICATION_JSON).build();
            }                
        }
        return response;
    }
    
    /**
     * Gets the relevant times of the race.
     * @param regattaName the name of the regatta
     * @return
     */
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/times")
    public Response getTimes(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName) {
        Response response = null;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = getBadRegattaErrorResponse(regattaName);
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = getBadRaceErrorResponse(regattaName, raceName);
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);

                JSONObject jsonRaceTimes = new JSONObject();
                jsonRaceTimes.put("name", trackedRace.getRace().getName());
                jsonRaceTimes.put("regatta", regatta.getName());

                jsonRaceTimes.put("startOfRace-ms", trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asMillis());
                jsonRaceTimes.put("startOfTracking-ms", trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asMillis());
                jsonRaceTimes.put("newestTrackingEvent-ms", trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asMillis());
                jsonRaceTimes.put("endOfTracking-ms", trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asMillis());
                jsonRaceTimes.put("endOfRace-ms", trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asMillis());
                jsonRaceTimes.put("delayToLive-ms", trackedRace.getDelayToLiveInMillis());
                
                JSONArray jsonMarkPassingTimes = new JSONArray();
                List<TimePoint> firstPassingTimepoints = new ArrayList<>();
                Iterable<com.sap.sse.common.Util.Pair<Waypoint, com.sap.sse.common.Util.Pair<TimePoint, TimePoint>>> markPassingsTimes = trackedRace.getMarkPassingsTimes();
                synchronized (markPassingsTimes) {
                    int numberOfWaypoints = Util.size(markPassingsTimes);
                    int wayPointNumber = 1;
                    for (com.sap.sse.common.Util.Pair<Waypoint, com.sap.sse.common.Util.Pair<TimePoint, TimePoint>> markPassingTimes : markPassingsTimes) {
                        JSONObject jsonMarkPassing = new JSONObject();
                        String name = "M" + (wayPointNumber - 1);
                        if (wayPointNumber == numberOfWaypoints) {
                            name = "F";
                        }
                        jsonMarkPassing.put("name", name);
                        com.sap.sse.common.Util.Pair<TimePoint, TimePoint> timesPair = markPassingTimes.getB();
                        TimePoint firstPassingTime = timesPair.getA();
                        TimePoint lastPassingTime = timesPair.getB();
                        jsonMarkPassing.put("firstPassing-ms", firstPassingTime == null ? null : firstPassingTime.asMillis());
                        jsonMarkPassing.put("lastPassing-ms", lastPassingTime == null ? null : lastPassingTime.asMillis());
           
                        firstPassingTimepoints.add(firstPassingTime);
                        
                        jsonMarkPassingTimes.add(jsonMarkPassing);
                        wayPointNumber++;
                    }
                }
                jsonRaceTimes.put("markPassings", jsonMarkPassingTimes);

                JSONArray jsonLegInfos = new JSONArray();
                trackedRace.getRace().getCourse().lockForRead();
                try {
                    Iterable<TrackedLeg> trackedLegs = trackedRace.getTrackedLegs();
                    int legNumber = 1;
                    for (TrackedLeg trackedLeg : trackedLegs) {
                        JSONObject jsonLegInfo = new JSONObject();
                        jsonLegInfo.put("name", "L" + legNumber);

                        try {
                            TimePoint firstPassingTime = firstPassingTimepoints.get(legNumber - 1);
                            if (firstPassingTime != null) {
                                jsonLegInfo.put("type", trackedLeg.getLegType(firstPassingTime));
                                jsonLegInfo.put("bearing-deg", UnitSerializationUtil.bearingDecimalFormatter.format(trackedLeg.getLegBearing(firstPassingTime).getDegrees()));
                            }
                        } catch (NoWindException e) {
                            // do nothing
                        }
                        jsonLegInfos.add(jsonLegInfo);

                        legNumber++;
                    }
                } finally {
                    trackedRace.getRace().getCourse().unlockAfterRead();
                }  
                jsonRaceTimes.put("legs", jsonLegInfos);

                Date now = new Date();
                jsonRaceTimes.put("currentServerTime-ms", now.getTime());
                
                String json = jsonRaceTimes.toJSONString();
                response = Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
    
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/wind")
    public Response getWind(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName,
            @DefaultValue("COMBINED") @QueryParam("windsource") String windSource,
            @QueryParam("fromtime") String fromtime, @QueryParam("fromtimeasmillis") Long fromtimeasmillis,
            @QueryParam("totime") String totime, @QueryParam("totimeasmillis") Long totimeasmillis) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);

                TimePoint from;
                TimePoint to;
                try {
                    from = parseTimePoint(fromtime, fromtimeasmillis,
                            trackedRace.getStartOfRace() == null ? new MillisecondsTimePoint(0) :
                                /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis()-24*3600*1000));
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'from' time.").type(MediaType.TEXT_PLAIN).build();
                }
                try {
                    to = parseTimePoint(totime, totimeasmillis, MillisecondsTimePoint.now());
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'to' time.").type(MediaType.TEXT_PLAIN).build();
                }

                TrackedRaceJsonSerializer serializer = new TrackedRaceJsonSerializer(new DefaultWindTrackJsonSerializer());
                serializer.setWindSource(windSource);
                serializer.setFromTime(from);
                serializer.setToTime(to);

                JSONObject jsonWindTracks = serializer.serialize(trackedRace);
                String json = jsonWindTracks.toJSONString();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/firstlegbearing")
    public Response getFirstLegBearing(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName,
            @QueryParam("time") String time, @QueryParam("timeasmillis") Long timeasmillis) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);
                final TimePoint timePoint;
                try {
                    timePoint = parseTimePoint(time, timeasmillis,
                            trackedRace.getStartOfRace() == null ? new MillisecondsTimePoint(0) :
                                trackedRace.getStartOfRace());
                } catch (InvalidDateException e1) {
                    return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Could not parse the 'from' time.").type(MediaType.TEXT_PLAIN).build();
                }

                BearingJsonSerializer serializer = new BearingJsonSerializer();
                JSONObject jsonBearing = serializer.serialize(trackedRace.getDirectionFromStartToNextMark(timePoint).getFrom());
                String json = jsonBearing.toJSONString();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
 
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/markpassings")
    public Response getMarkPassings(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);
                MarkPassingsJsonSerializer serializer = new MarkPassingsJsonSerializer();
                JSONObject jsonMarkPassings = serializer.serialize(trackedRace);
                String json = jsonMarkPassings.toJSONString();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
 
    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/competitors/legs")
    public Response getCompetitorRanks(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);
                TimePoint timePoint = trackedRace.getTimePointOfNewestEvent() == null ? MillisecondsTimePoint.now()
                        : trackedRace.getTimePointOfNewestEvent();

                JSONObject jsonRaceResults = new JSONObject();
                jsonRaceResults.put("name", trackedRace.getRace().getName());
                jsonRaceResults.put("regatta", regatta.getName());
                jsonRaceResults.put("startOfRace-ms", trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asMillis());

                JSONArray jsonLegs = new JSONArray();
                for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                    JSONObject jsonLeg = new JSONObject();
                    jsonLeg.put("from", leg.getLeg().getFrom().getName());
                    jsonLeg.put("fromWaypointId", leg.getLeg().getFrom().getId());
                    jsonLeg.put("to", leg.getLeg().getTo().getName());
                    jsonLeg.put("toWaypointId", leg.getLeg().getTo().getId());
                    try {
                        jsonLeg.put("upOrDownwindLeg", leg.isUpOrDownwindLeg(timePoint));
                    } catch (NoWindException e) {
                        // no wind, then it's simply no upwind or downwind leg
                        jsonLeg.put("upOrDownwindLeg", "false");
                    }
                    JSONArray jsonCompetitors = new JSONArray();
                    Map<Competitor, Integer> ranks = leg.getRanks(timePoint);
                    for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                        JSONObject jsonCompetitorInLeg = new JSONObject();
                        TrackedLegOfCompetitor trackedLegOfCompetitor = leg.getTrackedLeg(competitor);
                        if (trackedLegOfCompetitor != null) {
                            jsonCompetitorInLeg.put("id", competitor.getId() != null ? competitor.getId().toString() : null);
                            jsonCompetitorInLeg.put("name", competitor.getName());
                            jsonCompetitorInLeg.put("sailNumber", competitor.getBoat().getSailID());
                            jsonCompetitorInLeg.put("color", competitor.getColor() != null ? competitor.getColor().getAsHtml() : null);

                            Speed averageSpeedOverGround = trackedLegOfCompetitor.getAverageSpeedOverGround(timePoint);
                            if(averageSpeedOverGround != null) {
                                jsonCompetitorInLeg.put("averageSOG-kts", UnitSerializationUtil.knotsDecimalFormatter.format(averageSpeedOverGround.getKnots()));
                            }
                            try {
                                Integer numberOfTacks = trackedLegOfCompetitor.getNumberOfTacks(timePoint, /* waitForLatest */ false);
                                Integer numberOfJibes = trackedLegOfCompetitor.getNumberOfJibes(timePoint, /* waitForLatest */ false);
                                Integer numberOfPenaltyCircles = trackedLegOfCompetitor
                                        .getNumberOfPenaltyCircles(timePoint, /* waitForLatest */ false);
                                jsonCompetitorInLeg.put("tacks", numberOfTacks);
                                jsonCompetitorInLeg.put("jibes", numberOfJibes);
                                jsonCompetitorInLeg.put("penaltyCircles", numberOfPenaltyCircles);
                            } catch (NoWindException e) {
                            }

                            TimePoint startTime = trackedLegOfCompetitor.getStartTime();
                            TimePoint finishTime = trackedLegOfCompetitor.getFinishTime();
                            TimePoint startOfRace = trackedRace.getStartOfRace();
                            // between the start of the race and the start of the first leg we have no 'timeSinceGun'
                            // for the competitor
                            if (startOfRace != null && startTime != null) {
                                long timeSinceGun = -1;
                                if (finishTime != null) {
                                    timeSinceGun = finishTime.asMillis() - startOfRace.asMillis();
                                } else {
                                    timeSinceGun = timePoint.asMillis() - startOfRace.asMillis();
                                }
                                if (timeSinceGun > 0) {
                                    jsonCompetitorInLeg.put("timeSinceGun-ms", timeSinceGun);
                                }
                                Distance distanceSinceGun = trackedRace.getTrack(competitor).getDistanceTraveled(startOfRace,
                                        finishTime != null ? finishTime : timePoint);
                                if (distanceSinceGun != null) {
                                    jsonCompetitorInLeg.put("distanceSinceGun-m", distanceSinceGun.getMeters());
                                }
                            }

                            Distance distanceTraveled = trackedLegOfCompetitor.getDistanceTraveled(timePoint);
                            if (distanceTraveled != null) {
                                jsonCompetitorInLeg.put("distanceTraveled-m", UnitSerializationUtil.distanceDecimalFormatter.format(distanceTraveled.getMeters()));
                            }
                            try {
                                Integer rank = ranks.get(competitor);
                                jsonCompetitorInLeg.put("rank", rank);
                            } catch (RuntimeException re) {
                                if (re.getCause() != null && re.getCause() instanceof NoWindException) {
                                    // well, we don't know the wind direction, so we can't compute a ranking
                                } else {
                                    throw re;
                                }
                            }
                            try {
                                jsonCompetitorInLeg.put("gapToLeader-s",
                                        trackedLegOfCompetitor.getGapToLeaderInSeconds(timePoint, WindPositionMode.LEG_MIDDLE));
                            } catch (NoWindException e1) {
                                // well, we don't know the wind direction... then no gap to leader will be shown...
                            }
                            jsonCompetitorInLeg.put("started", trackedLegOfCompetitor.hasStartedLeg(timePoint));
                            jsonCompetitorInLeg.put("finished", trackedLegOfCompetitor.hasFinishedLeg(timePoint));
                            jsonCompetitors.add(jsonCompetitorInLeg);
                        }
                    }
                    jsonLeg.put("competitors", jsonCompetitors);
                    jsonLegs.add(jsonLeg);
                }
                jsonRaceResults.put("legs", jsonLegs);

                String json = jsonRaceResults.toJSONString();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @Path("{regattaname}/races/{racename}/competitors/live")
    public Response getCompetitorLiveRanks(@PathParam("regattaname") String regattaName, @PathParam("racename") String raceName,
            @DefaultValue("-1") @QueryParam("topN") Integer topN) {
        Response response;
        Regatta regatta = findRegattaByName(regattaName);
        if (regatta == null) {
            response = Response.status(Status.NOT_FOUND).entity("Could not find a regatta with name '" + regattaName + "'.").type(MediaType.TEXT_PLAIN).build();
        } else {
            RaceDefinition race = findRaceByName(regatta, raceName);
            if (race == null) {
                response = Response.status(Status.NOT_FOUND).entity("Could not find a race with name '" + raceName + "'.").type(MediaType.TEXT_PLAIN).build();
            } else {     
                TrackedRace trackedRace = findTrackedRace(regattaName, raceName);
                Course course = trackedRace.getRace().getCourse();
                Waypoint lastWaypoint = course.getLastWaypoint();

                TimePoint timePoint = trackedRace.getTimePointOfNewestEvent() == null ? MillisecondsTimePoint.now()
                        : trackedRace.getTimePointOfNewestEvent();
                // if(trackedRace.isLive(timePoint)) {

                JSONObject jsonLiveData = new JSONObject();
                jsonLiveData.put("name", trackedRace.getRace().getName());
                jsonLiveData.put("regatta", regatta.getName());

                if(trackedRace.getStartOfRace() != null) {
                    TimePoint startOfRace = trackedRace.getStartOfRace();
                    TimePoint now =  MillisecondsTimePoint.now();
                    jsonLiveData.put("startTime", startOfRace.asMillis());
                    jsonLiveData.put("liveTime", now.asMillis());
                    if(startOfRace.before(now)) {
                        jsonLiveData.put("timeSinceStart-s", (now.asMillis() - startOfRace.asMillis()) / 1000.0);
                    } else {
                        jsonLiveData.put("timeToStart-s", (startOfRace.asMillis() - now.asMillis()) / 1000.0);
                    }
                }
                
                JSONArray jsonCompetitors = new JSONArray();
                try {
                    List<Competitor> competitorsFromBestToWorst = trackedRace.getCompetitorsFromBestToWorst(timePoint);
                    Integer rank = 1;
                    for (Competitor competitor : competitorsFromBestToWorst) {
                        JSONObject jsonCompetitorInLeg = new JSONObject();
                        
                        if(topN != null && topN > 0 && rank > topN) {
                            break;
                        }
                        jsonCompetitorInLeg.put("id", competitor.getId() != null ? competitor.getId().toString() : null);
                        jsonCompetitorInLeg.put("name", competitor.getName());
                        jsonCompetitorInLeg.put("sailNumber", competitor.getBoat().getSailID());
                        jsonCompetitorInLeg.put("color", competitor.getColor() != null ? competitor.getColor().getAsHtml() : null);
                        jsonCompetitorInLeg.put("rank", rank++);

                        TrackedLegOfCompetitor currentLegOfCompetitor = trackedRace.getCurrentLeg(competitor, timePoint);
                        if (currentLegOfCompetitor != null) {
                            int indexOfWaypoint = course.getIndexOfWaypoint(currentLegOfCompetitor.getLeg().getFrom());
                            jsonCompetitorInLeg.put("leg", indexOfWaypoint+1);

                            Speed speedOverGround = currentLegOfCompetitor.getSpeedOverGround(timePoint);
                            if(speedOverGround != null) {
                                jsonCompetitorInLeg.put("speedOverGround-kts", roundDouble(speedOverGround.getKnots(), 2));
                            }
    
                            Distance distanceTraveled = currentLegOfCompetitor.getDistanceTraveled(timePoint);
                            if (distanceTraveled != null) {
                                jsonCompetitorInLeg.put("distanceTraveled-m", roundDouble(distanceTraveled.getMeters(), 2));
                            }
                            
                            Double gapToLeaderInSeconds = currentLegOfCompetitor.getGapToLeaderInSeconds(timePoint, WindPositionMode.LEG_MIDDLE);
                            if(gapToLeaderInSeconds != null) {
                                jsonCompetitorInLeg.put("gapToLeader-s", roundDouble(gapToLeaderInSeconds, 2));
                            }
                            
                            Distance windwardDistanceToOverallLeader = currentLegOfCompetitor.getWindwardDistanceToOverallLeader(timePoint, WindPositionMode.LEG_MIDDLE);
                            if(windwardDistanceToOverallLeader != null) {
                                jsonCompetitorInLeg.put("gapToLeader-m", roundDouble(windwardDistanceToOverallLeader.getMeters(), 2));
                            }
                            jsonCompetitorInLeg.put("finished", false);
                        } else {
                            // we need to distinguish between competitors which did not start and competitors which already finished
                            if(trackedRace.getMarkPassing(competitor, lastWaypoint) != null) {
                                jsonCompetitorInLeg.put("finished", true);
                            } else {
                                jsonCompetitorInLeg.put("finished", false);
                            }
                        }
                        jsonCompetitors.add(jsonCompetitorInLeg);
                    }
                } catch (NoWindException e1) {
                    // well, we don't know the wind direction... then no gap to leader will be shown...
                }
                jsonLiveData.put("competitors", jsonCompetitors);
                
                String json = jsonLiveData.toJSONString();
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        return response;
    }
}
 