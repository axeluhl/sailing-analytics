package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.InvalidDateException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class WindJsonGetServlet extends AbstractJsonHttpServlet {
    static final String ALL = "ALL";

    private static final long serialVersionUID = -1408004464252437535L;

    private static final String PARAM_NAME_WINDSOURCE = "windsource";
    private static final String PARAM_NAME_WINDSOURCE_ID = "windsourceid";
    private static final String PARAM_NAME_FROM_TIME = "fromtime";
    private static final String PARAM_NAME_FROM_TIME_MILLIS = "fromtimeasmillis";
    private static final String PARAM_NAME_TO_TIME = "totime";
    private static final String PARAM_NAME_TO_TIME_MILLIS = "totimeasmillis";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Regatta regatta = getRegatta(req);
        if (regatta == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Regatta not found");
        } else {
            RaceDefinition race = getRaceDefinition(req);
            if (race == null) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Race not found");
            } else {
                String windSourceParam = req.getParameter(PARAM_NAME_WINDSOURCE);
                String windSourceToRead = windSourceParam != null ? windSourceParam : ALL;         
                String windSourceIdParam = req.getParameter(PARAM_NAME_WINDSOURCE_ID);
                String windSourceIdToRead = windSourceIdParam != null ? windSourceIdParam : null;

                TrackedRace trackedRace = getService().getOrCreateTrackedRegatta(regatta).getTrackedRace(race);

                TimePoint from;
                TimePoint to;
                try {
                    from = readTimePointParam(req, PARAM_NAME_FROM_TIME, PARAM_NAME_FROM_TIME_MILLIS,
                            trackedRace.getStartOfRace() == null ? new MillisecondsTimePoint(0) :
                                /* 24h before race start */ new MillisecondsTimePoint(trackedRace.getStartOfRace().asMillis()-24*3600*1000));
                } catch (InvalidDateException e1) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not parse the 'from' time");
                    return;
                }
                try {
                    to = readTimePointParam(req, PARAM_NAME_TO_TIME, PARAM_NAME_TO_TIME_MILLIS, MillisecondsTimePoint.now());
                } catch (InvalidDateException e1) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not parse the 'to' time");
                    return;
                }

                JSONObject jsonWindTracks = getResult(windSourceToRead, windSourceIdToRead, trackedRace, from, to);
                setJsonResponseHeader(resp);
                jsonWindTracks.writeJSONString(resp.getWriter());
            }
        }
    }

    JSONObject getResult(String windSourceToRead, String windSourceIdToRead, TrackedRace trackedRace, TimePoint from, TimePoint to) {
        Map<WindSource, List<Wind>> fixes = getRelevantFixes(windSourceToRead, windSourceIdToRead, trackedRace, from, to);
        JSONObject jsonWindTracks = getResultAsJsonObject(trackedRace, fixes);
        return jsonWindTracks;
    }

    private Map<WindSource, List<Wind>> getRelevantFixes(String windSourceToRead, String windSourceIdToRead, TrackedRace trackedRace, TimePoint from,
            TimePoint to) {
        // quickly extract relevant fixes to hold locks as shortly as possible; process later
        List<WindSource> windSources = getAvailableWindSources(trackedRace);
        Map<WindSource, List<Wind>> fixes = new HashMap<>();
        for (WindSource windSource : windSources) {
            if (ALL.equals(windSourceToRead) || windSource.getType().name().equalsIgnoreCase(windSourceToRead)) {
                if (windSourceIdToRead != null && windSource.getId() != null && !windSourceIdToRead.equalsIgnoreCase(windSource.getId().toString())) {
                    continue;
                }
                ArrayList<Wind> fixesForWindSource = new ArrayList<>();
                fixes.put(windSource, fixesForWindSource);
                WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
                windTrack.lockForRead();
                try {
                    Iterator<Wind> windIter = windTrack.getFixesIterator(from, /* inclusive */true);
                    while (windIter.hasNext()) {
                        Wind wind = windIter.next();
                        if (wind.getTimePoint().compareTo(to) > 0) {
                            break;
                        } else {
                            fixesForWindSource.add(wind);
                        }
                    }
                } finally {
                    windTrack.unlockAfterRead();
                }
            }
        }
        return fixes;
    }

    private JSONObject getResultAsJsonObject(TrackedRace trackedRace, Map<WindSource, List<Wind>> fixes) {
        JSONObject jsonWindTracks = new JSONObject();
        JSONArray jsonWindSourcesDisplayed = new JSONArray();
        for (WindSource windSource : fixes.keySet()) {
            JSONObject windSourceInformation = new JSONObject();
            windSourceInformation.put("typeName", windSource.getType().name());
            windSourceInformation.put("id", windSource.getId() != null ? windSource.getId().toString() : "");
            jsonWindSourcesDisplayed.add(windSourceInformation);
        }
        jsonWindTracks.put("availableWindSources", jsonWindSourcesDisplayed);
        for (Map.Entry<WindSource, List<Wind>> e : fixes.entrySet()) {
            WindSource windSource = e.getKey();
            JSONArray jsonWindArray = new JSONArray();
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
            for (Wind wind : e.getValue()) {
                JSONObject jsonWind = new JSONObject();
                jsonWind.put("truebearingdeg", wind.getBearing().getDegrees());
                jsonWind.put("knotspeed", wind.getKnots());
                jsonWind.put("meterspersecondspeed", wind.getMetersPerSecond());
                if (wind.getTimePoint() != null) {
                    jsonWind.put("timepoint", wind.getTimePoint().asMillis());
                    final Wind averagedWind = windTrack.getAveragedWind(wind.getPosition(), wind.getTimePoint());
                    jsonWind.put("dampenedtruebearingdeg", averagedWind.getBearing().getDegrees());
                    jsonWind.put("dampenedknotspeed", averagedWind.getKnots());
                    jsonWind.put("dampenedmeterspersecondspeed", averagedWind.getMetersPerSecond());
                }
                if (wind.getPosition() != null) {
                    jsonWind.put("latdeg", wind.getPosition().getLatDeg());
                    jsonWind.put("lngdeg", wind.getPosition().getLngDeg());
                }
                jsonWindArray.add(jsonWind);
            }
            jsonWindTracks.put(windSource.toString()+(windSource.getId() != null ? "-"+windSource.getId().toString() : ""), jsonWindArray);
        }
        return jsonWindTracks;
    }

    List<WindSource> getAvailableWindSources(TrackedRace trackedRace) {
        List<WindSource> windSources = new ArrayList<WindSource>();
        for (WindSource windSource : trackedRace.getWindSources()) {
            windSources.add(windSource);
        }
        for (WindSource windSourceToExclude : trackedRace.getWindSourcesToExclude()) {
            windSources.remove(windSourceToExclude);
        }
        windSources.add(new WindSourceImpl(WindSourceType.COMBINED));
        return windSources;
    }

}
