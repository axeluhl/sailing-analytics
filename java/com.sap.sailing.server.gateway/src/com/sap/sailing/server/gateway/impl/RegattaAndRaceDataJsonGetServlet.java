package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindPositionMode;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaAndRaceDataJsonGetServlet extends AbstractJsonHttpServlet {
    private static final Logger logger = Logger.getLogger(RegattaAndRaceDataJsonGetServlet.class.getName());
    
    private static final long serialVersionUID = 1333207389294903999L;

    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    private static final String ACTION_NAME_SHOW_RACE = "showrace";
    
    private static final String ACTION_NAME_SHOW_WAYPOINTS = "showwaypoints";
    
    private static final String ACTION_NAME_SHOW_BOAT_POSITIONS = "showboatpositions";
    
    private static final String PARAM_NAME_SINCE_UPDATE = "sinceupdate";
    
    private static final String PARAM_NAME_SINCE = "since";
    
    private static final String PARAM_NAME_SINCE_MILLIS = "sinceasmillis";
    
    private static final String PARAM_NAME_TO = "to";
    
    private static final String PARAM_NAME_TO_MILLIS = "toasmillis";
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_LIST_EVENTS.equals(action)) {
                    listEvents(resp);
                } else if (ACTION_NAME_SHOW_RACE.equals(action)) {
                    showRace(req, resp);
                } else if (ACTION_NAME_SHOW_WAYPOINTS.equals(action)) {
                    showWaypoints(req, resp);
                } else if (ACTION_NAME_SHOW_BOAT_POSITIONS.equals(action)) {
                    showBoatPositions(req, resp);
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown action \""+action+"\"");
                }
            } else {
                resp.getWriter().println("No action specified!");
            }
        } catch (Exception e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }

    private void showBoatPositions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TrackedRace trackedRace = getTrackedRace(req);
        if (trackedRace == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
        } else {
            try {
                TimePoint sinceTimePoint = readTimePointParam(req, PARAM_NAME_SINCE, PARAM_NAME_SINCE_MILLIS, null);
                TimePoint toTimePoint = readTimePointParam(req, PARAM_NAME_TO, PARAM_NAME_TO_MILLIS, null);
                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", trackedRace.getRace().getName());
                JSONArray jsonCompetitors = new JSONArray();
                for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                    JSONObject jsonCompetitor = new JSONObject();
                    jsonCompetitor.put("name", competitor.getName());
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    JSONArray jsonFixes = new JSONArray();
                    track.lockForRead();
                    try {
                        Iterator<GPSFixMoving> fixIter;
                        if (sinceTimePoint == null) {
                            fixIter = track.getFixes().iterator();
                        } else {
                            fixIter = track.getFixesIterator(sinceTimePoint, /* inclusive */true);
                        }
                        while (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            if (toTimePoint != null && fix.getTimePoint() != null
                                    && toTimePoint.compareTo(fix.getTimePoint()) < 0) {
                                break;
                            }
                            JSONObject jsonFix = new JSONObject();
                            jsonFix.put("timepoint", fix.getTimePoint().asMillis());
                            jsonFix.put("latdeg", fix.getPosition().getLatDeg());
                            jsonFix.put("lngdeg", fix.getPosition().getLngDeg());
                            jsonFix.put("truebearingdeg", fix.getSpeed().getBearing().getDegrees());
                            jsonFix.put("knotspeed", fix.getSpeed().getKnots());
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
                            jsonFixes.add(jsonFix);
                        }
                    } finally {
                        track.unlockAfterRead();
                    }
                    jsonCompetitor.put("track", jsonFixes);
                    jsonCompetitors.add(jsonCompetitor);
                }
                jsonRace.put("competitors", jsonCompetitors);
                setJsonResponseHeader(resp);
                jsonRace.writeJSONString(resp.getWriter());
            } catch (InvalidDateException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't parse time specification " + e.getMessage());
            }
        }
    }

    private void showWaypoints(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TrackedRace trackedRace = getTrackedRace(req);
        if (trackedRace == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
        } else {
            try {
                TimePoint timePoint = readTimePointParam(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS,
                        trackedRace.getStartOfRace() != null ? trackedRace.getStartOfRace()
                                : trackedRace.getStartOfTracking() != null ? trackedRace.getStartOfTracking()
                                        : trackedRace.getTimePointOfNewestEvent() == null ? MillisecondsTimePoint.now()
                                                : trackedRace.getTimePointOfNewestEvent());
                JSONArray jsonWaypoints = new JSONArray();
                for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                    JSONObject jsonWaypoint = new JSONObject();
                    jsonWaypoint.put("name", waypoint.getName());
                    JSONArray jsonMarks = new JSONArray();
                    for (Mark mark : waypoint.getMarks()) {
                        JSONObject jsonMark = new JSONObject();
                        jsonMark.put("name", mark.getName());
                        GPSFixTrack<Mark, GPSFix> markTrack = trackedRace.getOrCreateTrack(mark);
                        GPSFix lastFixAtOrBefore = markTrack.getLastFixAtOrBefore(timePoint);
                        if (lastFixAtOrBefore != null) {
                            Position markPosition = lastFixAtOrBefore.getPosition();
                            if (markPosition != null) {
                                jsonMark.put("lat", markPosition.getLatDeg());
                                jsonMark.put("lng", markPosition.getLngDeg());
                            }
                        }
                        jsonMarks.add(jsonMark);
                    }
                    jsonWaypoint.put("marks", jsonMarks);
                    jsonWaypoints.add(jsonWaypoint);
                }
                setJsonResponseHeader(resp);
                jsonWaypoints.writeJSONString(resp.getWriter());
            } catch (InvalidDateException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't parse time specification " + e.getMessage());
            }
        }
    }

    private void showRace(HttpServletRequest req, HttpServletResponse resp) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        TrackedRace trackedRace = getTrackedRace(req);
        if (trackedRace == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
        } else {
            try {
                TimePoint timePoint = readTimePointParam(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS,
                        trackedRace.getTimePointOfNewestEvent()==null?MillisecondsTimePoint.now():trackedRace.getTimePointOfNewestEvent());
                String sinceUpdateString = req.getParameter(PARAM_NAME_SINCE_UPDATE);
                if (sinceUpdateString != null) {
                    int sinceUpdate = Integer.valueOf(sinceUpdateString);
                    // block until there is new data:
                    System.out.println("Blocking at "+trackedRace.getUpdateCount()+" waiting for "+sinceUpdate);
                    trackedRace.waitForNextUpdate(sinceUpdate);
                }
                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", trackedRace.getRace().getName());
                jsonRace.put("startoftracking", trackedRace.getStartOfTracking() == null ? 0l : trackedRace
                        .getStartOfTracking().asMillis());
                jsonRace.put("endoftracking", trackedRace.getEndOfTracking() == null ? 0l : trackedRace
                        .getEndOfTracking().asMillis());
                jsonRace.put("start", trackedRace.getStartOfRace() == null ? 0l : trackedRace.getStartOfRace().asMillis());
                jsonRace.put("end", trackedRace.getEndOfRace() == null ? 0l : trackedRace.getEndOfRace().asMillis());
                jsonRace.put("timeofnewestevent", trackedRace.getTimePointOfNewestEvent() == null ? 0l : trackedRace
                        .getTimePointOfNewestEvent().asMillis());
                jsonRace.put("timeoflastevent", trackedRace.getTimePointOfLastEvent() == null ? 0l : trackedRace
                        .getTimePointOfLastEvent().asMillis());
                jsonRace.put("updatecount", trackedRace.getUpdateCount());
                jsonRace.put("windaveragingintervalmillis", trackedRace.getMillisecondsOverWhichToAverageWind());
                jsonRace.put("speedaveragingintervalmillis", trackedRace.getMillisecondsOverWhichToAverageSpeed());
                Position positionForWind = null;
                TrackedLeg currentLeg = trackedRace.getCurrentLeg(timePoint);
                if (currentLeg != null) {
                    positionForWind = trackedRace.getOrCreateTrack(currentLeg.getLeg().getFrom().getMarks().iterator().next())
                            .getEstimatedPosition(timePoint, false);
                }
                Wind currentWind = trackedRace.getWind(positionForWind, timePoint);
                if (currentWind != null) {
                    JSONObject jsonWind = new JSONObject();
                    jsonWind.put("truebearingdeg", currentWind.getBearing().getDegrees());
                    jsonWind.put("knotspeed", currentWind.getKnots());
                    jsonWind.put("meterspersecondspeed", currentWind.getMetersPerSecond());
                    jsonRace.put("wind", jsonWind);
                }
                JSONArray jsonLegs = new JSONArray();
                for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                    JSONObject jsonLeg = new JSONObject();
                    jsonLeg.put("from", leg.getLeg().getFrom().getName());
                    jsonLeg.put("fromwaypointid", leg.getLeg().getFrom().getId());
                    jsonLeg.put("to", leg.getLeg().getTo().getName());
                    jsonLeg.put("towaypointid", leg.getLeg().getTo().getId());
                    try {
                        jsonLeg.put("upordownwindleg", leg.isUpOrDownwindLeg(timePoint));
                    } catch (NoWindException e) {
                        // no wind, then it's simply no upwind or downwind leg
                        jsonLeg.put("upordownwindleg", "false");
                    }
                    JSONArray jsonCompetitors = new JSONArray();
                    Map<Competitor, Integer> ranks = leg.getRanks(timePoint);
                    for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                        JSONObject jsonCompetitorInLeg = new JSONObject();
                        TrackedLegOfCompetitor trackedLegOfCompetitor = leg.getTrackedLeg(competitor);
                        if (trackedLegOfCompetitor != null) {
                            jsonCompetitorInLeg.put("name", competitor.getName());
                            Speed currentSpeedOverGround = trackedLegOfCompetitor.getSpeedOverGround(timePoint);
                            if (currentSpeedOverGround != null) {
                                jsonCompetitorInLeg.put("currentSpeedOverGroundInKnots",
                                        currentSpeedOverGround == null ? null : currentSpeedOverGround.getKnots());
                                jsonCompetitorInLeg.put("currentSpeedOverGroundInMetersPerSecond",
                                        currentSpeedOverGround == null ? null : currentSpeedOverGround.getMetersPerSecond());
                            }
                            Speed averageSpeedOverGround = trackedLegOfCompetitor.getAverageSpeedOverGround(timePoint);
                            if (averageSpeedOverGround != null) {
                                jsonCompetitorInLeg.put("averageSpeedOverGroundInKnots",
                                        averageSpeedOverGround.getKnots());
                                jsonCompetitorInLeg.put("averageSpeedOverGroundInMetersPerSecond",
                                        averageSpeedOverGround.getMetersPerSecond());
                            }
                            Speed currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinished =
                                    trackedLegOfCompetitor.hasFinishedLeg(timePoint) ?
                                            averageSpeedOverGround : currentSpeedOverGround;
                            if (currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinished != null) {
                                jsonCompetitorInLeg.put("currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinishedInKnots",
                                        currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinished.getKnots());
                                jsonCompetitorInLeg.put("currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinishedInMetersPerSecond",
                                        currentSpeedOverGroundOrAverageSpeedOverGroundIfLegFinished.getMetersPerSecond());
                            }
                            Distance distanceTraveled = trackedLegOfCompetitor.getDistanceTraveled(timePoint);
                            if (distanceTraveled != null) {
                                jsonCompetitorInLeg.put("distanceTraveledOverGroundInMeters",
                                        distanceTraveled.getMeters());
                            }
                            try {
                                Speed velocityMadeGood = trackedLegOfCompetitor.getVelocityMadeGood(timePoint, WindPositionMode.EXACT);
                                if (velocityMadeGood != null) {
                                    jsonCompetitorInLeg.put("velocityMadeGoodInKnots", velocityMadeGood.getKnots());
                                    jsonCompetitorInLeg.put("velocityMadeGoodInMetersPerSecond", velocityMadeGood.getMetersPerSecond());
                                }
                            } catch (NoWindException e) {
                                // well, we don't know the wind direction... then no VMG will be shown...
                            }
                            try {
                                Speed averageVelocityMadeGood = trackedLegOfCompetitor
                                        .getAverageVelocityMadeGood(timePoint);
                                if (averageVelocityMadeGood != null) {
                                    jsonCompetitorInLeg.put("averageVelocityMadeGoodInKnots",
                                            averageVelocityMadeGood.getKnots());
                                    jsonCompetitorInLeg.put("averageVelocityMadeGoodInMetersPerSecond",
                                            averageVelocityMadeGood.getMetersPerSecond());
                                }
                            } catch (NoWindException e1) {
                                // well, we don't know the wind direction... then no average VMG will be shown...
                            }
                            try {
                                Integer rank = ranks.get(competitor);
                                if (rank == null) {
                                    logger.warning("Can't find rank of competitor "+competitor+" in leg "+leg);
                                }
                                jsonCompetitorInLeg.put("rank", ranks.get(competitor));
                            } catch (RuntimeException re) {
                                if (re.getCause() != null && re.getCause() instanceof NoWindException) {
                                    // well, we don't know the wind direction, so we can't compute a ranking
                                } else {
                                    throw re;
                                }
                            }
                            try {
                                jsonCompetitorInLeg.put("gapToLeaderInSeconds",
                                        trackedLegOfCompetitor.getGapToLeader(timePoint, WindPositionMode.LEG_MIDDLE));
                            } catch (NoWindException e1) {
                                // well, we don't know the wind direction... then no gap to leader will be shown...
                            }
                            try {
                                final Duration estimatedTimeToNextMarkInSeconds = trackedLegOfCompetitor.getEstimatedTimeToNextMark(timePoint, WindPositionMode.EXACT);
                                jsonCompetitorInLeg.put("estimatedTimeToNextMarkInSeconds",
                                        estimatedTimeToNextMarkInSeconds==null?null:estimatedTimeToNextMarkInSeconds);
                            } catch (NoWindException e) {
                                // well, we don't know the wind direction... then no windward distance will be shown...
                            }
                            jsonCompetitorInLeg.put("windwardDistanceToGoInMeters", trackedLegOfCompetitor
                                    .getWindwardDistanceToGo(timePoint, WindPositionMode.LEG_MIDDLE).getMeters());
                            jsonCompetitorInLeg.put("started", trackedLegOfCompetitor.hasStartedLeg(timePoint));
                            jsonCompetitorInLeg.put("finished", trackedLegOfCompetitor.hasFinishedLeg(timePoint));
                            jsonCompetitors.add(jsonCompetitorInLeg);
                        }
                    }
                    jsonLeg.put("competitors", jsonCompetitors);
                    jsonLegs.add(jsonLeg);
                }
                jsonRace.put("legs", jsonLegs);
                JSONArray jsonRaceRanking = new JSONArray();
                for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                    JSONObject competitorRank = new JSONObject();
                    competitorRank.put("competitor", competitor.getName());
                    competitorRank.put("rank", trackedRace.getRank(competitor, timePoint));
                    jsonRaceRanking.add(competitorRank);
                }
                jsonRace.put("ranks", jsonRaceRanking);
                setJsonResponseHeader(resp);
                jsonRace.writeJSONString(resp.getWriter());
            } catch (InvalidDateException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't parse time specification " + e.getMessage());
            }
        }
        System.out.println("showrace took "+(System.currentTimeMillis()-start)+"ms");
    }

    private void listEvents(HttpServletResponse resp) throws IOException {
        // TODO one a new top-level event will have been introduced, produce it here
        JSONArray regattaList = new JSONArray();
        for (Regatta regatta : getService().getAllRegattas()) {
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("name", regatta.getName());
            if (regatta.getBoatClass() != null) {
                jsonEvent.put("boatclass", regatta.getBoatClass().getName());
            }
            JSONArray jsonCompetitors = new JSONArray();
            for (Competitor competitor : regatta.getAllCompetitors()) {
                JSONObject jsonCompetitor = new JSONObject();
                jsonCompetitor.put("name", competitor.getName());
                jsonCompetitor.put("sailID", competitor.getBoat()==null?"":competitor.getBoat().getSailID());
                final Nationality nationality = competitor.getTeam().getNationality();
                jsonCompetitor.put("nationality", nationality == null ? "" : nationality.getThreeLetterIOCAcronym());
                CountryCode countryCode = nationality == null ? null : nationality.getCountryCode();
                jsonCompetitor.put("nationalityISO2", countryCode == null ? "" : countryCode.getTwoLetterISOCode());
                jsonCompetitor.put("nationalityISO3", countryCode == null ? "" : countryCode.getThreeLetterISOCode());
                JSONArray jsonTeam = new JSONArray();
                for (Person sailor : competitor.getTeam().getSailors()) {
                    JSONObject jsonSailor = new JSONObject();
                    jsonSailor.put("name", sailor.getName());
                    jsonSailor.put("description", sailor.getDescription()==null?"":sailor.getDescription());
                    jsonTeam.add(jsonSailor);
                }
                jsonCompetitor.put("team", jsonTeam);
                jsonCompetitors.add(jsonCompetitor);
            }
            jsonEvent.put("competitors", jsonCompetitors);
            JSONArray jsonRaces = new JSONArray();
            for (RaceDefinition race : regatta.getAllRaces()) {
                // don't wait for the arrival of a tracked race; just ignore it if it's not currently being tracked
                TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
                if (trackedRace != null) {
                    JSONObject jsonRace = new JSONObject();
                    jsonRace.put("name", race.getName());
                    jsonRace.put("boatclass", race.getBoatClass() == null ? "" : race.getBoatClass().getName());
                    TimePoint start = trackedRace.getStartOfRace();
                    jsonRace.put("start", start == null ? Long.MAX_VALUE : start.asMillis());
                    JSONArray jsonLegs = new JSONArray();
                    for (Leg leg : race.getCourse().getLegs()) {
                        JSONObject jsonLeg = new JSONObject();
                        jsonLeg.put("start", leg.getFrom().getName());
                        jsonLeg.put("end", leg.getTo().getName());
                        jsonLegs.add(jsonLeg);
                    }
                    jsonRace.put("legs", jsonLegs);
                    jsonRaces.add(jsonRace);
                }
            }
            jsonEvent.put("races", jsonRaces);
            regattaList.add(jsonEvent);
        }
        setJsonResponseHeader(resp);
        regattaList.writeJSONString(resp.getWriter());
    }
}
