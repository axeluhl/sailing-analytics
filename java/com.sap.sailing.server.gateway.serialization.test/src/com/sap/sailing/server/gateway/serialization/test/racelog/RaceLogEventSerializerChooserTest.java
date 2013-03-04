package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.racelog.RaceLogEventSerializerChooser;

public class RaceLogEventSerializerChooserTest {
    
    private RaceLogEventSerializerChooser chooser;
    
    private JsonSerializer<RaceLogEvent> flagEventSerializer;
    private JsonSerializer<RaceLogEvent> startTimeSerializer;
    private JsonSerializer<RaceLogEvent> raceStatusSerializer;
    private JsonSerializer<RaceLogEvent> courseAreaChangedEventSerializer;
    private JsonSerializer<RaceLogEvent> passChangedSerializer;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        flagEventSerializer = mock(JsonSerializer.class);
        startTimeSerializer = mock(JsonSerializer.class);
        raceStatusSerializer = mock(JsonSerializer.class);
        courseAreaChangedEventSerializer = mock(JsonSerializer.class);
        passChangedSerializer = mock(JsonSerializer.class);
        
        chooser = new RaceLogEventSerializerChooser(
                flagEventSerializer, 
                startTimeSerializer, 
                raceStatusSerializer, 
                courseAreaChangedEventSerializer);
    }
    
    @Test
    public void testChoosesFlagEventSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(null, 0, null, null, false);
        assertEquals(flagEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesStartTimeSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(null, 0, null);
        assertEquals(startTimeSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesRaceStatusSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(null, 0, RaceLogRaceStatus.SCHEDULED);
        assertEquals(raceStatusSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesCourseAreaChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createRaceLogCourseAreaChangedEvent(null, 0, null);
        assertEquals(courseAreaChangedEventSerializer, chooser.getSerializer(event));
    }
    
    @Test
    public void testChoosesPassChangedSerializer() {
        // we use the real event type here because we do not want to re-implement the dispatching.
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createRaceLogPassChangeEvent(null, 0);
        assertEquals(passChangedSerializer, chooser.getSerializer(event));
    }

}
