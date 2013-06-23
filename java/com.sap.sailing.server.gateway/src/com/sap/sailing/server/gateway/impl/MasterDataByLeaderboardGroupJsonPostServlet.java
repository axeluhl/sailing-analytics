package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.io.InputStreamReader;

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
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.Person;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.domain.base.impl.EventMasterData;
import com.sap.sailing.domain.base.impl.LeaderboardGroupMasterData;
import com.sap.sailing.domain.base.impl.RegattaMasterData;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.BoatClassJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.ColorDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.FleetDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.NationalityJsonDeserialzer;
import com.sap.sailing.server.gateway.deserialization.impl.PersonJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.TeamJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.CompetitorMasterDataDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.EventMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardMasterDataJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.RegattaMasterDataJsonDeserializer;
import com.sap.sailing.server.operationaltransformation.CreationCount;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;

public class MasterDataByLeaderboardGroupJsonPostServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;
    private DomainFactory domainFactory;

    private CreationCount creationCount;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        domainFactory = DomainFactory.INSTANCE;
        //Initialize just in case the servlet does not even run any import operations
        creationCount = new CreationCount();
        JsonDeserializer<BoatClass> boatClassDeserializer = new BoatClassJsonDeserializer(domainFactory);

        JsonDeserializer<Nationality> nationalityDeserializer = new NationalityJsonDeserialzer();
        JsonDeserializer<Person> personDeserializer = new PersonJsonDeserializer(nationalityDeserializer);
        JsonDeserializer<Team> teamDeserializer = new TeamJsonDeserializer(personDeserializer);
        JsonDeserializer<Competitor> competitorDeserializer = new CompetitorMasterDataDeserializer(
                boatClassDeserializer, teamDeserializer, domainFactory);
        JsonDeserializer<LeaderboardMasterData> leaderboardDeserializer = new LeaderboardMasterDataJsonDeserializer(
                competitorDeserializer, domainFactory);
        JsonDeserializer<EventMasterData> eventDeserializer = new EventMasterDataJsonDeserializer();
        JsonDeserializer<Color> colorDeserializer = new ColorDeserializer();
        JsonDeserializer<Fleet> fleetDeserializer = new FleetDeserializer(colorDeserializer);
        JsonDeserializer<RegattaMasterData> regattaDeserializer = new RegattaMasterDataJsonDeserializer(
                fleetDeserializer);
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = new LeaderboardGroupMasterDataJsonDeserializer(
                leaderboardDeserializer, eventDeserializer, regattaDeserializer);
        JSONParser parser = new JSONParser();
        try {
            JSONArray leaderboardGroupsMasterDataJsonArray = (JSONArray) parser.parse(new InputStreamReader(req
                    .getInputStream()));
            for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
                JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
                LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                        .deserialize(leaderBoardGroupMasterDataJson);
                ImportMasterDataOperation op = new ImportMasterDataOperation(masterData);
                creationCount = getService().apply(op);
            }
        } catch (ParseException e) {
            resp.sendError(400);
            e.printStackTrace();
        }
        resp.getWriter().write(creationCount.toJson().toJSONString());
    }

  
}
