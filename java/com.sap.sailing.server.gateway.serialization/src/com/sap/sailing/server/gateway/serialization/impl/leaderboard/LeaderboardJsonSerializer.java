package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;



public class LeaderboardJsonSerializer extends ExtendableJsonSerializer<Leaderboard> {

	public LeaderboardJsonSerializer(
			ExtensionJsonSerializer<Leaderboard, ?> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	protected JSONObject serializeFields(Leaderboard object) {
		JSONObject result = new JSONObject();
		
		result.put("name", object.getName());
		result.put("courseArea", "tdb");
		
		return result;
	}


}
