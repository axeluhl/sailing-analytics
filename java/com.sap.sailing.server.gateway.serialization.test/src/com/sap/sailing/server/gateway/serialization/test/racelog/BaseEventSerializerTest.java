package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFlagEventSerializer;

public class BaseEventSerializerTest extends AbstractEventSerializerTest<RaceLogEvent> {

    protected final String expectedClassValue = "AllYourBaseAreBelongToUs";

    private class TestSerializer extends BaseRaceLogEventSerializer {
        public TestSerializer(JsonSerializer<Competitor> competitorSerializer) {
            super(competitorSerializer);
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
    protected JsonSerializer<RaceLogEvent> createSerializer(JsonSerializer<Competitor> competitorSerializer) {
        return new TestSerializer(competitorSerializer);
    }

    @Test
    public void testClassAttribute() {
        JSONObject json = serializer.serialize(event);

        assertEquals(
                expectedClassValue,
                json.get(RaceLogFlagEventSerializer.FIELD_CLASS));
    }

}
