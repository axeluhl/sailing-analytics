package com.sap.sailing.racecommittee.app.deserialization.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.RaceGroup;
import com.sap.sailing.domain.base.SeriesWithRows;
import com.sap.sailing.domain.base.impl.RaceGroupImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.RaceGroupJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racegroup.SeriesWithRowsOfRaceGroupSerializer;

public class RaceGroupDeserializer implements JsonDeserializer<RaceGroup> {
	
	private JsonDeserializer<BoatClass> boatClassDeserializer;
	private JsonDeserializer<SeriesWithRows> seriesDeserializer;

	public RaceGroupDeserializer(
			JsonDeserializer<BoatClass> boatClassDeserializer,
			JsonDeserializer<SeriesWithRows> seriesDeserializer) {
		this.boatClassDeserializer = boatClassDeserializer;
		this.seriesDeserializer = seriesDeserializer;
	}

	public RaceGroup deserialize(JSONObject object)
			throws JsonDeserializationException {
		String name = object.get(RaceGroupJsonSerializer.FIELD_NAME).toString();
		BoatClass boatClass = null;
		CourseArea courseArea = null;
		
		if (object.containsKey(RaceGroupJsonSerializer.FIELD_COURSE_AREA)) {
			// TODO: deserialize CourseArea ...
		}
		if (object.containsKey(RaceGroupJsonSerializer.FIELD_BOAT_CLASS)) {
			boatClass = boatClassDeserializer.deserialize(
					Helpers.getNestedObjectSafe(object, RaceGroupJsonSerializer.FIELD_BOAT_CLASS));
		}
		
		Collection<SeriesWithRows> series = new ArrayList<SeriesWithRows>();
		for (Object seriesObject : Helpers.getNestedArraySafe(
				object, 
				SeriesWithRowsOfRaceGroupSerializer.FIELD_SERIES)) {
			JSONObject seriesJson = Helpers.toJSONObjectSafe(seriesObject);
			 series.add(seriesDeserializer.deserialize(seriesJson));
		}
		return new RaceGroupImpl(name, boatClass, courseArea, series);
	}
}