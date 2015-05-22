package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sse.common.Util.Pair;

public class EventRaceStatesSerializer implements JsonSerializer<Pair<Event, Iterable<Leaderboard>>> {
    public static final String FIELD_EVENT_NAME = "name";
    public static final String FIELD_EVENT_ID = "id";
    public static final String FIELD_RACE_STATES = "raceStates";
    
    private String filterByCourseArea;
    private String filterByLeaderboardName;
    private Calendar filterDay;

    public EventRaceStatesSerializer() {
        this.filterByCourseArea = null;
        this.filterByLeaderboardName = null;
        this.filterDay = null;
    }

    public EventRaceStatesSerializer(String filterByCourseArea, String filterByLeaderboard, String filterByDayOffset) {
        this.filterByCourseArea = filterByCourseArea;
        this.filterByLeaderboardName = filterByLeaderboard;
        
        Integer dayOffset = null;
        if(filterByDayOffset != null) {
            try {
                dayOffset = Integer.parseInt(filterByDayOffset);
                filterDay = Calendar.getInstance();
                filterDay.setTime(new Date());
                filterDay.add(Calendar.DAY_OF_YEAR, dayOffset);
            } catch (NumberFormatException e) {
                // invalid integer
                filterDay = null;
            }
        }
    }

    @Override
    public JSONObject serialize(Pair<Event, Iterable<Leaderboard>> eventAndLeaderboards) {
        Event event = eventAndLeaderboards.getA();
        Iterable<Leaderboard> leaderboards = eventAndLeaderboards.getB();
        
        JSONObject result = new JSONObject();
        result.put(FIELD_EVENT_NAME, event.getName());
        result.put(FIELD_EVENT_ID, event.getId().toString());
        JSONArray raceStatesLogEntriesJson = new JSONArray();
        result.put(FIELD_RACE_STATES, raceStatesLogEntriesJson);
        for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
            if(filterByCourseArea == null || courseArea.getName().equals(filterByCourseArea)) {
                for (Leaderboard leaderboard : leaderboards) {
                    RaceStateSerializer raceStateSerializer = new RaceStateSerializer(leaderboard);
                    if (filterByLeaderboardName == null || leaderboard.getName().equals(filterByLeaderboardName)) {
                        if (leaderboard.getDefaultCourseArea() != null && leaderboard.getDefaultCourseArea().equals(courseArea)) {
                            String leaderboardName = leaderboard.getName();
                            String leaderboardDisplayName = leaderboard.getDisplayName();
                            for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
                                for (Fleet fleet : raceColumn.getFleets()) {
                                    Pair<RaceColumn, Fleet> raceColumnAndFleet = new Pair<RaceColumn, Fleet>(raceColumn, fleet);
                                    if(filterDay == null || raceStateSerializer.isRaceStateOfSameDay(raceColumnAndFleet, filterDay)) {
                                        JSONObject raceStateJson = raceStateSerializer.serialize(raceColumnAndFleet);
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
        return result;
    }
}
