package com.sap.sailing.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.KnotSpeedImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.server.util.InvalidDateException;

public class AdminApp extends Servlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String ACTION_NAME_ADD_EVENT = "addevent";

    private static final String ACTION_NAME_STOP_EVENT = "stopevent";
    
    private static final String ACTION_NAME_SET_WIND = "setwind";
    
    private static final String ACTION_NAME_SELECT_WIND_SOURCE = "selectwindsource";

    private static final String PARAM_WINDSOURCE_NAME = "sourcename";
    
    private static final String PARAM_NAME_BEARING = "truebearingdegrees";
    
    private static final String PARAM_NAME_SPEED = "knotspeed";
    
    private static final String PARAM_NAME_LATDEG = "latdeg";
    
    private static final String PARAM_NAME_LNGDEG = "lngdeg";
    
    private static final String ACTION_NAME_LIST_WINDTRACKERS = "listwindtrackers";
    
    private static final String ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "receiveexpeditionwind";

    private static final String ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "stopreceivingexpeditionwind";

    private static final String PARAM_NAME_PORT = "port";

    public AdminApp() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_ADD_EVENT.equals(action)) {
                    addEvent(req, resp);
                } else if (ACTION_NAME_STOP_EVENT.equals(action)) {
                    stopEvent(req, resp);
                } else if (ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    startReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    stopReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_LIST_WINDTRACKERS.equals(action)) {
                    listWindTrackers(req, resp);
                } else if (ACTION_NAME_SET_WIND.equals(action)) {
                    setWind(req, resp);
                } else if (ACTION_NAME_SELECT_WIND_SOURCE.equals(action)) {
                    selectWindSource(req, resp);
                }
            } else {
                resp.getWriter().println("Hello admin!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }
    
    private void selectWindSource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sourceName = req.getParameter(PARAM_WINDSOURCE_NAME);
        if (sourceName == null) {
            resp.sendError(500, "Wind source name not provided");
        } else {
            try {
                WindSource windSource = WindSource.valueOf(sourceName);
                Event event = getEvent(req);
                if (event == null) {
                    resp.sendError(500, "Event not found");
                } else {
                    RaceDefinition race = getRaceDefinition(req);
                    if (race == null) {
                        resp.sendError(500, "Race not found");
                    } else {
                        TrackedRace trackedRace = getService().getDomainFactory().trackEvent(event)
                                .getTrackedRace(race);
                        trackedRace.setWindSource(windSource);
                        resp.getWriter().println(
                                "Successfully set wind source for event " + event.getName() + " and race "
                                        + race.getName() + " to " + windSource);
                    }
                }
            } catch (IllegalArgumentException e) {
                StringBuilder errorMessage = new StringBuilder("Wind source name " + sourceName
                        + " not known. Known wind source names: ");
                boolean first = true;
                for (WindSource s : WindSource.values()) {
                    if (first) {
                        first = false;
                    } else {
                        errorMessage.append(", ");
                    }
                    errorMessage.append(s.toString());
                }
                resp.sendError(500, errorMessage.toString());
            }
        }
    }

    private void setWind(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(500, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(500, "Race not found");
            } else {
                String bearingAsString = req.getParameter(PARAM_NAME_BEARING);
                if (bearingAsString != null) {
                    Bearing bearing = new DegreeBearingImpl(Double.valueOf(bearingAsString));
                    String speedAsString = req.getParameter(PARAM_NAME_SPEED);
                    SpeedWithBearing speed;
                    if (speedAsString == null) {
                        // only bearing provided; no speed; assume speed as 1kn
                        speed = new KnotSpeedImpl(1, bearing);
                    } else {
                        speed = new KnotSpeedImpl(Double.valueOf(speedAsString), bearing);
                    }
                    Position p = null;
                    String lat = req.getParameter(PARAM_NAME_LATDEG);
                    if (lat != null) {
                        String lng = req.getParameter(PARAM_NAME_LNGDEG);
                        if (lng != null) {
                            p = new DegreePosition(Double.valueOf(lat), Double.valueOf(lng));
                        }
                    }
                    try {
                        TimePoint timePoint = getTimePoint(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS, MillisecondsTimePoint.now());
                        Wind wind = new WindImpl(p, timePoint, speed);
                        getService().getDomainFactory().trackEvent(event).getTrackedRace(race).recordWind(wind, WindSource.WEB);
                    } catch (InvalidDateException e) {
                        resp.sendError(500, "Couldn't parse time specification " + e.getMessage());
                    }
                }
            }
        }
    }

    private void listWindTrackers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONArray windTrackers = new JSONArray();
        for (Pair<Event, RaceDefinition> eventAndRace : getService().getWindTrackedRaces()) {
            JSONObject windTracker = new JSONObject();
            windTracker.put("eventname", eventAndRace.getA().getName());
            windTracker.put("racename", eventAndRace.getB().getName());
            windTrackers.add(windTracker);
        }
        windTrackers.writeJSONString(resp.getWriter());
    }

    private void stopReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws SocketException, IOException {
        RaceDefinition race = getRaceDefinition(req);
        if (race == null) {
            resp.sendError(500, "Race not found");
        } else {
            getService().stopTrackingWind(getEvent(req), race);
        }
    }

    private void startReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RaceDefinition race = getRaceDefinition(req);
        if (race == null) {
            resp.sendError(500, "Race not found");
        } else {
            int port = Integer.valueOf(req.getParameter(PARAM_NAME_PORT));
            getService().startTrackingWind(getEvent(req), race, port);
        }
    }

    private void stopEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
            InterruptedException {
        Event event = getEvent(req);
        if (event != null) {
            getService().stopTracking(event);
        } else {
            resp.sendError(500, "Event not found");
        }
    }

    private void addEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException,
            URISyntaxException, FileNotFoundException {
        URL paramURL = new URL(req.getParameter("paramURL"));
        URI liveURI = new URI(req.getParameter("liveURI"));
        URI storedURI = new URI(req.getParameter("storedURI"));
        getService().addEvent(paramURL, liveURI, storedURI);
    }

}
