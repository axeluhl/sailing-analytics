package com.sap.sailing.server.gateway.serialization.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.RaceDefinitionImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.RaceDefinitionJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.WaypointJsonSerializer;

/// TODO: Move these tests somewhere else...
public class EverythingTest {
	
	@Test
	public void testEverything() {
		
		ControlPoint startAndFinishControlPoint = 
				new MarkImpl("FirstMark", MarkType.BUOY, "RED", "TRIANGLE", "DASHED");
		Mark leftGateMark = new MarkImpl("LeftGateMark", MarkType.BUOY, "BLUE", "TRIANGLE", "DASHED");;
		Mark rightGateMark = new MarkImpl("RightGateMark", MarkType.BUOY, "GREEN", "TRIANGLE", "DASHED");;
		ControlPoint gate = new GateImpl(leftGateMark, rightGateMark, "Gate");
		
		Waypoint firstWaypoint = new WaypointImpl(startAndFinishControlPoint, NauticalSide.PORT);
		Waypoint secondWaypoint = new WaypointImpl(gate, NauticalSide.PORT);
		Waypoint thirdWaypoint = new WaypointImpl(startAndFinishControlPoint, NauticalSide.STARBOARD);
		
		Course course = new CourseImpl(
				"Rainbow Route", 
				Arrays.asList(firstWaypoint, secondWaypoint, thirdWaypoint));
		
		BoatClass boatClass = new BoatClassImpl("Laser", false);
		RaceDefinition race = new RaceDefinitionImpl(
				"Race 1", 
				course, 
				boatClass, 
				Collections.<Competitor>emptyList(), 
				UUID.randomUUID());
		
		JsonSerializer<ControlPoint> markSerializer = new MarkJsonSerializer();
		JsonSerializer<RaceDefinition> serializer = 
				new RaceDefinitionJsonSerializer(new CourseJsonSerializer(
					new WaypointJsonSerializer(
							new ControlPointJsonSerializer(
									markSerializer, 
									new GateJsonSerializer(markSerializer)))));
		
		JSONObject result = serializer.serialize(race);
		
		System.out.println(result.toJSONString());
		assertTrue(result.containsKey(RaceDefinitionJsonSerializer.FIELD_NAME));
		
	}

}
