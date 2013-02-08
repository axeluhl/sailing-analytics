package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.FleetWithRaceNamesJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.FleetWithRaceNamesOfSeriesExtensionSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.LeaderboardJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.leaderboard.SeriesOfLeaderboardExtensionSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.SeriesJsonSerializer;

public class LeaderboardJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 4510175441769759252L;
	
	private static final String PARAM_COURSE_AREA_FILTER = "courseArea";
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String courseAreaFilter = request.getParameter(PARAM_COURSE_AREA_FILTER);
		if (courseAreaFilter == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Need to set a course area filter.");
			return;
		}
		UUID courseAreaId = toUUID(courseAreaFilter);
		if (courseAreaId == null) { 
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Course are filter must be valid UUID.");
			return;
		}
		
		CourseArea filterCourseArea = getCourseArea(courseAreaId);
		
		JsonSerializer<Leaderboard> serializer = createSerializer();
		JSONArray result = new JSONArray();
		
		for (Leaderboard leaderboard : getService().getLeaderboards().values()) {
			if (leaderboard.getDefaultCourseArea().equals(filterCourseArea)) {
				result.add(serializer.serialize(leaderboard));
			}
		}
		
		result.writeJSONString(response.getWriter());
	}

	private CourseArea getCourseArea(UUID courseAreaId) {
		for (Event event : getService().getAllEvents()) {
			if (event.getVenue() == null) {
				continue;
			}
			for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
				if (courseArea.getId().equals(courseAreaId)) {
					return courseArea;
				}
			}
		}
		return null;
	}

	private UUID toUUID(String value) {
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException iae) {
			return null;
		}
	}
	
	private static JsonSerializer<Leaderboard> createSerializer() {
		return new LeaderboardJsonSerializer(
						new SeriesOfLeaderboardExtensionSerializer(
								new SeriesJsonSerializer(
										new FleetWithRaceNamesOfSeriesExtensionSerializer(
												new FleetWithRaceNamesJsonSerializer(
														new FleetJsonSerializer(
																new ColorJsonSerializer()))))));
		
	}

}
