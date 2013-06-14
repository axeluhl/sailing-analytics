package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardGroupMasterDataJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;

public class MasterDataByLeaderboardGroupJsonGetServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;
    
    private final JsonSerializer<RaceLogEvent> raceLogEventSerializer =  RaceLogEventSerializer.create(new CompetitorJsonSerializer(new TeamJsonSerializer(new PersonJsonSerializer())));
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JSONParser jsonParser = new JSONParser();
        Map<String, LeaderboardGroup> leaderboardGroups = getService().getLeaderboardGroups();
        Set<String> requestedLeaderboardGroupNames = new HashSet<String>();
        try {
            JSONArray requestedLeaderboardGroupNamesJson = (JSONArray) jsonParser.parse(req.getReader());
            for (int i = 0; i < requestedLeaderboardGroupNamesJson.size(); i++) {
                String name = (String) requestedLeaderboardGroupNamesJson.get(i);
                requestedLeaderboardGroupNames.add(name);
            }
        } catch (ParseException e) {
            // No range supplied. Export all for now
            requestedLeaderboardGroupNames.addAll(leaderboardGroups.keySet());
        }

        JSONArray masterData = new JSONArray();

        for (String name : requestedLeaderboardGroupNames) {
            LeaderboardGroup leaderboardGroup = leaderboardGroups.get(name);
            JsonSerializer<LeaderboardGroup> serializer = new LeaderboardGroupMasterDataJsonSerializer();
            masterData.add(serializer.serialize(leaderboardGroup));
        }

        setJsonResponseHeader(resp);
        masterData.writeJSONString(resp.getWriter());
    }

}
