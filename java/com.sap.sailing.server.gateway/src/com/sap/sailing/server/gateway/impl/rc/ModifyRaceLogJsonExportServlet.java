package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;


public class ModifyRaceLogJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 2197006949440887925L;

	/// TODO: for presentation purpose only - implementation needed
	@SuppressWarnings("unused")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		/// TODO: split race identifier into its part (leaderboard name, series name, fleet name, race column name)
		try {
			Object requestBody = JSONValue.parseWithException(request.getReader());
			JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
			
			/// TODO: deserialize request (including identifier and to-be-added event)
			String leaderboardName = "";
			String seriesName = "";
			String fleetName = "";
			String raceColumnName = "";
			RaceLogEvent raceLogEvent = null;
			
			Leaderboard leaderboard = getService().getLeaderboardByName(leaderboardName);
			if (leaderboard == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such leaderboard found.");
				return;
			}
			
			Fleet fleet = leaderboard.getFleet(fleetName);
			if (fleet == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such fleet found.");
				return;
			}
			
			RaceColumn raceColumn = leaderboard.getRaceColumnByName(raceColumnName);
			if (raceColumn == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such race column found.");
				return;
			}
			
			TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
			if (trackedRace == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "No such tracked race found.");
				return;
			}
			
			DynamicTrackedRace dynamicTrackedRace = getDynamicTrackedRace(trackedRace);
			if (dynamicTrackedRace == null) {
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
						"Tracked race cannot be modified right now.");
				return;
			}
			
			dynamicTrackedRace.recordRaceLogEvent(raceLogEvent);
		} catch (ParseException e) {
			throw new JsonDeserializationException("Invalid JSON in request body.", e);
		}
	}

	private DynamicTrackedRace getDynamicTrackedRace(TrackedRace trackedRace) {
		TrackedRegatta trackedRegatta = trackedRace.getTrackedRegatta();
		if (trackedRegatta == null) {
			return null;
		}
		DynamicTrackedRegatta dynamicTrackedRegatta = getService().getTrackedRegatta(trackedRegatta.getRegatta());
		if (dynamicTrackedRegatta == null) {
			return null;
		}
		return dynamicTrackedRegatta.getExistingTrackedRace(trackedRace.getRace());
	}

}
