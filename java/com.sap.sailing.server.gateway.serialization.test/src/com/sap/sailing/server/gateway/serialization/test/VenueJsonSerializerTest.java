package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.VenueJsonSerializer;

public class VenueJsonSerializerTest {
    
    private JsonSerializer<CourseArea> courseAreaSerializer;
    private VenueJsonSerializer serializer;
    
    private Venue venue;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        courseAreaSerializer = mock(JsonSerializer.class);
        serializer = new VenueJsonSerializer(courseAreaSerializer);
        
        venue = mock(Venue.class);
        
        when(venue.getCourseAreas()).thenReturn(Collections.<CourseArea>emptyList());
    }

    @Test
    public void testName() {
        when(venue.getName()).thenReturn("NAME");
        
        JSONObject result = serializer.serialize(venue);

        assertEquals("NAME", result.get(VenueJsonSerializer.FIELD_NAME));
    }
    
    @Test
    public void testSerializesCourseAreas() {
        // 1. Set up some course areas to be serialized...
        Map<CourseArea, JSONObject> expectedCourseAreas = new HashMap<CourseArea, JSONObject>();
        expectedCourseAreas.put(mock(CourseArea.class), new JSONObject());
        expectedCourseAreas.put(mock(CourseArea.class), new JSONObject());
        // 2. Let return them when needed...
        for (Map.Entry<CourseArea, JSONObject> entry : expectedCourseAreas.entrySet()) {
            when(courseAreaSerializer.serialize(entry.getKey())).thenReturn(entry.getValue());
        }
        when(venue.getCourseAreas()).thenReturn(expectedCourseAreas.keySet());
        
        // 3. Serialize the venue!
        JSONObject result = serializer.serialize(venue);
        
        // 4. Verify results are stored in JSON...
        JSONArray actualCourseAreas = (JSONArray) result.get(VenueJsonSerializer.FIELD_COURSE_AREAS);
        assertEquals(expectedCourseAreas.size(), actualCourseAreas.size());
        for (JSONObject courseArea : expectedCourseAreas.values()) {
            assertThat(actualCourseAreas, CoreMatchers.hasItem((Object)courseArea));
        }
    }
}
