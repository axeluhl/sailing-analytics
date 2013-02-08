package com.sap.sailing.server.gateway.impl.rc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.impl.JsonExportServlet;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorIdJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogCourseAreaChangedEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogStartTimeEventSerializer;


public class QueryRaceLogJsonExportServlet extends JsonExportServlet {
	private static final long serialVersionUID = 2197006949440887925L;
	private static final Logger logger = Logger.getLogger(QueryRaceLogJsonExportServlet.class.getName());

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			Object requestBody = JSONValue.parseWithException(request.getReader());
			JSONArray requestedIdentifiers = Helpers.toJSONArraySafe(requestBody);

			for (Object element : requestedIdentifiers) {
				logger.info(String.format("Client requests race log for race with identifier %s.", element));
				/// TODO: deserialize identifier and look up TrackedRace
			}
			/// TODO: Return RaceLogs for found TrackedRaces...
			
		} catch (ParseException e) {
			throw new JsonDeserializationException("Invalid JSON in request body.", e);
		}
	}
	
	protected JSONArray serizalizeTrack(TrackedRace trackedRace) {
		JsonSerializer<RaceLogEvent> serializer = createSerializer();
		JSONArray result = new JSONArray();
		
		RaceLog log = trackedRace.getRaceLog();
		log.lockForRead();
		for (RaceLogEvent event :  log.getFixes()) {
				result.add(serializer.serialize(event));
		}
		log.unlockAfterRead();
		
		return result;
	}

	private static JsonSerializer<RaceLogEvent> createSerializer() {
		JsonSerializer<Competitor> competitorSerializer = new CompetitorIdJsonSerializer();
		return new RaceLogEventSerializer(
				new RaceLogFlagEventSerializer(competitorSerializer),
				new RaceLogStartTimeEventSerializer(competitorSerializer),
				new RaceLogCourseAreaChangedEventSerializer(competitorSerializer));
	}

}
