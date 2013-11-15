package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

public class LeaderboardGroupMasterDataJsonSerializer implements JsonSerializer<LeaderboardGroup> {

    public static final String FIELD_LEADERBOARDS = "leaderboards";
    public static final String FIELD_HAS_OVERALL_LEADERBOARD = "overallLeaderboard";
    public static final String FIELD_OVERALL_LEADERBOARD_SCORING_SCHEME = "overallLeaderboardScoringScheme";
    public static final String FIELD_OVERALL_LEADERBOARD_DISCARDING_THRESHOLDS = "overallLeaderboardDiscardingThresholds";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DISPLAY_GROUPS_REVERSE = "displayGroupsReverse";
    public static final String FIELD_EVENTS = "events";
    public static final String FIELD_REGATTAS = "regattas";
    private final LeaderboardMasterDataJsonSerializer leadboardSerializer;
    private final Iterable<Event> allEvents;
    private final JsonSerializer<Event> eventSerializer;
    private final JsonSerializer<Regatta> regattaSerializer;

    /**
     * If masterdata is imported from a server where exported races are not tracked, data like race log competitor
     * data may be lost in the process of serialization
     * @param events 
     * @param regattaForRaceIdString 
     */
    public LeaderboardGroupMasterDataJsonSerializer(Iterable<Event> events, ConcurrentHashMap<String, Regatta> regattaForRaceIdStrings) {
        this.allEvents = events;
        NationalityJsonSerializer nationalityJsonSerializer = new NationalityJsonSerializer();
        PersonJsonSerializer personSerializer = new PersonJsonSerializer(nationalityJsonSerializer);
        TeamJsonSerializer teamSerializer = new TeamJsonSerializer(personSerializer);
        BoatClassJsonSerializer boatClassSerializer = new BoatClassJsonSerializer();
        CompetitorMasterDataJsonSerializer competitorSerializer = new CompetitorMasterDataJsonSerializer(
                boatClassSerializer, teamSerializer);
        JsonSerializer<Color> colorSerializer = new ColorJsonSerializer();
        JsonSerializer<Fleet> fleetSerializer = new FleetJsonSerializer(colorSerializer);
        JsonSerializer<RaceColumn> raceColumnSerializer = new RaceColumnMasterDataJsonSerializer();
        JsonSerializer<RaceLogEvent> raceLogEventSerializer = RaceLogEventSerializer.create(competitorSerializer);
        eventSerializer = new EventMasterDataJsonSerializer();
        leadboardSerializer = new LeaderboardMasterDataJsonSerializer(competitorSerializer, raceColumnSerializer, raceLogEventSerializer);
        regattaSerializer = new RegattaMasterDataJsonSerializer(fleetSerializer, raceColumnSerializer, regattaForRaceIdStrings);
    }

    @Override
    public JSONObject serialize(LeaderboardGroup leaderboardGroup) {
        JSONObject jsonLeaderboardGroup = new JSONObject();
        jsonLeaderboardGroup.put(FIELD_NAME, leaderboardGroup.getName());
        jsonLeaderboardGroup.put(FIELD_DESCRIPTION, leaderboardGroup.getDescription());
        jsonLeaderboardGroup.put(FIELD_HAS_OVERALL_LEADERBOARD, leaderboardGroup.getOverallLeaderboard() != null);
        if (leaderboardGroup.getOverallLeaderboard() != null) {
            // TODO see bug 1605; the overall leaderboard's column factors also need to be serialized
            jsonLeaderboardGroup.put(FIELD_OVERALL_LEADERBOARD_DISCARDING_THRESHOLDS,
                    LeaderboardMasterDataJsonSerializer.createJsonForResultDiscardingRule(leaderboardGroup
                            .getOverallLeaderboard().getResultDiscardingRule()));
            jsonLeaderboardGroup.put(FIELD_OVERALL_LEADERBOARD_SCORING_SCHEME,
                    LeaderboardMasterDataJsonSerializer.createJsonForScoringScheme(leaderboardGroup
                            .getOverallLeaderboard().getScoringScheme()));
        }
        jsonLeaderboardGroup.put(FIELD_LEADERBOARDS, createJsonArrayForLeaderboards(leaderboardGroup.getLeaderboards()));
        jsonLeaderboardGroup.put(FIELD_DISPLAY_GROUPS_REVERSE, leaderboardGroup.isDisplayGroupsInReverseOrder());
        
        //Important to call this after serializing leaderboards, as the leaderboard serializer has state
        jsonLeaderboardGroup.put(FIELD_EVENTS, createJsonArrayForEvents());
        jsonLeaderboardGroup.put(FIELD_REGATTAS, createJsonArrayForRegattas());
        
        return jsonLeaderboardGroup;
    }

    private JSONArray createJsonArrayForRegattas() {
        JSONArray array = new JSONArray();
        Iterable<Regatta> regattas = leadboardSerializer.getRegattas();
        for (Regatta regatta : regattas) {
            array.add(regattaSerializer.serialize(regatta));
        }
        return array;
    }

    /*
     * TODO, this is a hack to find out which events are needed for the exported 
     *  regatta leaderboards. should be replaced by a proper connection from regatta to event
     */
    private JSONArray createJsonArrayForEvents() {
        Set<String> courseAreaIds = leadboardSerializer.getCourseAreaIds();
        Set<Event> eventsThatShouldBeExported = new HashSet<Event>();
        for (Event event : allEvents) {
            boolean shouldBeExported = false;
            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
                if (courseAreaIds.contains(courseArea.getId().toString())) {
                    shouldBeExported = true;
                    break;
                }
            }
            if (shouldBeExported) {
                eventsThatShouldBeExported.add(event);
            }
        }
        
        JSONArray array = new JSONArray();
        for (Event event : eventsThatShouldBeExported) {
            array.add(eventSerializer.serialize(event));
        }
        return array;
    }

    private JSONArray createJsonArrayForLeaderboards(Iterable<Leaderboard> leaderboards) {
        JSONArray jsonLeaderBoards = new JSONArray();
        for (Leaderboard leaderboard : leaderboards) {
            JSONObject jsonLeaderboard = leadboardSerializer.serialize(leaderboard);
            jsonLeaderBoards.add(jsonLeaderboard);
        }
        return jsonLeaderBoards;
    }

}
