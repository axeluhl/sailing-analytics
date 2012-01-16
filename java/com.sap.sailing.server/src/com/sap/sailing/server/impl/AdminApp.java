package com.sap.sailing.server.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mongodb.MongoException;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.persistence.DomainObjectFactory;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.domain.persistence.MongoWindStoreFactory;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.server.Servlet;
import com.sap.sailing.util.InvalidDateException;

public class AdminApp extends Servlet {
    private static final long serialVersionUID = -6849138354941569249L;
    
    private static final String ACTION_NAME_ADD_WIND_TO_MARKS = "addwindtomarksforonehour";

    private static final String ACTION_NAME_ADD_EVENT = "addevent";

    private static final String ACTION_NAME_ADD_RACE = "addrace";
    
    private static final String ACTION_NAME_LIST_RACES_IN_EVENT = "listracesinevent";

    private static final String ACTION_NAME_STOP_EVENT = "stopevent";
    
    private static final String ACTION_NAME_STOP_RACE = "stoprace";
    
    private static final String ACTION_NAME_SET_WIND = "setwind";
    
    private static final String ACTION_NAME_REMOVE_WIND = "removewind";
    
    private static final String ACTION_NAME_SELECT_WIND_SOURCE = "selectwindsource";
    
    private static final String ACTION_NAME_SHOW_WIND = "showwind";
    
    private static final String ACTION_NAME_SET_AVERAGING = "setaveraging";
    
    private static final String PARAM_NAME_WIND_AVERAGING_INTERVAL_MILLIS = "windaveragingintervalmillis";
    
    private static final String PARAM_NAME_SPEED_AVERAGING_INTERVAL_MILLIS = "speedaveragingintervalmillis";

    private static final String PARAM_NAME_EVENT_JSON_URL = "eventJSONURL";

    private static final String PARAM_NAME_PARAM_URL = "paramURL";

    private static final String PARAM_NAME_LIVE_URI = "liveURI";

    private static final String PARAM_NAME_STORED_URI = "storedURI";

    private static final String PARAM_NAME_FROM_TIME = "fromtime";

    private static final String PARAM_NAME_FROM_TIME_MILLIS = "fromtimeasmillis";

    private static final String PARAM_NAME_TO_TIME = "totime";

    private static final String PARAM_NAME_TO_TIME_MILLIS = "totimeasmillis";

    private static final String PARAM_NAME_WINDSOURCE_NAME = "sourcename";
    
    private static final String PARAM_NAME_BEARING = "truebearingdegrees";
    
    private static final String PARAM_NAME_SPEED = "knotspeed";
    
    private static final String PARAM_NAME_LATDEG = "latdeg";
    
    private static final String PARAM_NAME_LNGDEG = "lngdeg";
    
    private static final String ACTION_NAME_LIST_WINDTRACKERS = "listwindtrackers";
    
    private static final String ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "receiveexpeditionwind";

    private static final String ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND = "stopreceivingexpeditionwind";

    private static final String PARAM_NAME_PORT = "port";

    private static final String PARAM_NAME_CORRECT_EXPEDITION_WIND_BEARING_BY_DECLINATION = "correctexpeditionwindbearingbydeclination";

    private static final String PARAM_NAME_WINDSTORE = "windstore";

    private static final String WIND_STORE_EMPTY = "empty";

