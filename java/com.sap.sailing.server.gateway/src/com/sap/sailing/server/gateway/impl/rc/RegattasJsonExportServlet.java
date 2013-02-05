package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.filter.Filter;
import com.sap.sailing.server.gateway.serialization.impl.BoatClassJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.race.RaceDefinitionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.ColorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.FleetJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.FleetWithRacesJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.FleetWithRacesOfSeriesExtensionSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.RegattaJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.SeriesJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.regatta.SeriesOfRegattaExtensionSerializer;


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
		
		Filter<RaceDefinition> raceFilter = null;
		if (courseAreaFilter != null) {
			raceFilter = createFilter(courseAreaFilter);
		} else {
			raceFilter = new Filter.NoFilter<RaceDefinition>();
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
	
	private JsonSerializer<Regatta> createSerializer(Filter<RaceDefinition> raceFilter) {
		JsonSerializer<BoatClass> boatClassSerializer = new BoatClassJsonSerializer();
		return new RegattaJsonSerializer(
						boatClassSerializer, 
						new SeriesOfRegattaExtensionSerializer(
								new SeriesJsonSerializer(
										new FleetWithRacesOfSeriesExtensionSerializer(
												new FleetWithRacesJsonSerializer(
														new FleetJsonSerializer(
																new ColorJsonSerializer()), 
																raceFilter, 
																new RaceDefinitionJsonSerializer(
																		boatClassSerializer))))));
	}
	
	private Filter<RaceDefinition> createFilter(final String courseAreaId) {
		
		return new Filter<RaceDefinition>() {
			@Override
			public boolean isFiltered(RaceDefinition race) {
				RacingEventService service = getService();
				Regatta regatta = service.getRememberedRegattaForRace(race.getId());
				if (regatta == null) {
					return true;
				} else {
					TrackedRace trackedRace = service.getTrackedRace(regatta, race);
					RaceLog log = trackedRace.getRaceLog();
					log.lockForRead();
					
					// filter for courseAreaId!
					
					log.unlockAfterRead();
					return trackedRace == null;
				}
			}
		};
	}

}
