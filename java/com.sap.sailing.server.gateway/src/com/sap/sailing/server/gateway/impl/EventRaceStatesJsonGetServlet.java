package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.LastFlagsFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.StartTimeFinder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceStateOfSameDayHelper;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sse.common.TimePoint;

public class EventRaceStatesJsonGetServlet extends AbstractJsonHttpServlet {
    private static final long serialVersionUID = -4820965681871902242L;
    
    private final static Logger logger = Logger.getLogger(EventRaceStatesJsonGetServlet.class.getName());

    private static final String PARAM_NAME_EVENTID = "eventId";
    // private static final String PARAM_NAME_FILTER_BY_REGATTA = "filterByRegatta";
    private static final String PARAM_NAME_FILTER_BY_LEADERBOARD = "filterByLeaderboard";
    private static final String PARAM_NAME_FILTER_BY_COURSEAREA = "filterByCourseArea";
    private static final String PARAM_NAME_FILTER_BY_DAYOFFSET = "filterByDayOffset";

    public static final String FIELD_EVENT_NAME = "name";
    public static final String FIELD_EVENT_ID = "id";
    public static final String FIELD_RACE_STATES = "raceStates";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String eventIdParam = request.getParameter(PARAM_NAME_EVENTID);
        String filterByCourseAreaParam = request.getParameter(PARAM_NAME_FILTER_BY_COURSEAREA);
        String filterByLeaderboardParam = request.getParameter(PARAM_NAME_FILTER_BY_LEADERBOARD);
        String filterByDayOffsetParam = request.getParameter(PARAM_NAME_FILTER_BY_DAYOFFSET);
        boolean filterByDayOffset = false;
        Calendar dayToCheck = Calendar.getInstance();
        dayToCheck.setTime(new Date());
        if(filterByDayOffsetParam != null) {
            try {
                int dayOffset = Integer.parseInt(filterByDayOffsetParam);
                filterByDayOffset = true;
                dayToCheck.add(Calendar.DAY_OF_YEAR, dayOffset);
            } catch (NumberFormatException e) {
                // invalid integer
            }
        }
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
                    if(filterByCourseAreaParam == null || courseArea.getName().equals(filterByCourseAreaParam)) {
                        for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
                            if (filterByLeaderboardParam == null || leaderboard.getName().equals(filterByLeaderboardParam)) {
                                if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea().equals(courseArea)) {
                                    String leaderboardName = leaderboard.getName();
                                    String leaderboardDisplayName = leaderboard.getDisplayName();
                                    for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                        for (Fleet fleet : raceColumn.getFleets()) {
                                            if(!filterByDayOffset || isRaceStateOfSameDay(raceColumn, fleet, dayToCheck)) {
                                                JSONObject raceStateJson = createRaceStateJsonObject(raceColumn, fleet);
                                                raceStateJson.put("courseAreaName", courseArea.getName());
                                                raceStateJson.put("leaderboardName", leaderboardName);
                                                raceStateJson.put("leaderboardDisplayName", leaderboardDisplayName);
                                                raceStatesLogEntriesJson.add(raceStateJson);
                                            }
                                        }
                                    }
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

    private boolean isRaceStateOfSameDay(RaceColumn raceColumn, Fleet fleet, Calendar now) {
        boolean result = false;
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null && !raceLog.isEmpty()) {
            TimePoint startTime = new StartTimeFinder(raceLog).analyze();
            TimePoint finishedTime = new FinishedTimeFinder(raceLog).analyze();
            RaceLogFlagEvent abortingFlagEvent = new AbortingFlagFinder(raceLog).analyze();
            TimePoint abortingTime = abortingFlagEvent != null ? abortingFlagEvent.getLogicalTimePoint() : null;
            
            result = RaceStateOfSameDayHelper.isRaceStateOfSameDay(startTime, finishedTime, abortingTime, now);
        }
        return result;
    }
    
    private JSONObject createRaceStateJsonObject(RaceColumn raceColumn, Fleet fleet) {
        JSONObject result = new JSONObject();
        result.put("raceName", raceColumn.getName());
        result.put("fleetName", fleet.getName());
        RaceIdentifier raceIdentifier = raceColumn.getRaceIdentifier(fleet);
        result.put("trackedRaceName", raceIdentifier != null ? raceIdentifier.getRaceName() : null);
        result.put("trackedRaceId", raceIdentifier != null ? raceIdentifier.toString() : null);
        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        if (raceLog != null && !raceLog.isEmpty()) {
            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
            RaceLogRaceStatus status = state.getStatus();
            TimePoint startTime = state.getStartTime();
            TimePoint finishedTime = state.getFinishedTime();
            JSONObject raceLogStateJson = new JSONObject();
            result.put("raceState", raceLogStateJson);
            raceLogStateJson.put("startTime", startTime != null ? startTime.toString() : null);
            raceLogStateJson.put("endTime", finishedTime != null ? finishedTime.toString() : null);
            raceLogStateJson.put("lastStatus", status.name());
            ReadonlyGateStartRacingProcedure procedure = state.getTypedReadonlyRacingProcedure(ReadonlyGateStartRacingProcedure.class);
            if (procedure != null) {
                raceLogStateJson.put("pathfinderId", procedure.getPathfinder());
                raceLogStateJson.put("gateLineOpeningTime", procedure.getGateLaunchStopTime());
            }
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
            LastFlagsFinder lastFlagFinder = new LastFlagsFinder(raceLog);
            RaceLogFlagEvent lastFlagEvent = LastFlagsFinder.getMostRecent(lastFlagFinder.analyze());
            if (lastFlagEvent != null) {
                setLastFlagField(raceLogStateJson, lastFlagEvent.getUpperFlag().name(), lastFlagEvent.getLowerFlag().name(), lastFlagEvent.isDisplayed());
            } else if (status.equals(RaceLogRaceStatus.UNSCHEDULED) && abortingFlagEvent != null) {
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

    /**
     * Tries to convert the given identifier to a UUID. When the identifier is not a UUID, null is returned.
     * @param identifierToConvert the identifier as a String
     * @return a UUID when the given identifier is a UUID, null otherwise
     */
    private UUID convertIdentifierStringToUuid(String identifierToConvert) {
        UUID result = null;
        if (identifierToConvert != null) {
            try {
                result = UUID.fromString(identifierToConvert);
            } catch (IllegalArgumentException iae) {
                logger.warning("The identifier " + identifierToConvert + " could not be converted to a UUID");
                //in this case null is returned
            }
        }
        return result;
    }
}
