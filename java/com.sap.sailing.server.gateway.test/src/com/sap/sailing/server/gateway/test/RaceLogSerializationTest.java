package com.sap.sailing.server.gateway.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDeviceCompetitorMappingEvent;
import com.sap.sailing.domain.abstractlog.shared.events.DeviceCompetitorMappingEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.BoatImpl;
import com.sap.sailing.domain.base.impl.CompetitorImpl;
import com.sap.sailing.domain.base.impl.NationalityImpl;
import com.sap.sailing.domain.base.impl.PersonImpl;
import com.sap.sailing.domain.base.impl.TeamImpl;
import com.sap.sailing.domain.racelog.tracking.test.mock.MockEmptyServiceFinder;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiIdentifier;
import com.sap.sailing.domain.racelog.tracking.test.mock.SmartphoneImeiJsonHandler;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.PlaceHolderDeviceIdentifier;
import com.sap.sailing.domain.trackfiles.TrackFileImportDeviceIdentifierImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.DeviceIdentifierJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceIdentifierJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.DeviceIdentifierJsonHandler;
import com.sap.sailing.server.gateway.serialization.racelog.tracking.impl.PlaceHolderDeviceIdentifierJsonHandler;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinder;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceLogSerializationTest {
    private JsonSerializer<RaceLogEvent> serializer;
    private JsonDeserializer<RaceLogEvent> deserializer;
    private final AbstractLogEventAuthor author = new LogEventAuthorImpl("test", 0);
    private static final Competitor competitor = new CompetitorImpl("a", "b", null,
            null, null,
            new TeamImpl("a", Collections.singletonList(new PersonImpl("a", new NationalityImpl("GER"), new Date(), "abc")), null), new BoatImpl("a", new BoatClassImpl("a", true), "abc"));
    
    @Before
    public void setup() {
    }
    
    private void setup(JsonDeserializer<DeviceIdentifier> deviceDeserializer, JsonSerializer<DeviceIdentifier> deviceSerializer) {
        JsonSerializer<Competitor> cSer = CompetitorJsonSerializer.create();
        serializer = RaceLogEventSerializer.create(cSer, deviceSerializer);
        deserializer = RaceLogEventDeserializer.create(DomainFactory.INSTANCE, deviceDeserializer);
    }
    
    private TimePoint t() {
        return MillisecondsTimePoint.now();
    }
    
    @Test
    public void testSerializationOfDeviceIdentifier() throws JsonDeserializationException {
        setup(DeviceIdentifierJsonDeserializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE),
                DeviceIdentifierJsonSerializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE));
        DeviceIdentifier device = new SmartphoneImeiIdentifier("abc");
        RaceLogDeviceCompetitorMappingEvent event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(
                t(), author, device, competitor, 0, t(), t());
        
        JSONObject json = serializer.serialize(event);
        
        RaceLogEvent deserialized = deserializer.deserialize(json);
        
        assertTrue(deserialized instanceof DeviceCompetitorMappingEvent);
        RaceLogDeviceCompetitorMappingEvent deserializedMapping = (RaceLogDeviceCompetitorMappingEvent) deserialized;
        assertEquals(event.getDevice(), deserializedMapping.getDevice());
    }
    
    @Test
    public void testSerializationOfDeviceIdentifierWithNoSerializer() throws JsonDeserializationException {
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> onlyFallback = new MockEmptyServiceFinder<>();
        onlyFallback.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
        
        setup(new DeviceIdentifierJsonDeserializer(onlyFallback),
                DeviceIdentifierJsonSerializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE));
        DeviceIdentifier device = new SmartphoneImeiIdentifier("abc");
        RaceLogDeviceCompetitorMappingEvent event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(
                t(), author, device, competitor, 0, t(), t());
        
        JSONObject json = serializer.serialize(event);
        
        RaceLogEvent deserialized = deserializer.deserialize(json);
        
        assertTrue(deserialized instanceof DeviceCompetitorMappingEvent);
        RaceLogDeviceCompetitorMappingEvent deserializedMapping = (RaceLogDeviceCompetitorMappingEvent) deserialized;
        assertTrue(deserializedMapping.getDevice() instanceof PlaceHolderDeviceIdentifier);
        assertEquals(event.getDevice().getStringRepresentation(), deserializedMapping.getDevice().getStringRepresentation());
        assertEquals(event.getDevice().getIdentifierType(), deserializedMapping.getDevice().getIdentifierType());
    }
    
    @Test
    public void testSerializationOfDeviceIdentifierWithNoDeserializer() throws JsonDeserializationException {
        TypeBasedServiceFinder<DeviceIdentifierJsonHandler> onlyFallback = new MockEmptyServiceFinder<>();
        onlyFallback.setFallbackService(new PlaceHolderDeviceIdentifierJsonHandler());
        
        setup(DeviceIdentifierJsonDeserializer.create(new SmartphoneImeiJsonHandler(), SmartphoneImeiIdentifier.TYPE),
                new DeviceIdentifierJsonSerializer(onlyFallback));
        //track file device id can't be restored from string rep
        DeviceIdentifier device = new TrackFileImportDeviceIdentifierImpl("file", "track");
        RaceLogDeviceCompetitorMappingEvent event = RaceLogEventFactory.INSTANCE.createDeviceCompetitorMappingEvent(
                t(), author, device, competitor, 0, t(), t());
        
        JSONObject json = serializer.serialize(event);
        
        RaceLogEvent deserialized = deserializer.deserialize(json);
        
        assertTrue(deserialized instanceof RaceLogDeviceCompetitorMappingEvent);
        RaceLogDeviceCompetitorMappingEvent deserializedMapping = (RaceLogDeviceCompetitorMappingEvent) deserialized;
        assertTrue(deserializedMapping.getDevice() instanceof PlaceHolderDeviceIdentifier);
        assertEquals(event.getDevice().getStringRepresentation(), deserializedMapping.getDevice().getStringRepresentation());
        assertEquals(event.getDevice().getIdentifierType(), deserializedMapping.getDevice().getIdentifierType());
    }
}
