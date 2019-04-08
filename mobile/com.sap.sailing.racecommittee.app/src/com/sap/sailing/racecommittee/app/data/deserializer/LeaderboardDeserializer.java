package com.sap.sailing.racecommittee.app.data.deserializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.Util;

public class LeaderboardDeserializer implements JsonDeserializer<LeaderboardResult> {

    @Override
    public LeaderboardResult deserialize(JSONObject object) throws JsonDeserializationException {
        Map<String, List<Util.Pair<Long, String>>> result = new HashMap<>();

        List<Util.Pair<Long, String>> rankList;
        JSONArray competitors = (JSONArray) object.get("competitors");
        for (int i = 0; i < competitors.size(); i++) {
            JSONObject jsonCompetitor = (JSONObject) competitors.get(i);
            String id = (String) jsonCompetitor.get("id");

            JSONObject jsonRaceScores = (JSONObject) jsonCompetitor.get("raceScores");
            for (Map.Entry<Object, Object> raceScore : jsonRaceScores.entrySet()) {
                String race = (String) raceScore.getKey();
                JSONObject jsonRace = (JSONObject) raceScore.getValue();
                Long rank = (Long) jsonRace.get("rank");
                rankList = result.get(race);
                if (rankList == null) {
                    rankList = new ArrayList<>();
                }
                rankList.add(new Util.Pair<>(rank, id));
                result.put(race, rankList);
            }
        }

        // sort by rank
        for (Map.Entry<String, List<Util.Pair<Long, String>>> item : result.entrySet()) {
            Collections.sort(item.getValue(), new Comparator<Util.Pair<Long, String>>() {
                @Override
                public int compare(Util.Pair<Long, String> left, Util.Pair<Long, String> right) {
                    return left.getA().intValue() - right.getA().intValue();
                }
            });
        }

        return new LeaderboardResult(result);
    }
}
