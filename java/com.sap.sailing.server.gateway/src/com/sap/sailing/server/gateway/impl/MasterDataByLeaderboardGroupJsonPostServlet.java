package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.NationalityJsonDeserialzer;
import com.sap.sailing.server.gateway.deserialization.impl.PersonJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.CompetitorMasterDataDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterData;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardMasterData;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.TeamJsonDeserializer;

public class MasterDataByLeaderboardGroupJsonPostServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;
    private SharedDomainFactory domainFactory;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer(domainFactory);
        
        JsonDeserializer<Nationality> nationalityDeserializer = new NationalityJsonDeserialzer();
        JsonDeserializer<Person> personDeserializer = new PersonJsonDeserializer(nationalityDeserializer );
        JsonDeserializer<Team> teamDeserializer = new TeamJsonDeserializer(personDeserializer );
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorMasterDataDeserializer(boatClassDeserializer , teamDeserializer , domainFactory);
        JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer = new LeaderboardMasterDataJsonDeserializer(competitorDeserializer);
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = new LeaderboardGroupMasterDataJsonDeserializer(
                leaderboardDeserializer);
        LeaderboardGroupMasterDataJsonDeserializer deserializer = new LeaderboardGroupMasterDataJsonDeserializer(null);
        domainFactory = DomainFactory.INSTANCE;
        JSONParser parser = new JSONParser();
        try {
            JSONArray leaderboardGroupsMasterDataJsonArray = (JSONArray) parser.parse(new InputStreamReader(req
                    .getInputStream()));
            for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
                JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
                LeaderboardGroupMasterData masterData = deserializer.deserialize(leaderBoardGroupMasterDataJson);
                createLeaderboardGroupWithAllRelatedObjects(masterData);
            }
        } catch (ParseException e) {
            resp.sendError(400);
            e.printStackTrace();
        }
    }

    private void createLeaderboardGroupWithAllRelatedObjects(LeaderboardGroupMasterData masterData) {
        Map<String, Leaderboard> existingLeaderboards = getService().getLeaderboards();
        for (Leaderboard board : masterData.getLeaderboards()) {
            if (existingLeaderboards.containsKey(board.getName())) {
                // Leaderboard exists
                continue;
            }
        }
    }

}
