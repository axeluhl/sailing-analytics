package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.racecommittee.app.domain.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.SeriesWithRows;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.LeaderboardJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.SeriesDataOfLeaderboardExtensionSerializer;

public class RaceGroupDeserializer implements JsonDeserializer<RaceGroup> {
	
	private JsonDeserializer<SeriesWithRows> seriesDeserializer;

	public RaceGroupDeserializer(JsonDeserializer<SeriesWithRows> seriesDeserializer) {
		this.seriesDeserializer = seriesDeserializer;
	}

	public RaceGroup deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(LeaderboardJsonSerializer.FIELD_NAME).toString();
		
		// CourseArea ... 
		
		Collection<SeriesWithRows> series = new ArrayList<SeriesWithRows>();
		for (Object seriesObject : Helpers.getNestedArraySafe(
				object, 
				SeriesDataOfLeaderboardExtensionSerializer.FIELD_SERIES)) {
			JSONObject seriesJson = Helpers.toJSONObjectSafe(seriesObject);
			 series.add(seriesDeserializer.deserialize(seriesJson));
		}
		return new RaceGroupImpl(name, null, series);
	}
}