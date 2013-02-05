package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;

public class RaceLogEventTest extends BaseRaceLogEventTest<RaceLogEvent> {

	protected final String expectedClassValue = "AllYourBaseAreBelongToUs";
	
	private class TestRaceCommitteeEventSerializer extends BaseRaceLogEventSerializer {
		@SuppressWarnings("unchecked")
		public TestRaceCommitteeEventSerializer() {
			super(mock(JsonSerializer.class));
		}

		@Override
		protected String getClassFieldValue() {
			return expectedClassValue;
		}
	}
	
	@Override
	protected RaceLogEvent createMockEvent() {
		return mock(RaceLogEvent.class);
	}

	@Override
	protected JsonSerializer<RaceLogEvent> createSerializer() {
		return new TestRaceCommitteeEventSerializer();
	}
	
	@Test
	public void testClassAttribute() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				expectedClassValue,
				json.get(RaceLogFlagEventSerializer.FIELD_CLASS));
	}

}
