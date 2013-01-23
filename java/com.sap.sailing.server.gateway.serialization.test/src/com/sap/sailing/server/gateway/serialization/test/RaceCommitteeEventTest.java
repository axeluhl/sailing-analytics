package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeEventSerializer;
import com.sap.sailing.server.gateway.serialization.RaceCommitteeFlagEventSerializer;

public class RaceCommitteeEventTest extends BaseRaceCommitteEventTest<RaceCommitteeEvent> {

	protected final String expectedClassValue = "AllYourBaseAreBelongToUs";
	
	private class TestRaceCommitteeEventSerializer extends RaceCommitteeEventSerializer {
		@Override
		protected String getClassFieldValue() {
			return expectedClassValue;
		}
	}
	
	@Override
	protected RaceCommitteeEvent createMockEvent() {
		return mock(RaceCommitteeEvent.class);
	}

	@Override
	protected JsonSerializer<RaceCommitteeEvent> createSerializer() {
		return new TestRaceCommitteeEventSerializer();
	}
	
	@Test
	public void testClassAttribute() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				expectedClassValue,
				json.get(RaceCommitteeFlagEventSerializer.FIELD_CLASS));
	}

}