    private static final String WIND_STORE_MONGO = "mongo";

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
                } else if (ACTION_NAME_ADD_RACE.equals(action)) {
                    addRace(req, resp);
                } else if (ACTION_NAME_LIST_RACES_IN_EVENT.equals(action)) {
                    listRacesInEvent(req, resp);
                } else if (ACTION_NAME_STOP_RACE.equals(action)) {
                    stopRace(req, resp);
                } else if (ACTION_NAME_SUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    startReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_UNSUBSCRIBE_RACE_FOR_EXPEDITION_WIND.equals(action)) {
                    stopReceivingExpeditionWindForRace(req, resp);
                } else if (ACTION_NAME_LIST_WINDTRACKERS.equals(action)) {
                    listWindTrackers(req, resp);
                } else if (ACTION_NAME_SET_WIND.equals(action)) {
                    setWind(req, resp);
                } else if (ACTION_NAME_REMOVE_WIND.equals(action)) {
                    removeWind(req, resp);
                } else if (ACTION_NAME_SELECT_WIND_SOURCE.equals(action)) {
                    selectWindSource(req, resp);
                } else if (ACTION_NAME_SHOW_WIND.equals(action)) {
                    showWind(req, resp);
                } else if (ACTION_NAME_ADD_WIND_TO_MARKS.equals(action)) {
                    addWindToMarks(req, resp);
                } else if (ACTION_NAME_SET_AVERAGING.equals(action)) {
                    setAveraging(req, resp);
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown action \""+action+"\"");
                }
            } else {
                resp.getWriter().println("Hello admin!");
            }
        } catch (Throwable e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }
    
    private void setAveraging(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                DynamicTrackedRace trackedRace = getService().getOrCreateTrackedEvent(event).getTrackedRace(race);
                String windAveragingIntervalInMIllis = req.getParameter(PARAM_NAME_WIND_AVERAGING_INTERVAL_MILLIS);
                if (windAveragingIntervalInMIllis != null) {
                    trackedRace.setMillisecondsOverWhichToAverageWind(Long.valueOf(windAveragingIntervalInMIllis));
                }
                String speedAveragingIntervalInMIllis = req.getParameter(PARAM_NAME_SPEED_AVERAGING_INTERVAL_MILLIS);
                if (speedAveragingIntervalInMIllis != null) {
                    trackedRace.setMillisecondsOverWhichToAverageSpeed(Long.valueOf(speedAveragingIntervalInMIllis));
                }
            }
        }
    }

    private void listRacesInEvent(HttpServletRequest req, HttpServletResponse resp) throws IOException, ParseException,
            org.json.simple.parser.ParseException, URISyntaxException {
        URL jsonURL = new URL(req.getParameter(PARAM_NAME_EVENT_JSON_URL));
        List<RaceRecord> raceRecords = getService().getTracTracRaceRecords(jsonURL).getB();
        JSONArray result = new JSONArray();
        for (RaceRecord raceRecord : raceRecords) {
            JSONObject jsonRaceRecord = new JSONObject();
            jsonRaceRecord.put("name", raceRecord.getName());
            jsonRaceRecord.put("ID", raceRecord.getID());
            jsonRaceRecord.put("paramURL", raceRecord.getParamURL());
            jsonRaceRecord.put("replayURL", raceRecord.getReplayURL());
            result.add(jsonRaceRecord);
        }
        result.writeJSONString(resp.getWriter());
    }

    private void addWindToMarks(HttpServletRequest req, HttpServletResponse resp) throws IOException,
            InvalidDateException, NoWindException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                TrackedRace trackedRace = getService().getOrCreateTrackedEvent(event).getTrackedRace(race);
                TimePoint time = getTimePoint(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS, MillisecondsTimePoint.now());
                TimePoint oneHourLater = new MillisecondsTimePoint(time.asMillis()+3600*1000);
                String[] latitudes = req.getParameterValues(PARAM_NAME_LATDEG);
                String[] longitudes = req.getParameterValues(PARAM_NAME_LNGDEG);
                JSONArray result = new JSONArray();
                if (latitudes != null && longitudes != null) {
                    for (int i = 0; i < Math.max(latitudes.length, longitudes.length); i++) {
                        double latDeg = Double.valueOf(latitudes[i]);
                        double lngDeg = Double.valueOf(longitudes[i]);
                        DegreePosition pos = new DegreePosition(latDeg, lngDeg);
                        Wind wind = trackedRace.getWind(pos, time);
                        if (wind == null) {
                            throw new NoWindException("No wind set for race "+race.getName()+
                                    " in event "+event.getName()+" while computing wind lines on marks");
                        }
                        Distance d = wind.travel(time, oneHourLater);
                        Position to = pos.translateGreatCircle(wind.getBearing(), d);
                        JSONObject record = new JSONObject();
                        record.put("markLatDeg", latDeg);
                        record.put("markLngDeg", lngDeg);
                        record.put("windTrueBearingDeg", wind.getBearing().getDegrees());
                        record.put("windKnotSpeed", wind.getKnots());
                        record.put("toLatDeg", to.getLatDeg());
                        record.put("toLngDeg", to.getLngDeg());
                        result.add(record);
                    }
                }
                result.writeJSONString(resp.getWriter());
            }
        }
    }

    private void showWind(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvalidDateException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                TrackedRace trackedRace = getService().getOrCreateTrackedEvent(event).getTrackedRace(race);
                TimePoint from = getTimePoint(req, PARAM_NAME_FROM_TIME, PARAM_NAME_FROM_TIME_MILLIS,
                        trackedRace.getStart()==null?new MillisecondsTimePoint(0):
                            /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStart().asMillis()-24*3600*1000));
                TimePoint to = getTimePoint(req, PARAM_NAME_TO_TIME, PARAM_NAME_TO_TIME_MILLIS, MillisecondsTimePoint.now());
                JSONObject jsonWindTracks = new JSONObject();
                jsonWindTracks.put("currentwindsource", trackedRace.getWindSource().toString());
                for (WindSource windSource : WindSource.values()) {
                    JSONArray jsonWindArray = new JSONArray();
                    WindTrack windTrack = trackedRace.getWindTrack(windSource);
                    synchronized (windTrack) {
                        Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                        while (windIter.hasNext()) {
                            Wind wind = windIter.next();
                            if (wind.getTimePoint().compareTo(to) > 0) {
                                break;
                            }
                            JSONObject jsonWind = new JSONObject();
                            jsonWind.put("truebearingdeg", wind.getBearing().getDegrees());
                            jsonWind.put("knotspeed", wind.getKnots());
                            jsonWind.put("meterspersecondspeed", wind.getMetersPerSecond());
                            if (wind.getTimePoint() != null) {
                                jsonWind.put("timepoint", wind.getTimePoint().asMillis());
                                jsonWind.put("dampenedtruebearingdeg",
                                        windTrack.getEstimatedWind(wind.getPosition(), wind.getTimePoint())
                                                .getBearing().getDegrees());
                                jsonWind.put("dampenedknotspeed",
                                        windTrack.getEstimatedWind(wind.getPosition(), wind.getTimePoint()).getKnots());
                                jsonWind.put("dampenedmeterspersecondspeed",
                                        windTrack.getEstimatedWind(wind.getPosition(), wind.getTimePoint())
                                                .getMetersPerSecond());
                            }
                            if (wind.getPosition() != null) {
                                jsonWind.put("latdeg", wind.getPosition().getLatDeg());
                                jsonWind.put("lngdeg", wind.getPosition().getLngDeg());
                            }
                            jsonWindArray.add(jsonWind);
                        }
                    }
                    jsonWindTracks.put(windSource.toString(), jsonWindArray);
                }
                jsonWindTracks.writeJSONString(resp.getWriter());
            }
        }
    }

    private void selectWindSource(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sourceName = req.getParameter(PARAM_NAME_WINDSOURCE_NAME);
        if (sourceName == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wind source name not provided");
        } else {
            try {
                WindSource windSource = WindSource.valueOf(sourceName);
                Event event = getEvent(req);
                if (event == null) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
                } else {
                    RaceDefinition race = getRaceDefinition(req);
                    if (race == null) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
                    } else {
                        DynamicTrackedRace trackedRace = getService().getOrCreateTrackedEvent(event)
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
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage.toString());
            }
        }
    }

    private void setWind(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event e = getEvent(req);
        Iterable<Event> events;
        if (e == null) {
            events = getService().getAllEvents();
        } else {
            events = Collections.singleton(e);
        }
        for (Event event : events) {
            RaceDefinition r = getRaceDefinition(event, req);
            Iterable<RaceDefinition> races;
            if (r == null) {
                races = event.getAllRaces();
            } else {
                races = Collections.singleton(r);
            }
            for (RaceDefinition race : races) {
                String bearingAsString = req.getParameter(PARAM_NAME_BEARING);
                if (bearingAsString != null) {
                    Bearing bearing = new DegreeBearingImpl(Double.valueOf(bearingAsString));
                    String speedAsString = req.getParameter(PARAM_NAME_SPEED);
                    SpeedWithBearing speed;
                    if (speedAsString == null) {
                        // only bearing provided; no speed; assume speed as 1kn
                        speed = new KnotSpeedWithBearingImpl(1, bearing);
                    } else {
                        speed = new KnotSpeedWithBearingImpl(Double.valueOf(speedAsString), bearing);
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
                        getService().getOrCreateTrackedEvent(event).getTrackedRace(race).recordWind(wind, WindSource.WEB);
                    } catch (InvalidDateException ex) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't parse time specification " + ex.getMessage());
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "wind bearing parameter "+PARAM_NAME_BEARING+" missing");
                }
            }
        }
    }

    private void removeWind(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                String sourceName = req.getParameter(PARAM_NAME_WINDSOURCE_NAME);
                if (sourceName == null) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wind source name not provided");
                } else {
                    WindSource windSource = WindSource.valueOf(sourceName);
                    if (windSource == null) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wind source name " + sourceName + " unknown");
                    } else {
                        try {
                            WindTrack windTrack = getService().getOrCreateTrackedEvent(event)
                                    .getTrackedRace(race).getWindTrack(windSource);
                            TimePoint timePoint = getTimePoint(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS,
                                    MillisecondsTimePoint.now());
                            Wind wind = windTrack.getLastFixAtOrBefore(timePoint);
                            if (wind != null  && wind.getTimePoint().equals(timePoint)) {
                                windTrack.remove(wind);
                                resp.getWriter().println("Successfully removed entry "+wind);
                            } else {
                                resp.getWriter().println(
                                        "No wind recorded for event " + event.getName() + " and race " + race.getName()
                                                + " at " + timePoint.asDate()+". No error, just no effect :-)");
                            }
                        } catch (InvalidDateException e) {
                            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't parse time specification " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void listWindTrackers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JSONArray windTrackers = new JSONArray();
        for (Triple<Event, RaceDefinition, String> eventAndRaceAndPort : getService().getWindTrackedRaces()) {
            JSONObject windTracker = new JSONObject();
            windTracker.put("eventname", eventAndRaceAndPort.getA().getName());
            windTracker.put("racename", eventAndRaceAndPort.getB().getName());
            windTracker.put("windtrackerinfo", eventAndRaceAndPort.getC());
            windTrackers.add(windTracker);
        }
        windTrackers.writeJSONString(resp.getWriter());
    }

    private void stopReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws SocketException, IOException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                getService().stopTrackingWind(event, race);
            }
        }
    }

    private void startReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Event event = getEvent(req);
        if (event == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                String portParam = req.getParameter(PARAM_NAME_PORT);
                if (portParam == null) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No port parameter provided");
                } else {
                    String correctByDeclination = req
                            .getParameter(PARAM_NAME_CORRECT_EXPEDITION_WIND_BEARING_BY_DECLINATION);
                    getService().startTrackingWind(event, race, Boolean.valueOf(correctByDeclination));
                }
            }
        }
    }

    private void stopEvent(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
            InterruptedException {
        Event event = getEvent(req);
        if (event != null) {
            getService().stopTracking(event);
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        }
    }

    private void addEvent(HttpServletRequest req, HttpServletResponse resp) throws URISyntaxException, IOException,
            ParseException, org.json.simple.parser.ParseException {
        URL jsonURL = new URL(req.getParameter(PARAM_NAME_EVENT_JSON_URL));
        URI liveURI = new URI(req.getParameter(PARAM_NAME_LIVE_URI));
        URI storedURI = new URI(req.getParameter(PARAM_NAME_STORED_URI));
        getService().addEvent(jsonURL, liveURI, storedURI, getWindStore(req), /* timeoutInMilliseconds */ 60000);
    }
    
    private WindStore getWindStore(HttpServletRequest req) throws UnknownHostException, MongoException {
        String windStore = req.getParameter(PARAM_NAME_WINDSTORE);
        if (windStore != null) {
            if (windStore.equals(WIND_STORE_EMPTY)) {
                return EmptyWindStore.INSTANCE;
            } else if (windStore.equals(WIND_STORE_MONGO)) {
                return MongoWindStoreFactory.INSTANCE.getMongoWindStore(MongoObjectFactory.INSTANCE, DomainObjectFactory.INSTANCE);
            } else {
                log("Couldn't find wind store "+windStore+". Using EmptyWindStore instead.");
                return EmptyWindStore.INSTANCE;
            }
        }
        return EmptyWindStore.INSTANCE;
    }

    private void addRace(HttpServletRequest req, HttpServletResponse resp) throws URISyntaxException, IOException,
            ParseException, org.json.simple.parser.ParseException {
        URL paramURL = new URL(req.getParameter(PARAM_NAME_PARAM_URL));
        URI liveURI = new URI(req.getParameter(PARAM_NAME_LIVE_URI));
        URI storedURI = new URI(req.getParameter(PARAM_NAME_STORED_URI));
        getService().addTracTracRace(paramURL, liveURI, storedURI, getWindStore(req), /* timeoutInMilliseconds */ 60000);
    }

    private void stopRace(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, InterruptedException {
        Event event = getEvent(req);
        if (event != null) {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                getService().stopTracking(event, race);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        }
    }

}
