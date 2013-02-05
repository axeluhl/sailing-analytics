package com.sap.sailing.racecommittee.app.domain.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.domain.base.impl.RaceGroupImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.LeaderboardJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.SeriesOfLeaderboardExtensionSerializer;

public class RaceGroupDeserializer implements JsonDeserializer<RaceGroup> {
	
	private JsonDeserializer<SeriesData> seriesDeserializer;

	public RaceGroupDeserializer(JsonDeserializer<SeriesData> seriesDeserializer) {
		this.seriesDeserializer = seriesDeserializer;
	}

	public RaceGroup deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(LeaderboardJsonSerializer.FIELD_NAME).toString();
		
		// CourseArea ... 
		
		Collection<SeriesData> series = new ArrayList<SeriesData>();
		for (Object seriesObject : Helpers.getNestedArraySafe(
				object, 
				SeriesOfLeaderboardExtensionSerializer.FIELD_SERIES)) {
			JSONObject seriesJson = Helpers.toJSONObjectSafe(seriesObject);
			 series.add(seriesDeserializer.deserialize(seriesJson));
		}
		return new RaceGroupImpl(name, null, series);
	}
}