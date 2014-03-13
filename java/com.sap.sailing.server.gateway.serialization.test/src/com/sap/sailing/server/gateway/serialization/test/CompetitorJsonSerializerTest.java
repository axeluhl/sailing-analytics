package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Team;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;

public class CompetitorJsonSerializerTest {

    private Competitor competitor;
    private CompetitorJsonSerializer serializer;
    private CompetitorJsonDeserializer deserializer;

    @Before
    public void setUp() {
        competitor = mock(Competitor.class);
        when(competitor.getTeam()).thenReturn(mock(Team.class));
        serializer = new CompetitorJsonSerializer();
        deserializer = new CompetitorJsonDeserializer(DomainFactory.INSTANCE.getCompetitorStore());
    }

    /**
     * This test case fails based on bug 1599.
     */
    @Test
    public void testIntegerId() throws JsonDeserializationException, ParseException {
        Integer expectedId = Integer.valueOf(123);
        when(competitor.getId()).thenReturn(expectedId);

        JSONObject result = serializer.serialize(competitor);
        assertEquals(
                expectedId, 
                result.get(CompetitorJsonSerializer.FIELD_ID));
        Competitor deserializedCompetitor = deserializer.deserialize((JSONObject) new JSONParser().parse(result.toString()));
        assertEquals(expectedId, deserializedCompetitor.getId());
    }

    @Test
    public void testStringId() throws JsonDeserializationException, ParseException {
        String expectedId = "123";
        when(competitor.getId()).thenReturn(expectedId);

        JSONObject result = serializer.serialize(competitor);
        assertEquals(
                expectedId, 
                result.get(CompetitorJsonSerializer.FIELD_ID));
        Competitor deserializedCompetitor = deserializer.deserialize((JSONObject) new JSONParser().parse(result.toString()));
        assertEquals(expectedId, deserializedCompetitor.getId());
    }

    @Test
    public void testUUIDId() throws JsonDeserializationException, ParseException {
        UUID expectedId = UUID.randomUUID();
        when(competitor.getId()).thenReturn(expectedId);

        JSONObject result = serializer.serialize(competitor);
        Competitor deserializedCompetitor = deserializer.deserialize((JSONObject) new JSONParser().parse(result.toString()));
        assertEquals(expectedId, deserializedCompetitor.getId());
    }

    @Test
    public void testLongId() throws JsonDeserializationException, ParseException {
        Long expectedId = Long.valueOf(123);
        when(competitor.getId()).thenReturn(expectedId);

        JSONObject result = serializer.serialize(competitor);
        assertEquals(
                expectedId, 
                result.get(CompetitorJsonSerializer.FIELD_ID));
        Competitor deserializedCompetitor = deserializer.deserialize((JSONObject) new JSONParser().parse(result.toString()));
        assertEquals(expectedId, deserializedCompetitor.getId());
    }
}
