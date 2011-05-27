package com.sap.sailing.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Distance;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Speed;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.util.DateParser;
import com.sap.sailing.server.util.InvalidDateException;

public class ModeratorApp extends Servlet {
    private static final long serialVersionUID = 1333207389294903999L;

    private static final String ACTION_NAME_LIST_EVENTS = "listevents";

    private static final String ACTION_NAME_SHOW_RACE = "showrace";
    
    private static final String ACTION_NAME_SHOW_WAYPOINTS = "showwaypoints";
    
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
                } else {
                    resp.sendError(500, "Unknown action \""+action+"\"");
                }
            } else {
                resp.getWriter().println("Hello moderator!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }

    private void showWaypoints(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        TrackedRace trackedRace = getTrackedRace(req);
        if (trackedRace == null) {
            resp.sendError(500, "Race not found");
        } else {
            String time = req.getParameter(PARAM_NAME_TIME);
            try {
                TimePoint timePoint;
                if (time != null && time.length() > 0) {
                    timePoint = new MillisecondsTimePoint(DateParser.parse(time).getTime());
                } else {
                    timePoint = MillisecondsTimePoint.now();
                }
                JSONArray jsonWaypoints = new JSONArray();
                for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                    JSONObject jsonWaypoint = new JSONObject();
                    jsonWaypoint.put("name", waypoint.getName());
                    JSONArray jsonBuoys = new JSONArray();
                    for (Buoy buoy : waypoint.getBuoys()) {
                        JSONObject jsonBuoy = new JSONObject();
                        jsonBuoy.put("name", buoy.getName());
                        GPSFixTrack<Buoy, GPSFix> buoyTrack = trackedRace.getTrack(buoy);
                        GPSFix lastFixAtOrBefore = buoyTrack.getLastFixAtOrBefore(timePoint);
                        if (lastFixAtOrBefore != null) {
                            Position buoyPosition = lastFixAtOrBefore.getPosition();
                            if (buoyPosition != null) {
                                jsonBuoy.put("lat", buoyPosition.getLatDeg());
                                jsonBuoy.put("lng", buoyPosition.getLngDeg());
                            }
                        }
                        jsonBuoys.add(jsonBuoy);
                    }
                    jsonWaypoint.put("buoys", jsonBuoys);
                    jsonWaypoints.add(jsonWaypoint);
                }
                jsonWaypoints.writeJSONString(resp.getWriter());
            } catch (InvalidDateException e) {
                resp.sendError(500, "Couldn't parse time specification " + time);
            }
        }
    }

    private TrackedRace getTrackedRace(HttpServletRequest req) {
        Event event = getEvent(req);
        RaceDefinition race = getRaceDefinition(req);
        TrackedRace trackedRace = null;
        if (event != null && race != null) {
            trackedRace = getService().getDomainFactory().trackEvent(event).getTrackedRace(race);
        }
        return trackedRace;
    }

    private void showRace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event event = getEvent(req);
        TrackedRace trackedRace = getTrackedRace(req);
        if (trackedRace == null) {
            resp.sendError(500, "Race not found");
        } else {
            String time = req.getParameter(PARAM_NAME_TIME);
            try {
                TimePoint timePoint;
                if (time != null && time.length() > 0) {
                    timePoint = new MillisecondsTimePoint(DateParser.parse(time).getTime());
                } else {
                    timePoint = MillisecondsTimePoint.now();
                }
                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", trackedRace.getRace().getName());
                jsonRace.put("start", trackedRace.getStart() == null ? 0l : trackedRace.getStart().asMillis());
                jsonRace.put("finish", trackedRace.getFirstFinish() == null ? 0l : trackedRace.getFirstFinish()
                        .asMillis());
                JSONArray jsonLegs = new JSONArray();
                for (TrackedLeg leg : trackedRace.getTrackedLegs()) {
                    JSONObject jsonLeg = new JSONObject();
                    jsonLeg.put("from", leg.getLeg().getFrom().getName());
                    jsonLeg.put("to", leg.getLeg().getTo().getName());
                    JSONArray jsonCompetitors = new JSONArray();
                    for (Competitor competitor : event.getCompetitors()) {
                        JSONObject jsonCompetitorInLeg = new JSONObject();
                        TrackedLegOfCompetitor trackedLegOfCompetitor = leg.getTrackedLeg(competitor);
                        jsonCompetitorInLeg.put("name", competitor.getName());
                        Speed averageSpeedOverGround = trackedLegOfCompetitor.getAverageSpeedOverGround(timePoint);
                        if (averageSpeedOverGround != null) {
                            jsonCompetitorInLeg.put("averageSpeedOverGroundInKnots", averageSpeedOverGround.getKnots());
                        }
                        Distance distanceTraveled = trackedLegOfCompetitor.getDistanceTraveled(timePoint);
                        if (distanceTraveled != null) {
                            jsonCompetitorInLeg.put("distanceTraveledOverGroundInMeters", distanceTraveled.getMeters());
                        }
                        try {
                            Speed velocityMadeGood = trackedLegOfCompetitor.getVelocityMadeGood(timePoint);
                            if (velocityMadeGood != null) {
                                jsonCompetitorInLeg.put("velocityMadeGoodInKnots", velocityMadeGood.getKnots());
                            }
                        } catch (NoWindException e) {
                            // well, we don't know the wind direction... then no VMG will be shown...
                        }
                        try {
                            Speed averageVelocityMadeGood = trackedLegOfCompetitor.getAverageVelocityMadeGood(timePoint);
                            if (averageVelocityMadeGood != null) {
                                jsonCompetitorInLeg.put("averageVelocityMadeGoodInKnots",
                                        averageVelocityMadeGood.getKnots());
                            }
                        } catch (NoWindException e1) {
                            // well, we don't know the wind direction... then no average VMG will be shown...
                        }
                        jsonCompetitorInLeg.put("rank", trackedLegOfCompetitor.getRank(timePoint));
                        try {
                            jsonCompetitorInLeg.put("gapToLeaderInSeconds",
                                    trackedLegOfCompetitor.getGapToLeaderInSeconds(timePoint));
                        } catch (NoWindException e1) {
                            // well, we don't know the wind direction... then no gap to leader will be shown...
                        }
                        try {
                            jsonCompetitorInLeg.put("windwardDistanceToGoInMeters", trackedLegOfCompetitor
                                    .getWindwardDistanceToGo(timePoint).getMeters());
                        } catch (NoWindException e) {
                            // well, we don't know the wind direction... then no windward distance will be shown...
                        }
                        jsonCompetitorInLeg.put("finished", trackedLegOfCompetitor.hasFinishedLeg(timePoint));
                        jsonCompetitors.add(jsonCompetitorInLeg);
                    }
                    jsonLeg.put("competitors", jsonCompetitors);
                    jsonLegs.add(jsonLeg);
                }
                jsonRace.put("legs", jsonLegs);
                jsonRace.writeJSONString(resp.getWriter());
            } catch (InvalidDateException e) {
                resp.sendError(500, "Couldn't parse time specification " + time);
            }
        }
    }

    private void listEvents(HttpServletResponse resp) throws IOException {
        JSONArray eventList = new JSONArray();
        for (Event event : getService().getAllEvents()) {
            JSONObject jsonEvent = new JSONObject();
            jsonEvent.put("name", event.getName());
            jsonEvent.put("boatclass", event.getBoatClass().getName());
            JSONArray jsonCompetitors = new JSONArray();
            for (Competitor competitor : event.getCompetitors()) {
                JSONObject jsonCompetitor = new JSONObject();
                jsonCompetitor.put("name", competitor.getName());
                jsonCompetitor.put("nationality", competitor.getTeam().getNationality().getThreeLetterAcronym());
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
            for (RaceDefinition race : event.getAllRaces()) {
                JSONObject jsonRace = new JSONObject();
                jsonRace.put("name", race.getName());
                jsonRace.put("boatclass", race.getBoatClass()==null?"":race.getBoatClass().getName());
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
            jsonEvent.put("races", jsonRaces);
            eventList.add(jsonEvent);
        }
        eventList.writeJSONString(resp.getWriter());
    }
}
