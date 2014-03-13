package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.impl.RaceLogFlagEventImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.BaseRaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
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
    
    @Test
    public void testAuthor() throws JsonDeserializationException {
        JSONObject json = RaceLogEventSerializer.create(new CompetitorJsonSerializer()).serialize(
                new RaceLogFlagEventImpl(MillisecondsTimePoint.now(), author, MillisecondsTimePoint.now(), "pid", /* pInvolvedBoats */ Collections.<Competitor>emptyList(),
                /* pPassId */ 1, Flags.NOVEMBER, Flags.NONE, /* pIsDisplayed */ true));
        RaceLogEventDeserializer deserializer = RaceLogEventDeserializer.create(DomainFactory.INSTANCE);
        RaceLogEvent deserializedEvent = deserializer.deserialize(json);
        final RaceLogEventAuthor myAuthor = deserializedEvent.getAuthor();
        assertNotNull(myAuthor);
        assertEquals(author.getName(), myAuthor.getName());
        assertEquals(author.getPriority(), myAuthor.getPriority());
    }

}
