package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeStartTimeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeStartTimeEventSerializer;

public class RaceCommitteeStartTimeEventSerializerTest extends BaseRaceCommitteEventTest<RaceCommitteeStartTimeEvent> {
	
	private final long expectedStartTimeTimestamp = 2013;
	
	@Override
	protected RaceCommitteeStartTimeEvent createMockEvent() {
		
		TimePoint startTime = mock(TimePoint.class);
		when(startTime.asMillis()).thenReturn(expectedStartTimeTimestamp);
		
		RaceCommitteeStartTimeEvent event = mock(RaceCommitteeStartTimeEvent.class);
		when(event.getStartTime()).thenReturn(startTime);
		
		return event;
	}

	@Override
	protected JsonSerializer<RaceCommitteeEvent> createSerializer() {
		return new RaceCommitteeStartTimeEventSerializer();
	}
	
	@Test
	public void testStartTimeAttributes() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				expectedStartTimeTimestamp,
				json.get(RaceCommitteeStartTimeEventSerializer.FIELD_START_TIME));
	}
	
	@Test
	public void testClassAttribute() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				RaceCommitteeStartTimeEventSerializer.VALUE_CLASS,
				json.get(RaceCommitteeStartTimeEventSerializer.FIELD_CLASS));
	}

}
