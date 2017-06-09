package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.race.RaceLogCourseAreaChangedEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseAreaChangedEventSerializer;

public class CourseAreaChangedEventSerializerTest extends AbstractEventSerializerTest<RaceLogCourseAreaChangedEvent> {

    private Serializable expectedCourseAreaId;
    
    @Before
    public void setUp() {
        expectedCourseAreaId = UUID.randomUUID();
        super.setUp();
    }
    
    @Override
    protected RaceLogCourseAreaChangedEvent createMockEvent() {
        RaceLogCourseAreaChangedEvent event = mock(RaceLogCourseAreaChangedEvent.class);
        when(event.getCourseAreaId()).thenReturn(expectedCourseAreaId);
        return event;
    }

    @Override
    protected JsonSerializer<RaceLogEvent> createSerializer(JsonSerializer<Competitor> competitorSerializer) {
        return new RaceLogCourseAreaChangedEventSerializer(competitorSerializer);
    }
    
    @Test
    public void testCourseAreaId() {
        
        JSONObject result = serializer.serialize(event);
        
        assertEquals(
                expectedCourseAreaId.toString(), 
                result.get(RaceLogCourseAreaChangedEventSerializer.FIELD_COURSE_AREA_ID).toString());
        
    }

}
