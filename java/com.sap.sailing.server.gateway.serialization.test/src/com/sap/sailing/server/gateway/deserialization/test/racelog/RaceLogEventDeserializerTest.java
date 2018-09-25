package com.sap.sailing.server.gateway.deserialization.test.racelog;

import static org.junit.Assert.assertEquals;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogTagEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogUseCompetitorsFromRaceLogEventImpl;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.impl.DynamicCompetitor;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.CompetitorJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEndOfTrackingEventDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogEventDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogFlagEventDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogTagEventDeserializer;
import com.sap.sailing.server.gateway.deserialization.racelog.impl.RaceLogUseCompetitorsFromRaceLogEventDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializer;
import com.sap.sse.common.TimePoint;

public class RaceLogEventDeserializerTest {
    
    JsonDeserializer<DynamicCompetitor> competitorDeserializer = CompetitorJsonDeserializer.create(DomainFactory.INSTANCE);
    
    protected RaceLogFlagEventDeserializer mockitoRaceLogFlagEventDeserializer = Mockito.spy(new RaceLogFlagEventDeserializer(competitorDeserializer));
    protected RaceLogUseCompetitorsFromRaceLogEventDeserializer mockitoRaceLogUseCompetitorsFromRaceLogEventDeserializer = Mockito.spy(new RaceLogUseCompetitorsFromRaceLogEventDeserializer(competitorDeserializer));
    protected RaceLogEndOfTrackingEventDeserializer mockitoRaceLogEndOfTrackingEventDeserializer = Mockito.spy(new RaceLogEndOfTrackingEventDeserializer(competitorDeserializer));
    protected RaceLogTagEventDeserializer mockitoRaceLogTagEventDeserializer = Mockito.spy(new RaceLogTagEventDeserializer(competitorDeserializer));
    
    public class InnerRaceLogEventDeserializer extends RaceLogEventDeserializer{
        
        
        public InnerRaceLogEventDeserializer() {
            super(mockitoRaceLogFlagEventDeserializer, null, null, null, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    mockitoRaceLogUseCompetitorsFromRaceLogEventDeserializer,
                    mockitoRaceLogEndOfTrackingEventDeserializer,mockitoRaceLogTagEventDeserializer);
        }
    }
    
    //Used for creating RaceLogEvents which get serialized, then deserialized and afterwards compared
    private AbstractLogEventAuthor author = new LogEventAuthorImpl("Test Author", 1);
    private TimePoint timePoint = TimePoint.BeginningOfTime;
    private TimePoint timePoint2 = TimePoint.EndOfTime;
    private InnerRaceLogEventDeserializer deserializer = new InnerRaceLogEventDeserializer();

    
    @Test
    public void testSerializationAndDeserializationForRaceLogFlagEvent() throws Exception{
        RaceLogFlagEventImpl originalEvent = new RaceLogFlagEventImpl(timePoint, author, 0, Flags.BLACK, Flags.ESSONE, false);
        RaceLogEventSerializer serializer = (RaceLogEventSerializer) RaceLogEventSerializer.create(CompetitorJsonSerializer.create());
        JSONObject object = serializer.serialize(originalEvent); 
        
        RaceLogEvent raceLogEvent = deserializer.deserialize(object);
        
        //assert correct deserializer was used
        Mockito.verify(mockitoRaceLogFlagEventDeserializer).deserialize(object);
        
        RaceLogFlagEventImpl newEvent = (RaceLogFlagEventImpl) raceLogEvent;
        
        //assert raceLogEvent has correct values
        assertEquals(originalEvent.getTimePoint().toString(), newEvent.getTimePoint().toString());
        assertEquals(originalEvent.getAuthor().toString(), newEvent.getAuthor().toString());
        assertEquals(originalEvent.getPassId(), newEvent.getPassId());
        assertEquals(originalEvent.getClass(), newEvent.getClass());
        assertEquals(originalEvent.getId(), newEvent.getId());
        assertEquals(originalEvent.getShortInfo(), newEvent.getShortInfo());
        
        assertEquals(originalEvent.getLowerFlag(), newEvent.getLowerFlag());
        assertEquals(originalEvent.getUpperFlag(), newEvent.getUpperFlag());
        assertEquals(originalEvent.getCreatedAt(), newEvent.getCreatedAt());
    }
    
