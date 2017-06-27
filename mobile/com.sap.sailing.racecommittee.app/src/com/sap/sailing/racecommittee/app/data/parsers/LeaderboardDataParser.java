package com.sap.sailing.racecommittee.app.data.parsers;

import java.io.Reader;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class LeaderboardDataParser implements DataParser<LeaderboardResult> {

    private JsonDeserializer<LeaderboardResult> deserializer;

    public LeaderboardDataParser(JsonDeserializer<LeaderboardResult> deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public LeaderboardResult parse(Reader reader) throws Exception {
        Object parsedResult = JSONValue.parseWithException(reader);
        JSONObject jsonLeaderboard = Helpers.toJSONObjectSafe(parsedResult);
        LeaderboardResult result = deserializer.deserialize(jsonLeaderboard);
        return result;
    }
}
