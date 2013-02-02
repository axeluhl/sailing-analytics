package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorIdJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeStartTimeEventSerializer;


public class EventTrackJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 2197006949440887925L;
	
	/**
	 * Created in init() - access is thread-safe.
	 */
	private JsonSerializer<RaceCommitteeEvent> rcEventSerializer;
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		JsonSerializer<Competitor> competitorSerializer = new CompetitorIdJsonSerializer();
		rcEventSerializer = new RaceCommitteeEventSerializer(
				new RaceCommitteeFlagEventSerializer(competitorSerializer),
				new RaceCommitteeStartTimeEventSerializer(competitorSerializer),
				new RaceCommitteeCourseAreaChangedEventSerializer(competitorSerializer));
	}
	
	@Override
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
		JSONArray result = new JSONArray();
		
		RaceCommitteeEventTrack track = trackedRace.getRaceCommitteeEventTrack();
		track.lockForRead();
		for (RaceCommitteeEvent event :  track.getFixes()) {
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

}
