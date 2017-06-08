package com.sap.sailing.racecommittee.app.data.deserializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.racecommittee.app.domain.impl.CompetitorWithRaceRankImpl;
import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.racecommittee.app.domain.impl.RaceRankImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class LeaderboardDeserializer implements JsonDeserializer<LeaderboardResult> {

    @Override
    public LeaderboardResult deserialize(JSONObject object) throws JsonDeserializationException {
        List<CompetitorWithRaceRankImpl> competitorWithRaceRanks = new ArrayList<>();
        JSONArray competitors = (JSONArray) object.get("competitors");
        for (int i = 0; i < competitors.size(); i++) {
            JSONObject jsonCompetitor = (JSONObject) competitors.get(i);
            /* competitor data */
            String name = (String) jsonCompetitor.get("name");
            Serializable id = (Serializable) jsonCompetitor.get("id");
            CompetitorWithRaceRankImpl competitor =
                new CompetitorWithRaceRankImpl(id, name, /* color */ null, /* email */ null, /* flagImage */ null,
                    /* dynamicTeam */ null, /* dynamicBoat */ null, /* onTimeFactor */ null, /* onDistance */ null,
                    /* searchTag */ null);

            /* races with rank */
            JSONObject jsonRaceScores = (JSONObject) jsonCompetitor.get("raceScores");
            for (Map.Entry<Object, Object> raceScore : jsonRaceScores.entrySet()) {
                String key = (String) raceScore.getKey();
                JSONObject value = (JSONObject) raceScore.getValue();
                Long rank = (Long) value.get("rank");
                RaceRankImpl raceRank = new RaceRankImpl(key, rank);
                competitor.addRaceRank(raceRank);
            }

            competitorWithRaceRanks.add(competitor);
        }
        return new LeaderboardResult(competitorWithRaceRanks);
    }
}
