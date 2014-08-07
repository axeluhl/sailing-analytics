package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.impl.StrippedLeaderboardGroupImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.EventBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.LeaderboardGroupBaseJsonSerializer;

public class LeaderboardGroupBaseJsonDeserializer implements JsonDeserializer<LeaderboardGroupBase> {

    public LeaderboardGroupBaseJsonDeserializer() {
    }

    public LeaderboardGroupBase deserialize(JSONObject object) throws JsonDeserializationException {
        UUID id = UUID.fromString((String) object.get(EventBaseJsonSerializer.FIELD_ID));
        String name = (String) object.get(LeaderboardGroupBaseJsonSerializer.FIELD_NAME);
        String description = (String) object.get(LeaderboardGroupBaseJsonSerializer.FIELD_DESCRIPTION);
        String displayName = (String) object.get(LeaderboardGroupBaseJsonSerializer.FIELD_DISPLAY_NAME);
        Boolean hasOverallLeaderboard = (Boolean) object.get(LeaderboardGroupBaseJsonSerializer.FIELD_HAS_OVERALL_LEADERBOARD);
        LeaderboardGroupBase result = new StrippedLeaderboardGroupImpl(id, name, description, displayName, 
                hasOverallLeaderboard == null ? false : hasOverallLeaderboard);
        return result;
    }
}
