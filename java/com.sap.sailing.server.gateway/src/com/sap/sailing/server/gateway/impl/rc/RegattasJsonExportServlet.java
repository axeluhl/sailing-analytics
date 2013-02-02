package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEventTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.SerializationFilter;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceDefinitionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RegattaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WaypointJsonSerializer;


public class RegattasJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = -5661776042560467182L;
	
	private static final String PARAM_EVENT_FILTER = "event";
	private static final String PARAM_COURSE_AREA_FILTER = "courseArea";

	private UUID toUuid(String idValue) throws IllegalArgumentException {
		return UUID.fromString(idValue);
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String eventFilter = request.getParameter(PARAM_EVENT_FILTER);
		String courseAreaFilter = request.getParameter(PARAM_COURSE_AREA_FILTER);
		
		Iterable<Regatta> regattas = getService().getAllRegattas();
		
		if (eventFilter != null) {
			try {
				Serializable eventId = toUuid(eventFilter);
				Event event = getService().getEvent(eventId);
				if (event == null) {
					response.sendError(
							HttpServletResponse.SC_NOT_FOUND, 
							String.format("No event found with id %s", eventId));
					return;
				}
				regattas = event.getRegattas();
			} catch (IllegalArgumentException iae) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, iae.getMessage());
				return;
			}
		}
		
		SerializationFilter<RaceDefinition> raceFilter = null;
		if (courseAreaFilter != null) {
			System.out.println(courseAreaFilter);
			raceFilter = createFilter(courseAreaFilter);
		} else {
			raceFilter = new SerializationFilter.NoFilter<RaceDefinition>();
		}
		
		serializeRegattas(createSerializer(raceFilter), regattas, response);
	}

	private void serializeRegattas(
			JsonSerializer<Regatta> serializer, 
			Iterable<Regatta> regattas, 
			HttpServletResponse response) throws IOException {
		
		JSONArray result = new JSONArray();
		for (Regatta regatta : regattas)
		{
			result.add(serializer.serialize(regatta));
		}
		result.writeJSONString(response.getWriter());
		
	}
	
	private JsonSerializer<Regatta> createSerializer(SerializationFilter<RaceDefinition> raceFilter) {
		JsonSerializer<BoatClass> boatClassSerializer = new BoatClassJsonSerializer();
		JsonSerializer<ControlPoint> markSerializer = new MarkJsonSerializer();
		return new RegattaJsonSerializer(
			boatClassSerializer,
			new RaceDefinitionJsonSerializer(
				boatClassSerializer,
				new CourseJsonSerializer(
					new WaypointJsonSerializer(
						new ControlPointJsonSerializer(
								markSerializer, 
								new GateJsonSerializer(markSerializer))))),
			raceFilter);
	}
	
	private SerializationFilter<RaceDefinition> createFilter(final String courseAreaId) {
		
		return new SerializationFilter<RaceDefinition>() {
			@Override
			public boolean isFiltered(RaceDefinition race) {
				RacingEventService service = getService();
				Regatta regatta = service.getRememberedRegattaForRace(race.getId());
				if (regatta == null) {
					return true;
				} else {
					TrackedRace trackedRace = service.getTrackedRace(regatta, race);
					RaceCommitteeEventTrack track = trackedRace.getRaceCommitteeEventTrack();
					track.lockForRead();
					
					// filter for courseAreaId!
					
					track.unlockAfterRead();
					return trackedRace == null;
				}
			}
		};
	}

}
