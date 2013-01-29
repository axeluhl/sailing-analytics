package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.racecommittee.Flags;
import com.sap.sailing.domain.racecommittee.RaceCommitteeEvent;
import com.sap.sailing.domain.racecommittee.RaceCommitteeFlagEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceCommitteeFlagEventSerializer;

public class RaceCommitteeFlagEventSerializerTest extends BaseRaceCommitteEventTest<RaceCommitteeFlagEvent> {
	
	
	private Flags expectedUpperFlag = Flags.FOXTROTT;
	private Flags expectedLowerFlag = Flags.FOXTROTT;
	private boolean expectedDisplayed = true;
	
	@Override
	protected RaceCommitteeFlagEvent createMockEvent() {
		RaceCommitteeFlagEvent event = mock(RaceCommitteeFlagEvent.class);

		when(event.getUpperFlag()).thenReturn(expectedUpperFlag);
		when(event.getLowerFlag()).thenReturn(expectedLowerFlag);
		when(event.isDisplayed()).thenReturn(expectedDisplayed);
		
		return event;
	}

	@Override
	protected JsonSerializer<RaceCommitteeEvent> createSerializer() {
		return new RaceCommitteeFlagEventSerializer();
	}
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	@Test
	public void testFlagStatusAttributes() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				expectedUpperFlag,
				json.get(RaceCommitteeFlagEventSerializer.FIELD_UPPER_FLAG));
		assertEquals(
				expectedLowerFlag,
				json.get(RaceCommitteeFlagEventSerializer.FIELD_LOWER_FLAG));
		assertEquals(
				expectedDisplayed,
				json.get(RaceCommitteeFlagEventSerializer.FIELD_DISPLAYED));
	}
	
	@Test
	public void testClassAttribute() {
		JSONObject json = serializer.serialize(event);
		
		assertEquals(
				RaceCommitteeFlagEventSerializer.VALUE_CLASS,
				json.get(RaceCommitteeFlagEventSerializer.FIELD_CLASS));
	}

}
