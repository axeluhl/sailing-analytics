package com.sap.sailing.server.gateway.serialization.impl.racelog;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogEventSerializer implements JsonSerializer<RaceLogEvent> {

	private final JsonSerializer<RaceLogEvent> flagEventSerializer;
	private final JsonSerializer<RaceLogEvent> startTimeSerializer;
	private final JsonSerializer<RaceLogEvent> raceStatusSerializer;
	private final JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;

	public RaceLogEventSerializer(
			JsonSerializer<RaceLogEvent> flagEventSerializer,
			JsonSerializer<RaceLogEvent> startTimeSerializer,
			JsonSerializer<RaceLogEvent> raceStatusSerializer,
			JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer) {
		this.flagEventSerializer = flagEventSerializer;
		this.startTimeSerializer = startTimeSerializer;
		this.raceStatusSerializer = raceStatusSerializer;
		this.courseAreaChangedEventSerializer = courseAreaChangedEventSerializer;
	}

	protected JsonSerializer<RaceLogEvent> getSerializer(
			RaceLogEvent event) {
		if (event instanceof RaceLogFlagEvent) {
			return flagEventSerializer;
		} else if (event instanceof RaceLogStartTimeEvent) {
			return startTimeSerializer;
		} else if (event instanceof RaceLogRaceStatusEvent) {
			return raceStatusSerializer;
		} else if (event instanceof RaceLogCourseAreaChangedEvent) {
			return courseAreaChangedEventSerializer;
		}

		throw new UnsupportedOperationException(String.format(
				"There is no serializer defined for event type %s", event
						.getClass().getName()));
	}

	@Override
	public JSONObject serialize(RaceLogEvent object) {
		return getSerializer(object).serialize(object);
	}

}
