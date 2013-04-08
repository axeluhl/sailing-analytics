package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogFlagEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogFlagEventSerializer;

public class RaceLogFlagEventSerializerTest extends BaseRaceLogEventTest<RaceLogFlagEvent> {


    private Flags expectedUpperFlag = Flags.FOXTROTT;
    private Flags expectedLowerFlag = Flags.FOXTROTT;
    private boolean expectedDisplayed = true;

    @Override
    protected RaceLogFlagEvent createMockEvent() {
        RaceLogFlagEvent event = mock(RaceLogFlagEvent.class);

        when(event.getUpperFlag()).thenReturn(expectedUpperFlag);
        when(event.getLowerFlag()).thenReturn(expectedLowerFlag);
        when(event.isDisplayed()).thenReturn(expectedDisplayed);

        return event;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected JsonSerializer<RaceLogEvent> createSerializer() {
        return new RaceLogFlagEventSerializer(mock(JsonSerializer.class));
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
                json.get(RaceLogFlagEventSerializer.FIELD_UPPER_FLAG));
        assertEquals(
                expectedLowerFlag,
                json.get(RaceLogFlagEventSerializer.FIELD_LOWER_FLAG));
        assertEquals(
                expectedDisplayed,
                json.get(RaceLogFlagEventSerializer.FIELD_DISPLAYED));
    }

    @Test
    public void testClassAttribute() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                RaceLogFlagEventSerializer.VALUE_CLASS,
                json.get(RaceLogFlagEventSerializer.FIELD_CLASS));
    }

}
