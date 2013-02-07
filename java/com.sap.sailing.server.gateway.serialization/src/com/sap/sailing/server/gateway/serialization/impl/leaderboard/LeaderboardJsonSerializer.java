package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;



public class LeaderboardJsonSerializer extends ExtendableJsonSerializer<Leaderboard> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_COURSE_AREA = "courseArea";
	
	
	public LeaderboardJsonSerializer(
			ExtensionJsonSerializer<Leaderboard, ?> extensionSerializer) {
		super(extensionSerializer);
	}

	@Override
	protected JSONObject serializeFields(Leaderboard object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		result.put(FIELD_COURSE_AREA, "tdb");
		
		return result;
	}


}
