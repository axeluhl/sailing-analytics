package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.domain.abstractlog.race.impl.CompetitorResultsImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEventAuthorImpl;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogFinishPositioningConfirmedEventDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;
import com.sap.sse.common.Util;

public class RaceLogFinishPositioningConfirmedEventSerializerTest {

    private RaceLogFinishPositioningConfirmedEventSerializer serializer;
    private RaceLogFinishPositioningConfirmedEventDeserializer deserializer;
    private RaceLogFinishPositioningConfirmedEvent event;
    private TimePoint now;
    private CompetitorResults positioningList;
    private RaceLogEventAuthor author = new RaceLogEventAuthorImpl("Test Author", 1);
    
    @Before
    public void setUp() {
        SharedDomainFactory factory = DomainFactory.INSTANCE;
        serializer = new RaceLogFinishPositioningConfirmedEventSerializer(new CompetitorJsonSerializer(
                new TeamJsonSerializer(new PersonJsonSerializer(new NationalityJsonSerializer())), null));
        deserializer = new RaceLogFinishPositioningConfirmedEventDeserializer(new CompetitorJsonDeserializer(factory.getCompetitorStore(), null, /* boatDeserializer */ null));

        now = MillisecondsTimePoint.now();
        positioningList = new CompetitorResultsImpl();
    }

    @Test
    public void testSerializeAndDeserializeRaceLogFinishPositioningConfirmedEvent() throws JsonDeserializationException {
        positioningList.add(new com.sap.sse.common.Util.Triple<Serializable, String, MaxPointsReason>(UUID.randomUUID(), "SAP Extreme",
                MaxPointsReason.NONE));
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, author, 0, positioningList);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getLogicalTimePoint(), deserializedEvent.getLogicalTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        assertNotNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertNotNull(deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertEquals(1, event.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
        assertEquals(1, deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
        assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getA(), deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()
                .get(0).getA());
        assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getB(), deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()
                .get(0).getB());
        assertEquals(event.getPositionedCompetitorsIDsNamesMaxPointsReasons().get(0).getC(), deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons()
                .get(0).getC());
    }

    @Test
    public void testSerializeAndDeserializeRaceLogFinishPositioningConfirmedEventWithoutPositioningBackwardsCompatible()
            throws JsonDeserializationException {
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, author, 0, null);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getLogicalTimePoint(), deserializedEvent.getLogicalTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        assertNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertNotNull(deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertEquals(0, deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
    }

    @Test
    public void testSerializeAndDeserializeRaceLogFinishPositioningConfirmedEventWithEmptyPositioning()
            throws JsonDeserializationException {
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, author, 0, positioningList);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getLogicalTimePoint(), deserializedEvent.getLogicalTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        assertNotNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertNotNull(deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertEquals(0, event.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
        assertEquals(0, deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
    }

}
