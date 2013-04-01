package com.sap.sailing.server.gateway.serialization.test.racelog;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import junit.framework.Assert;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.GateImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.ControlPointDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.CourseDataDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.GateDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.MarkDeserializer;
import com.sap.sailing.server.gateway.deserialization.coursedata.impl.WaypointDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.RaceLogCourseDesignChangedEventDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.ControlPointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.GateJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.MarkJsonSerializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.WaypointJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racegroup.impl.CompetitorJsonSerializer;
import com.sap.sailing.server.gateway.serialization.racelog.impl.RaceLogCourseDesignChangedEventSerializer;

public class RaceLogCourseDesignChangedEventSerializerTest {
    
    private RaceLogCourseDesignChangedEventSerializer serializer;
    private RaceLogCourseDesignChangedEventDeserializer deserializer;
    private RaceLogCourseDesignChangedEvent event;
    private TimePoint now;
    
    @Before
    public void setUp() {
        SharedDomainFactory factory = DomainFactory.INSTANCE;
        serializer = new RaceLogCourseDesignChangedEventSerializer(new CompetitorJsonSerializer(), 
                new CourseBaseJsonSerializer(new WaypointJsonSerializer(
                        new ControlPointJsonSerializer(
                                new MarkJsonSerializer(), 
                                new GateJsonSerializer(
                                        new MarkJsonSerializer())))));
        deserializer = new RaceLogCourseDesignChangedEventDeserializer(new CourseDataDeserializer(
                new WaypointDeserializer(
                        new ControlPointDeserializer(
                                new MarkDeserializer(factory), 
                                new GateDeserializer(factory, new MarkDeserializer(factory))))));
        now = MillisecondsTimePoint.now();
        
        event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(now, 0, createCourseData());
    }
    
    @Test
    public void testSerializeAndDeserializeRaceLogCourseDesignChangedEvent() throws JsonDeserializationException {
        JSONObject jsonCourseDesignEvent = serializer.serialize(event);
        RaceLogCourseDesignChangedEvent deserializedEvent = (RaceLogCourseDesignChangedEvent) deserializer.deserialize(jsonCourseDesignEvent);
        
        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getPassId(), deserializedEvent.getPassId());
        assertEquals(event.getTimePoint(), deserializedEvent.getTimePoint());
        assertEquals(0, Util.size(event.getInvolvedBoats()));
        assertEquals(0, Util.size(deserializedEvent.getInvolvedBoats()));
        
        compareCourseData(event.getCourseDesign(), deserializedEvent.getCourseDesign());
    }

    
    protected CourseBase createCourseData() {
        CourseBase course = new CourseDataImpl("Test Course");
        
        course.addWaypoint(0, new WaypointImpl(new GateImpl(UUID.randomUUID(), 
                new MarkImpl(UUID.randomUUID(), "Black", MarkType.BUOY, "black", "round", "circle"),
                new MarkImpl(UUID.randomUUID(), "Green", MarkType.BUOY, "green", "round", "circle"),
                "Upper gate")));
        course.addWaypoint(1, new WaypointImpl(new MarkImpl(UUID.randomUUID(), "White", MarkType.BUOY, "white", "conical", "bold"), NauticalSide.PORT));
        
        return course;
    }
    
    protected void compareCourseData(CourseBase serializedCourse, CourseBase deserializedCourse) {
        assertEquals(serializedCourse.getFirstWaypoint().getPassingSide(), null);
        assertEquals(deserializedCourse.getFirstWaypoint().getPassingSide(), null);
        Assert.assertTrue(serializedCourse.getFirstWaypoint().getControlPoint() instanceof Gate);
        Assert.assertTrue(deserializedCourse.getFirstWaypoint().getControlPoint() instanceof Gate);
        
        Gate serializedGate = (Gate) serializedCourse.getFirstWaypoint().getControlPoint();
        Gate deserializedGate = (Gate) deserializedCourse.getFirstWaypoint().getControlPoint();
        
        assertEquals(serializedGate.getName(), deserializedGate.getName());
        compareMarks(serializedGate.getLeft(), deserializedGate.getLeft());
        compareMarks(serializedGate.getRight(), deserializedGate.getRight());
        
        assertEquals(serializedCourse.getLastWaypoint().getPassingSide(), NauticalSide.PORT);
        assertEquals(deserializedCourse.getLastWaypoint().getPassingSide(), NauticalSide.PORT);
        Assert.assertTrue(serializedCourse.getLastWaypoint().getControlPoint() instanceof Mark);
        Assert.assertTrue(deserializedCourse.getLastWaypoint().getControlPoint() instanceof Mark);
        
        Mark serializedMark = (Mark) serializedCourse.getLastWaypoint().getControlPoint();
        Mark deserializedMark = (Mark) deserializedCourse.getLastWaypoint().getControlPoint();
        compareMarks(serializedMark, deserializedMark);
    }
    
    private void compareMarks(Mark serializedMark, Mark deserializedMark) {
        assertEquals(serializedMark.getId(), deserializedMark.getId());
        assertEquals(serializedMark.getColor(), deserializedMark.getColor());
        assertEquals(serializedMark.getName(), deserializedMark.getName());
        assertEquals(serializedMark.getPattern(), deserializedMark.getPattern());
        assertEquals(serializedMark.getShape(), deserializedMark.getShape());
        assertEquals(serializedMark.getType(), deserializedMark.getType());
    }

}
