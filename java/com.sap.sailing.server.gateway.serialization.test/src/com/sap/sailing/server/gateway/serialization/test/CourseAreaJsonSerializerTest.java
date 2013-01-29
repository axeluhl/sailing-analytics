package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.server.gateway.serialization.impl.CourseAreaJsonSerializer;

public class CourseAreaJsonSerializerTest {
	
	protected CourseArea courseArea;
	protected CourseAreaJsonSerializer serializer;
	
	@Before
	public void setUp() {
		courseArea = mock(CourseArea.class);
		serializer = new CourseAreaJsonSerializer();
	}
	
	@Test
	public void testName() {
		String expectedName = "Cruiser";
		when(courseArea.getName()).thenReturn(expectedName);
		
		JSONObject result = serializer.serialize(courseArea);
		
		assertEquals(
				expectedName, 
				result.get(CourseAreaJsonSerializer.FIELD_NAME));
	}
	
	@Ignore
	@Test
	public void testRaces() {
		/// TODO: Implement a test for checking if races are correctly serialized on course areas
	}
}