    @Test
    public void testSerializationAndDeserializationForRaceLogEndOfTrackingEvent() throws Exception{
        RaceLogEndOfTrackingEventImpl originalEvent = new  RaceLogEndOfTrackingEventImpl(timePoint, timePoint2, author, UUID.randomUUID(), 3);
        RaceLogEventSerializer serializer = (RaceLogEventSerializer) RaceLogEventSerializer.create(CompetitorJsonSerializer.create());
        JSONObject object = serializer.serialize(originalEvent); 
        
        RaceLogEvent raceLogEvent = deserializer.deserialize(object);
        
        //assert correct deserializer was used
        Mockito.verify(mockitoRaceLogEndOfTrackingEventDeserializer).deserialize(object);
        
        RaceLogEndOfTrackingEventImpl newEvent = (RaceLogEndOfTrackingEventImpl) raceLogEvent;
        
        //assert raceLogEvent has correct value
        assertEquals(originalEvent.getTimePoint().toString(), newEvent.getTimePoint().toString());
        assertEquals(originalEvent.getAuthor().toString(), newEvent.getAuthor().toString());
        assertEquals(originalEvent.getPassId(), newEvent.getPassId());
        assertEquals(originalEvent.getClass(), newEvent.getClass());
        assertEquals(originalEvent.getId(), newEvent.getId());
        assertEquals(originalEvent.getShortInfo(), newEvent.getShortInfo());
        
        assertEquals(originalEvent.getCreatedAt(), newEvent.getCreatedAt());
    }   
    
    @Test
    public void testSerializationAndDeserializationForRaceLogUseCompetitorsFromRaceLogEvent() throws Exception{
        RaceLogUseCompetitorsFromRaceLogEventImpl originalEvent = new  RaceLogUseCompetitorsFromRaceLogEventImpl(timePoint, author, timePoint2, UUID.randomUUID(), 3);
        RaceLogEventSerializer serializer = (RaceLogEventSerializer) RaceLogEventSerializer.create(CompetitorJsonSerializer.create());
        JSONObject object = serializer.serialize(originalEvent); 
        
        RaceLogEvent raceLogEvent = deserializer.deserialize(object);
        
        //assert correct deserializer was used
        Mockito.verify(mockitoRaceLogUseCompetitorsFromRaceLogEventDeserializer).deserialize(object);
        
        RaceLogUseCompetitorsFromRaceLogEventImpl newEvent = (RaceLogUseCompetitorsFromRaceLogEventImpl) raceLogEvent;
        
        //assert raceLogEvent has correct value
        assertEquals(originalEvent.getTimePoint().toString(), newEvent.getTimePoint().toString());
        assertEquals(originalEvent.getAuthor().toString(), newEvent.getAuthor().toString());
        assertEquals(originalEvent.getPassId(), newEvent.getPassId());
        assertEquals(originalEvent.getClass(), newEvent.getClass());
        assertEquals(originalEvent.getId(), newEvent.getId());
        assertEquals(originalEvent.getShortInfo(), newEvent.getShortInfo());
        
        assertEquals(originalEvent.getCreatedAt(), newEvent.getCreatedAt());

    }  
    
    @Test
    public void testSerializationAndDeserializationForRaceLogTagEvent() throws Exception{
        RaceLogTagEventImpl originalEvent = new RaceLogTagEventImpl("tag", "comment", "a", "b", timePoint, timePoint2, author, UUID.randomUUID(), 3);
        RaceLogEventSerializer serializer = (RaceLogEventSerializer) RaceLogEventSerializer.create(CompetitorJsonSerializer.create());
        JSONObject object = serializer.serialize(originalEvent); 
        
        RaceLogEvent raceLogEvent = deserializer.deserialize(object);
        
        //assert correct deserializer was used
        Mockito.verify(mockitoRaceLogTagEventDeserializer).deserialize(object);
        
        RaceLogTagEventImpl newEvent = (RaceLogTagEventImpl) raceLogEvent;
        
        //assert raceLogEvent has correct value
        assertEquals(originalEvent.getTimePoint().toString(), newEvent.getTimePoint().toString());
        assertEquals(originalEvent.getAuthor().toString(), newEvent.getAuthor().toString());
        assertEquals(originalEvent.getPassId(), newEvent.getPassId());
        assertEquals(originalEvent.getClass(), newEvent.getClass());
        assertEquals(originalEvent.getId(), newEvent.getId());
        assertEquals(originalEvent.getShortInfo(), newEvent.getShortInfo());
        
        assertEquals(originalEvent.getCreatedAt(), newEvent.getCreatedAt());
        assertEquals(originalEvent.getTag(), newEvent.getTag());
        assertEquals(originalEvent.getComment(), newEvent.getComment());
        assertEquals(originalEvent.getImageURL(), newEvent.getImageURL());
        assertEquals(originalEvent.getResizedImageURL(), newEvent.getResizedImageURL());
        assertEquals(originalEvent.getUsername(), newEvent.getUsername());
    }   
}