package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.util.InvalidDateException;

public class WindJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -1408004464252437535L;

    private static final String PARAM_NAME_WINDSOURCE = "windsource";
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
                String windSourceToRead = windSourceParam != null ? windSourceParam : "ALL";         

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

                JSONObject jsonWindTracks = new JSONObject();
                List<WindSource> windSources = getAvailableWindSources(trackedRace);
                for (WindSource windSource : windSources) {
                    if("ALL".equals(windSourceToRead) || windSource.getType().name().equalsIgnoreCase(windSourceToRead)) {
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
                }
                setJsonResponseHeader(resp);
                jsonWindTracks.writeJSONString(resp.getWriter());
            }
        }
    }

    private List<WindSource> getAvailableWindSources(TrackedRace trackedRace) {
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
