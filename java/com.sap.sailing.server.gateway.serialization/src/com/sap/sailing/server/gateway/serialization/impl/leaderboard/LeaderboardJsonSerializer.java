package com.sap.sailing.server.gateway.serialization.impl.leaderboard;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.gateway.serialization.ExtendableJsonSerializer;
import com.sap.sailing.server.gateway.serialization.ExtensionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class LeaderboardJsonSerializer extends ExtendableJsonSerializer<Leaderboard> {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_COURSE_AREA = "courseArea";
	public static final String FIELD_BOAT_CLASS = "boatClass";
	
	private JsonSerializer<BoatClass> boatClassSerializer;
	private JsonSerializer<CourseArea> courseAreaSerializer;
	
	public LeaderboardJsonSerializer(
			JsonSerializer<BoatClass> boatClassSerializer,
			JsonSerializer<CourseArea> courseAreaSerializer,
			ExtensionJsonSerializer<Leaderboard, ?> extensionSerializer) {
		super(extensionSerializer);
		this.courseAreaSerializer = courseAreaSerializer;
		this.boatClassSerializer = boatClassSerializer;
	}

	@Override
	protected JSONObject serializeFields(Leaderboard object) {
		JSONObject result = new JSONObject();
		
		result.put(FIELD_NAME, object.getName());
		
		if (object.getDefaultCourseArea() != null) {
			result.put(FIELD_COURSE_AREA, courseAreaSerializer.serialize(object.getDefaultCourseArea()));
		}
		
		if (object instanceof RegattaLeaderboard) {
			Regatta regatta = ((RegattaLeaderboard) object).getRegatta();
			if (regatta.getBoatClass() != null) {
				result.put(FIELD_BOAT_CLASS, boatClassSerializer.serialize(regatta.getBoatClass()));
			}
		}
		
		return result;
	}


}
