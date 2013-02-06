package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorIdJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogStartTimeEventSerializer;


public class RaceLogsJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 2197006949440887925L;


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Object requestBody = JSONValue.parseWithException(request.getReader());
			JSONArray requestedIds = Helpers.toJSONArraySafe(requestBody);
			
			for (Object element : requestedIds) {
				System.out.println(element);
			}
			
		} catch (ParseException e) {
			throw new JsonDeserializationException("Invalid JSON in request body.", e);
		}
		
	}
	
	
	private DynamicTrackedRace getDynamicTrackedRace(String regattaName, String raceName) {
		RegattaNameAndRaceName raceAndRegattaIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
		
		RaceDefinition raceDefinition = getService().getRace(raceAndRegattaIdentifier);
		Regatta regatta = getService().getRegatta(raceAndRegattaIdentifier);
		
		DynamicTrackedRegatta trackedRegatta = getService().getTrackedRegatta(regatta);
		DynamicTrackedRace trackedRace = trackedRegatta.getExistingTrackedRace(raceDefinition);
		
		return trackedRace;
	}
	
	private boolean validateParameter(HttpServletResponse response, String... parameters) throws IOException {
		for (String parameter : parameters) {
			if (parameter == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
				return false;
			}
		}
		return true;
	}

	private static JsonSerializer<RaceLogEvent> createSerializer() {
		JsonSerializer<Competitor> competitorSerializer = new CompetitorIdJsonSerializer();
		return new RaceLogEventSerializer(
				new RaceLogFlagEventSerializer(competitorSerializer),
				new RaceLogStartTimeEventSerializer(competitorSerializer),
				new RaceLogCourseAreaChangedEventSerializer(competitorSerializer));
	}
	/*@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		TrackedRace trackedRace = getTrackedRace(request);
		if (trackedRace == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		JSONArray result = serizalizeTrack(trackedRace);
		result.writeJSONString(response.getWriter());
	}

	private JSONArray serizalizeTrack(TrackedRace trackedRace) {
		JsonSerializer<RaceLogEvent> rcEventSerializer = createSerializer();
		JSONArray result = new JSONArray();
		
		RaceLog track = trackedRace.getRaceLog();
		track.lockForRead();
		for (RaceLogEvent event :  track.getFixes()) {
				result.add(rcEventSerializer.serialize(event));
		}
		track.unlockAfterRead();
		
		return result;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String regattaName = request.getParameter(PARAM_NAME_REGATTANAME);
		String raceName = request.getParameter(PARAM_NAME_RACENAME);
		if (!validateParameter(response, regattaName, raceName)) return;
		
		DynamicTrackedRace trackedRace = getDynamicTrackedRace(regattaName, raceName);
		if (trackedRace == null)
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// TODO: add event to trackedRace...
		// trackedRace.recordRaceCommitteeEvent(null);
		response.sendError(HttpServletResponse.SC_NO_CONTENT);
		
	}*/

}
