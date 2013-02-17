package com.sap.sailing.server.gateway.serialization.impl.racelog;

import com.sap.sailing.domain.racelog.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.domain.racelog.RaceLogPassChangeEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class RaceLogEventSerializerChooser implements RaceLogEventVisitor {

	private final JsonSerializer<RaceLogEvent> flagEventSerializer;
	private final JsonSerializer<RaceLogEvent> startTimeSerializer;
	private final JsonSerializer<RaceLogEvent> raceStatusSerializer;
	private final JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
	
	private JsonSerializer<RaceLogEvent> chosenSerializer;
	
	public RaceLogEventSerializerChooser(
			JsonSerializer<RaceLogEvent> flagEventSerializer,
			JsonSerializer<RaceLogEvent> startTimeSerializer,
			JsonSerializer<RaceLogEvent> raceStatusSerializer,
			JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer) {
		this.flagEventSerializer = flagEventSerializer;
		this.startTimeSerializer = startTimeSerializer;
		this.raceStatusSerializer = raceStatusSerializer;
		this.courseAreaChangedEventSerializer = courseAreaChangedEventSerializer;
		
		this.chosenSerializer = null;
	}
	
	public JsonSerializer<RaceLogEvent> getSerializer(RaceLogEvent event) {
		chosenSerializer = null;
		event.accept(this);
		if (chosenSerializer == null) {
			throw new UnsupportedOperationException(
					String.format("There is no serializer for event type %s", 
							event.getClass().getName()));
		}
		return chosenSerializer;
	}
	
	@Override
	public void visit(RaceLogFlagEvent event) {
		chosenSerializer = flagEventSerializer;
	}

	@Override
	public void visit(RaceLogPassChangeEvent event) {
		chosenSerializer = null;
	}

	@Override
	public void visit(RaceLogRaceStatusEvent event) {
		chosenSerializer = raceStatusSerializer;
	}

	@Override
	public void visit(RaceLogStartTimeEvent event) {
		chosenSerializer = startTimeSerializer;
	}

	@Override
	public void visit(RaceLogCourseAreaChangedEvent event) {
		chosenSerializer = courseAreaChangedEventSerializer;
	}

}
