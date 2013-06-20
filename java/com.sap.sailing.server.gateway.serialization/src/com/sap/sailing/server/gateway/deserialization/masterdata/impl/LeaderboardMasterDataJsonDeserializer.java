package com.sap.sailing.server.gateway.deserialization.masterdata.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.impl.FlexibleLeaderboardMasterData;
import com.sap.sailing.domain.base.impl.RegattaLeaderboardMasterData;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.masterdata.impl.LeaderboardMasterDataJsonSerializer;

public class LeaderboardMasterDataJsonDeserializer implements JsonDeserializer<LeaderboardMasterData> {

    private final JsonDeserializer<Competitor> competitorDeserializer;
    private final DomainFactory domainFactory;

    public LeaderboardMasterDataJsonDeserializer(JsonDeserializer<Competitor> competitorDeserializer, DomainFactory domainFactory) {
        this.competitorDeserializer = competitorDeserializer;
        this.domainFactory = domainFactory;
    }

    @Override
    public LeaderboardMasterData deserialize(JSONObject object) throws JsonDeserializationException {
        if (object == null) {
            return null;
        }
        String name = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_NAME);
        String displayName = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_DISPLAY_NAME);
        int[] resultDiscardingRule = deserializeResultDesicardingRule((JSONObject) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_RESULT_DISCARDING_RULE));
        boolean isRegattaLeaderBoard = (Boolean) object
                .get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_LEADERBOARD);
        
        Set<Competitor> competitors = new HashSet<Competitor>();
        JSONArray competitorsJsonArray = (JSONArray) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COMPETITORS);

        for (Object obj : competitorsJsonArray) {
            JSONObject competitorJson = (JSONObject) obj;
            competitors.add(competitorDeserializer.deserialize(competitorJson));
        }
        if (isRegattaLeaderBoard) {
            String regattaName = (String) object.get(LeaderboardMasterDataJsonSerializer.FIELD_REGATTA_NAME); 
            return new RegattaLeaderboardMasterData(name, displayName, resultDiscardingRule, competitors, regattaName);
            
        } else {
            ScoringScheme scoringScheme = deserializeScoringScheme((JSONObject) object.get(LeaderboardMasterDataJsonSerializer.FIELD_SCORING_SCHEME));
            String courseAreaId = deserializeCourseAreaId((JSONObject) object.get(LeaderboardMasterDataJsonSerializer.FIELD_COURSE_AREA));
            return new FlexibleLeaderboardMasterData(name, displayName, resultDiscardingRule, competitors, scoringScheme, courseAreaId);
        }
    }

    private String deserializeCourseAreaId(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        String id = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_ID);
        return id;
        
        
    }

    private ScoringScheme deserializeScoringScheme(JSONObject jsonObject) {
        String type = (String) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_TYPE);
        return domainFactory.createScoringScheme(ScoringSchemeType.valueOf(type));
    }

    private int[] deserializeResultDesicardingRule(JSONObject jsonObject) {
        JSONArray indeces = (JSONArray) jsonObject.get(LeaderboardMasterDataJsonSerializer.FIELD_INDICES);
        if (indeces.size() < 1) {
            return null;
        }
        int[] result = new int[indeces.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((Long) indeces.get(i)).intValue();
        }
        return result;
    }
}
