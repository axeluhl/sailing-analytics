package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.GateLineOpeningTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastFlagFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.PathfinderFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;

public class RegattaRaceStatesJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -4820965681871902242L;

    private static final String PARAM_NAME_EVENTID = "eventId";

    public static final String FIELD_EVENT_NAME = "name";
    public static final String FIELD_EVENT_ID = "id";
    public static final String FIELD_RACE_STATES = "raceStates";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventIdParam = request.getParameter(PARAM_NAME_EVENTID);
        if (eventIdParam == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "You need to specify a event id using the "+
                    PARAM_NAME_EVENTID + " parameter");
        } else {
            Event event = getService().getEvent(convertIdentifierStringToUuid(eventIdParam));
            if (event == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Event "+eventIdParam+" not found");
            } else {
                JSONObject result = new JSONObject();
                result.put(FIELD_EVENT_NAME, event.getName());
                result.put(FIELD_EVENT_ID, event.getId().toString());
                JSONArray raceStatesLogEntriesJson = new JSONArray();
                result.put(FIELD_RACE_STATES, raceStatesLogEntriesJson);
                for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                    for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                        if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea().equals(courseArea)) {
                            String regattaDisplayName = leaderboard.getDisplayName();
                            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                for (Fleet fleet : raceColumn.getFleets()) {
                                    JSONObject raceStateJson = createRaceStateJsonObject(raceColumn, fleet);
                                    raceStateJson.put("courseAreaName", courseArea.getName());
                                    raceStateJson.put("regattaName", regattaDisplayName);
                                    raceStatesLogEntriesJson.add(raceStateJson);
                                }
                            }
                        }
                    }
                }
                setJsonResponseHeader(response);
                result.writeJSONString(response.getWriter());
            }
        }
    }
    
    private JSONObject createRaceStateJsonObject(RaceColumn raceColumn, Fleet fleet) {
        JSONObject result = new JSONObject();
        result.put("raceName", raceColumn.getName());
        result.put("fleetName", fleet.getName());
        RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        result.put("trackedRaceId", raceIdentifier != null ? raceIdentifier.toString() : null);
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null && !raceLog.isEmpty()) {
            JSONObject raceLogStateJson = new JSONObject();
            result.put("raceState", raceLogStateJson);
            StartTimeFinder startTimeFinder = new StartTimeFinder(raceLog);
            raceLogStateJson.put("startTime", startTimeFinder.getStartTime() != null ? startTimeFinder.getStartTime().toString() : null);
            RaceStatusAnalyzer raceStatusAnalyzer = new RaceStatusAnalyzer(raceLog);
            RaceLogRaceStatus lastStatus = raceStatusAnalyzer.getStatus();
            raceLogStateJson.put("lastStatus", lastStatus.name());
            PathfinderFinder pathfinderFinder = new PathfinderFinder(raceLog);
            raceLogStateJson.put("pathfinderId", pathfinderFinder.getPathfinderId());
            GateLineOpeningTimeFinder gateLineOpeningTimeFinder = new GateLineOpeningTimeFinder(raceLog);
            raceLogStateJson.put("gateLineOpeningTime", gateLineOpeningTimeFinder.getGateLineOpeningTime());
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.getAbortingFlagEvent();
            LastFlagFinder lastFlagFinder = new LastFlagFinder(raceLog);
            RaceLogFlagEvent lastFlagEvent = lastFlagFinder.getLastFlagEvent();
            if (lastFlagEvent != null) {
                setLastFlagField(raceLogStateJson, lastFlagEvent.getUpperFlag().name(), lastFlagEvent.getLowerFlag().name(), lastFlagEvent.isDisplayed());
            } else if (lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED) && abortingFlagEvent != null) {
                setLastFlagField(raceLogStateJson, abortingFlagEvent.getUpperFlag().name(), abortingFlagEvent.getLowerFlag().name(), abortingFlagEvent.isDisplayed());
            } else {
                setLastFlagField(raceLogStateJson, null, null, null);
            }
        }
        return result;
    }
    
    private void setLastFlagField(JSONObject raceLogStateJson, String upperFlagName, String lowerFlagName, Boolean isDisplayed) {
        raceLogStateJson.put("lastUpperFlag", upperFlagName);
        raceLogStateJson.put("lastLowerFlag", lowerFlagName);
        raceLogStateJson.put("displayed", isDisplayed);
    }

    private UUID convertIdentifierStringToUuid(String identifierToConvert) {
        UUID result = null;
        if (identifierToConvert != null) {
            try {
                result = UUID.fromString(identifierToConvert);
            } catch (IllegalArgumentException iae) {
            }
        }
        return result;
    }
}
