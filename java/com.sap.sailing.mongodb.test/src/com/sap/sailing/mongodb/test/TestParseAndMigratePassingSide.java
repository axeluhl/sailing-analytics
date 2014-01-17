package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sailing.domain.racelog.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.mongodb.MongoDBConfiguration;

public class TestParseAndMigratePassingSide {
    public TestParseAndMigratePassingSide() {
        super();
    }

    @Test
    public void test() {
        DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(MongoDBConfiguration.getDefaultTestConfiguration()
                .getService().getDB(), com.sap.sailing.domain.base.DomainFactory.INSTANCE);
        DBObject waypoint = new BasicDBObject();
        waypoint.put(FieldNames.WAYPOINT_PASSINGSIDE.name(), "Gate");
        waypoint.put(FieldNames.CONTROLPOINT.name(), new BasicDBObject());
        BasicDBList list = new BasicDBList();
        list.add(waypoint);
        DBObject raceLogEvent = new BasicDBObject();
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogCourseDesignChangedEvent.class.getSimpleName());
        raceLogEvent.put(FieldNames.RACE_LOG_COURSE_DESIGN.name(), list);
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_CREATED_AT.name(), System.currentTimeMillis());
        raceLogEvent.put("TIME_AS_MILLIS", System.currentTimeMillis());
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), new BasicDBList());
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_PASS_ID.name(), 1);
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_AUTHOR_NAME.name(), "Test Author");
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_AUTHOR_PRIORITY.name(), 1);
        RaceLogCourseDesignChangedEvent event = (RaceLogCourseDesignChangedEvent) dof.loadRaceLogEvent(raceLogEvent);
        assertEquals(event.getCourseDesign().getFirstWaypoint().getPassingInstructions(), PassingInstruction.Gate);
        assertTrue(waypoint.containsField(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name())
                && !waypoint.containsField(FieldNames.WAYPOINT_PASSINGSIDE.name()));
    }
}
