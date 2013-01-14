package com.sap.sailing.server.gateway.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStore;
import com.sap.sailing.domain.racecommittee.impl.EmptyRaceCommitteeStore;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeEventTrackImpl;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeFlagEventImpl;
import com.sap.sailing.domain.racecommittee.impl.RaceCommitteeStartTimeEventImpl;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.impl.DynamicTrackedRaceImpl;
import com.sap.sailing.domain.tracking.impl.EmptyWindStore;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeEventSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeEventSerializerFactory;


public class EventTrackRaceCommitteeServlet extends JsonExportServlet {
	private static final long serialVersionUID = 2197006949440887925L;
	
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
		
		RaceCommitteeEventSerializerFactory serializerFactory = new RaceCommitteeEventSerializerFactory();
		RaceCommitteeEventTrack track = trackedRace.getOrCreateRaceCommitteeEventTrack();
		
		track.lockForRead();
		for (RaceCommitteeEvent event :  track.getFixes()) {
				result.add(serializerFactory.getSerializer(event).serialize(event));
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
		response.sendError(HttpServletResponse.SC_ACCEPTED);
		
	}

	private DynamicTrackedRace getDynamicTrackedRace(String regattaName,
			String raceName) {
		RegattaNameAndRaceName nameIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
		DynamicTrackedRace trackedRace = 
				getService().getOrCreateTrackedRegatta(
						getService().getRegattaByName(regattaName)).
							getExistingTrackedRace(getService().getRace(nameIdentifier));
		return trackedRace;
	}

	@Override
	protected TrackedRace getTrackedRace(HttpServletRequest request) {
		RaceCommitteeStore store = new FakeStore();
		TrackedRace trackedRace = new DynamicTrackedRaceImpl(
				null, 
				new RaceDefinitionImpl("Race One", 
						new CourseImpl("Course IA", Collections.<Waypoint>emptyList()), 
						new BoatClassImpl("MyBoats", false), 
						Collections.<Competitor>emptyList()), 
				EmptyWindStore.INSTANCE, 
				1, 1, 1, 
				store);
		return trackedRace;
	}
	
	class FakeStore extends EmptyRaceCommitteeStore implements RaceCommitteeStore {
		@Override
		public RaceCommitteeEventTrack getRaceCommitteeEventTrack(
				TrackedRegatta trackedRegatta, TrackedRace trackedRace) {
			
			RaceCommitteeEventTrack track = new RaceCommitteeEventTrackImpl("karlos");
			track.add(
					new RaceCommitteeFlagEventImpl(new MillisecondsTimePoint(new Date()), UUID.randomUUID(), null, 
							0, Flags.ALPHA, Flags.NOVEMBER,  true));
			return track;
		}
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
