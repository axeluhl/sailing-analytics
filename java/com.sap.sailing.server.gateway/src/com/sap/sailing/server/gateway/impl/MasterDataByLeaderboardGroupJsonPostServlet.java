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

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.masterdataimport.LeaderboardGroupMasterData;
import com.sap.sailing.server.gateway.AbstractJsonHttpServlet;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.masterdata.impl.LeaderboardGroupMasterDataJsonDeserializer;
import com.sap.sailing.server.operationaltransformation.CreationCount;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;

public class MasterDataByLeaderboardGroupJsonPostServlet extends AbstractJsonHttpServlet {

    private static final long serialVersionUID = 998103495657252850L;
    private DomainFactory domainFactory;

    private CreationCount creationCount;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        domainFactory = DomainFactory.INSTANCE;
        creationCount = new CreationCount();
        JsonDeserializer<LeaderboardGroupMasterData> leaderboardGroupMasterDataDeserializer = LeaderboardGroupMasterDataJsonDeserializer
                .create(domainFactory);
        JSONParser parser = new JSONParser();
        try {
            JSONArray leaderboardGroupsMasterDataJsonArray = (JSONArray) parser.parse(new InputStreamReader(req
                    .getInputStream()));
            for (Object leaderBoardGroupMasterData : leaderboardGroupsMasterDataJsonArray) {
                JSONObject leaderBoardGroupMasterDataJson = (JSONObject) leaderBoardGroupMasterData;
                LeaderboardGroupMasterData masterData = leaderboardGroupMasterDataDeserializer
                        .deserialize(leaderBoardGroupMasterDataJson);
                ImportMasterDataOperation op = new ImportMasterDataOperation(masterData);
                creationCount.add(getService().apply(op));
            }
        } catch (ParseException e) {
            resp.sendError(400);
            e.printStackTrace();
        }
        resp.getWriter().write(creationCount.toJson().toJSONString());
    }

  
}
