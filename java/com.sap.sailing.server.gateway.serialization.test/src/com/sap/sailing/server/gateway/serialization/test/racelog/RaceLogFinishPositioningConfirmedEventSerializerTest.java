package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogFinishPositioningConfirmedEvent;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogFinishPositioningConfirmedEventDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.NationalityJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.PersonJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.TeamJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogFinishPositioningConfirmedEventSerializer;

public class RaceLogFinishPositioningConfirmedEventSerializerTest {

    private RaceLogFinishPositioningConfirmedEventSerializer serializer;
    private RaceLogFinishPositioningConfirmedEventDeserializer deserializer;
    private RaceLogFinishPositioningConfirmedEvent event;
    private TimePoint now;
    private List<Triple<Serializable, String, MaxPointsReason>> positioningList;

    @Before
    public void setUp() {
        SharedDomainFactory factory = DomainFactory.INSTANCE;
        serializer = new RaceLogFinishPositioningConfirmedEventSerializer(new CompetitorJsonSerializer(
                new TeamJsonSerializer(new PersonJsonSerializer(new NationalityJsonSerializer())), null));
        deserializer = new RaceLogFinishPositioningConfirmedEventDeserializer(new CompetitorJsonDeserializer(factory, null));

        now = MillisecondsTimePoint.now();
        positioningList = new ArrayList<Triple<Serializable, String, MaxPointsReason>>();
    }

    @Test
    public void testSerializeAndDeserializeRaceLogFinishPositioningConfirmedEvent() throws JsonDeserializationException {
        positioningList.add(new Triple<Serializable, String, MaxPointsReason>(UUID.randomUUID(), "SAP Extreme",
                MaxPointsReason.NONE));
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, 0, positioningList);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getTimePoint(), deserializedEvent.getTimePoint());
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
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, 0, null);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getTimePoint(), deserializedEvent.getTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        assertNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertNotNull(deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertEquals(0, deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
    }

    @Test
    public void testSerializeAndDeserializeRaceLogFinishPositioningConfirmedEventWithEmptyPositioning()
            throws JsonDeserializationException {
        event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(now, 0, positioningList);
        JSONObject jsonConfirmationEvent = serializer.serialize(event);
        RaceLogFinishPositioningConfirmedEvent deserializedEvent = (RaceLogFinishPositioningConfirmedEvent) deserializer
                .deserialize(jsonConfirmationEvent);

        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getTimePoint(), deserializedEvent.getTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        assertNotNull(event.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertNotNull(deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons());
        assertEquals(0, event.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
        assertEquals(0, deserializedEvent.getPositionedCompetitorsIDsNamesMaxPointsReasons().size());
    }

}
