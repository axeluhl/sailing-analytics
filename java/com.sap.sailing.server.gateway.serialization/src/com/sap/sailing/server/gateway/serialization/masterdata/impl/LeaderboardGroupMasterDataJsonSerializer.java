package com.sap.sailing.server.gateway.serialization.masterdata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogSerializer;

public class LeaderboardGroupMasterDataJsonSerializer implements JsonSerializer<LeaderboardGroup> {

    public static final String FIELD_LEADERBOARDS = "leaderboards";
    public static final String FIELD_OVERALL_LEADERBOARD = "overallLeaderboard";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "name";
    private final JsonSerializer<Leaderboard> leadboardSerializer;

    /**
     * If masterdata is imported from a server where exported races where not tracked, data like race log competitor
     * data may be lost in the process of serialization
     */
    public LeaderboardGroupMasterDataJsonSerializer() {
        NationalityJsonSerializer nationalityJsonSerializer = new NationalityJsonSerializer();
        PersonJsonSerializer personSerializer = new PersonJsonSerializer(nationalityJsonSerializer);
        TeamJsonSerializer teamSerializer = new TeamJsonSerializer(personSerializer);
        BoatClassJsonSerializer boatClassSerializer = new BoatClassJsonSerializer();
        CompetitorMasterDataJsonSerializer competitorSerializer = new CompetitorMasterDataJsonSerializer(
                boatClassSerializer, teamSerializer);

        JsonSerializer<Color> colorSerializer = new ColorJsonSerializer();
        JsonSerializer<Fleet> fleetSerializer = new FleetJsonSerializer(colorSerializer);

        JsonSerializer<RaceLogEvent> raceLogEventSerializer = RaceLogEventSerializer.create(competitorSerializer);
        JsonSerializer<RaceLog> raceLogSerializer = new RaceLogSerializer(raceLogEventSerializer);

        JsonSerializer<RaceColumn> raceColumnSerializer = new RaceColumnMasterDataJsonSerializer(fleetSerializer,
                raceLogSerializer);
        leadboardSerializer = new LeaderboardMasterDataJsonSerializer(competitorSerializer, raceColumnSerializer);
    }

    @Override
    public JSONObject serialize(LeaderboardGroup leaderboardGroup) {
        JSONObject jsonLeaderboardGroup = new JSONObject();
        jsonLeaderboardGroup.put(FIELD_NAME, leaderboardGroup.getName());
        jsonLeaderboardGroup.put(FIELD_DESCRIPTION, leaderboardGroup.getDescription());
        jsonLeaderboardGroup.put(FIELD_OVERALL_LEADERBOARD,
                leadboardSerializer.serialize(leaderboardGroup.getOverallLeaderboard()));
        jsonLeaderboardGroup.put(FIELD_LEADERBOARDS, createJsonArrayForLeaderboards(leaderboardGroup.getLeaderboards()));

        return jsonLeaderboardGroup;
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
