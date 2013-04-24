package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.RaceLogEventSerializerChooser;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogEventSerializerChooserImpl;

public class RaceLogEventSerializerChooserTest {
    
    private RaceLogEventSerializerChooser chooser;
    
    private JsonSerializer<RaceLogEvent> flagEventSerializer;
    private JsonSerializer<RaceLogEvent> startTimeSerializer;
    private JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> passChangedSerializer;
    private JsonSerializer<RaceLogEvent> courseDesignChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> finishPositioningListChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> finishPositioningConfirmedEventSerializer;
    private JsonSerializer<RaceLogEvent> pathfinderEventSerializer;
    private JsonSerializer<RaceLogEvent> gateLineOpeningTimeEventSerializer;
    private JsonSerializer<RaceLogEvent> startProcedureTypeChangedEventSerializer;
    
    private RaceLogEventFactory factory;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        flagEventSerializer = mock(JsonSerializer.class);
        startTimeSerializer = mock(JsonSerializer.class);
        raceStatusSerializer = mock(JsonSerializer.class);
        courseAreaChangedEventSerializer = mock(JsonSerializer.class);
        passChangedSerializer = mock(JsonSerializer.class);
        courseDesignChangedEventSerializer = mock(JsonSerializer.class);
        finishPositioningListChangedEventSerializer = mock(JsonSerializer.class);
        finishPositioningConfirmedEventSerializer = mock(JsonSerializer.class);
        pathfinderEventSerializer = mock(JsonSerializer.class);
        gateLineOpeningTimeEventSerializer = mock(JsonSerializer.class);
        startProcedureTypeChangedEventSerializer = mock(JsonSerializer.class);
        
        chooser = new RaceLogEventSerializerChooserImpl(
                flagEventSerializer, 
                startTimeSerializer, 
                raceStatusSerializer, 
                courseAreaChangedEventSerializer,
                passChangedSerializer,
                courseDesignChangedEventSerializer,
                finishPositioningListChangedEventSerializer,
                finishPositioningConfirmedEventSerializer,
                pathfinderEventSerializer, gateLineOpeningTimeEventSerializer,
                startProcedureTypeChangedEventSerializer);
        
        factory = RaceLogEventFactory.INSTANCE;
    }
    
    @Test
    public void testChoosesFlagEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFlagEvent(null, 0, null, null, false);
        assertEquals(flagEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartTimeEvent(null, 0, null);
        assertEquals(startTimeSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesRaceStatusSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createRaceStatusEvent(null, 0, RaceLogRaceStatus.SCHEDULED);
        assertEquals(raceStatusSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesCourseAreaChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseAreaChangedEvent(null, 0, null);
        assertEquals(courseAreaChangedEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesPassChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPassChangeEvent(null, 0);
        assertEquals(passChangedSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesCourseDesignChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createCourseDesignChangedEvent(null, 0, null);
        assertEquals(courseDesignChangedEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesFinishPositioningListChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningListChangedEvent(null, 0, null);
        assertEquals(finishPositioningListChangedEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesFinishPositioningConfirmedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createFinishPositioningConfirmedEvent(null, 0);
        assertEquals(finishPositioningConfirmedEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesPathfinderSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createPathfinderEvent(null, 0, "GER 20");
        assertEquals(pathfinderEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesGateLineOpeningTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createGateLineOpeningTimeEvent(null, 0, new Long(0));
        assertEquals(gateLineOpeningTimeEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testStartProcedureTypeChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = factory.createStartProcedureChangedEvent(null, 0, StartProcedureType.GateStart);
        assertEquals(startProcedureTypeChangedEventSerializer, chooser.getSerializer(event));
    }

}
