package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.WindImpl;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tractracadapter.RaceRecord;
import com.sap.sailing.server.gateway.SailingServerHttpServlet;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class AdminApp extends SailingServerHttpServlet {
    private static final long serialVersionUID = -6849138354941569249L;

    private static final String ACTION_NAME_ADD_WIND_TO_MARKS = "addwindtomarksforonehour";

    private static final String ACTION_NAME_LIST_RACES_IN_REGATTA = "listracesinregatta";

    private static final String ACTION_NAME_STOP_REGATTA = "stopregatta";

    private static final String ACTION_NAME_STOP_RACE = "stoprace";

    private static final String ACTION_NAME_SET_WIND = "setwind";

    private static final String ACTION_NAME_REMOVE_WIND = "removewind";

    private static final String ACTION_NAME_SHOW_WIND = "showwind";

    private static final String ACTION_NAME_SET_AVERAGING = "setaveraging";

    private static final String PARAM_NAME_WIND_AVERAGING_INTERVAL_MILLIS = "windaveragingintervalmillis";

    private static final String PARAM_NAME_SPEED_AVERAGING_INTERVAL_MILLIS = "speedaveragingintervalmillis";

    private static final String PARAM_NAME_EVENT_JSON_URL = "eventJSONURL";

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
    
    public AdminApp() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter(PARAM_ACTION);
            if (action != null) {
                if (ACTION_NAME_STOP_REGATTA.equals(action)) {
                    stopRegatta(req, resp);
                }  else if (ACTION_NAME_LIST_RACES_IN_REGATTA.equals(action)) {
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
        } catch (Exception e) {
            resp.getWriter().println("Error processing request:");
            e.printStackTrace(resp.getWriter());
        }
    }

    private void setAveraging(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                DynamicTrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
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
        List<RaceRecord> raceRecords = getTracTracAdapterFactory()
                .getOrCreateTracTracAdapter(getService().getBaseDomainFactory()).getTracTracRaceRecords(jsonURL, true)
                .getB();
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
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
                TimePoint time = readTimePointParam(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS, MillisecondsTimePoint.now());
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
                                    " in regatta "+regatta.getName()+" while computing wind lines on marks");
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
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);
                TimePoint from = readTimePointParam(req, PARAM_NAME_FROM_TIME, PARAM_NAME_FROM_TIME_MILLIS,
                        trackedRace.getStartOfRace()==null?new MillisecondsTimePoint(0):
                            /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis()-24*3600*1000));
                TimePoint to = readTimePointParam(req, PARAM_NAME_TO_TIME, PARAM_NAME_TO_TIME_MILLIS, MillisecondsTimePoint.now());
                JSONObject jsonWindTracks = new JSONObject();
                List<WindSource> windSources = new ArrayList<WindSource>();
                for (WindSource windSource : trackedRace.getWindSources()) {
                    windSources.add(windSource);
                }
                for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
                    windSources.remove(windSourceToExclude);
                }
                windSources.add(new WindSourceImpl(WindSourceType.COMBINED));
                for (WindSource windSource : windSources) {
                    JSONArray jsonWindArray = new JSONArray();
                    WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                    windTrack.lockForRead();
                    try {
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
                                        windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint())
                                        .getBearing().getDegrees());
                                jsonWind.put("dampenedknotspeed",
                                        windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint()).getKnots());
                                jsonWind.put("dampenedmeterspersecondspeed",
                                        windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint())
                                        .getMetersPerSecond());
                            }
                            if (wind.getPosition() != null) {
                                jsonWind.put("latdeg", wind.getPosition().getLatDeg());
                                jsonWind.put("lngdeg", wind.getPosition().getLngDeg());
                            }
                            jsonWindArray.add(jsonWind);
                        }
                    } finally {
                        windTrack.unlockAfterRead();
                    }
                    jsonWindTracks.put(windSource.toString(), jsonWindArray);
                }
                jsonWindTracks.writeJSONString(resp.getWriter());
            }
        }
    }

    private void setWind(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Regatta e = getRegatta(req);
        Iterable<Regatta> regattas;
        if (e == null) {
            regattas = getService().getAllRegattas();
        } else {
            regattas = Collections.singleton(e);
        }
        for (Regatta regatta : regattas) {
            RaceDefinition r = getRaceDefinition(regatta, req);
            Iterable<RaceDefinition> races;
            if (r == null) {
                races = regatta.getAllRaces();
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
                        TimePoint timePoint = readTimePointParam(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS, MillisecondsTimePoint.now());
                        Wind wind = new WindImpl(p, timePoint, speed);
                        final DynamicTrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getExistingTrackedRace(race);
                        if (trackedRace != null) {
                            trackedRace.recordWind(wind, new WindSourceImpl(WindSourceType.WEB));
                        }
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
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
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
                    WindSourceType windSourceType = WindSourceType.valueOf(sourceName);
                    if (windSourceType == null) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Wind source type name " + sourceName + " unknown");
                    } else {
                        try {
                            final DynamicTrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta)
                                    .getTrackedRace(race);
                            WindTrack windTrack = trackedRace.getOrCreateWindTrack(trackedRace.getWindSources(windSourceType).iterator().next());
                            TimePoint timePoint = readTimePointParam(req, PARAM_NAME_TIME, PARAM_NAME_TIME_MILLIS,
                                    MillisecondsTimePoint.now());
                            Wind wind = windTrack.getLastFixAtOrBefore(timePoint);
                            if (wind != null  && wind.getTimePoint().equals(timePoint)) {
                                windTrack.remove(wind);
                                resp.getWriter().println("Successfully removed entry "+wind);
                            } else {
                                resp.getWriter().println(
                                        "No wind recorded for regatta " + regatta.getName() + " and race " + race.getName()
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
        for (Util.Triple<Regatta, RaceDefinition, String> regattaAndRaceAndPort : getService().getWindTrackedRaces()) {
            JSONObject windTracker = new JSONObject();
            windTracker.put("regattaname", regattaAndRaceAndPort.getA().getName());
            windTracker.put("racename", regattaAndRaceAndPort.getB().getName());
            windTracker.put("windtrackerinfo", regattaAndRaceAndPort.getC());
            windTrackers.add(windTracker);
        }
        windTrackers.writeJSONString(resp.getWriter());
    }

    private void stopReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws SocketException, IOException {
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                getService().stopTrackingWind(regatta, race);
            }
        }
    }

    private void startReceivingExpeditionWindForRace(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
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
                    getService().startTrackingWind(regatta, race, Boolean.valueOf(correctByDeclination));
                }
            }
        }
    }

    private void stopRegatta(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException,
    InterruptedException {
        Regatta regatta = getRegatta(req);
        if (regatta != null) {
            getService().stopTracking(regatta);
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event not found");
        }
    }

    private void stopRace(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, InterruptedException {
        Regatta regatta = getRegatta(req);
        if (regatta != null) {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                getService().stopTracking(regatta, race);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Regatta not found");
        }
    }
}
