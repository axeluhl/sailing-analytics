package com.sap.sailing.mongodb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;

import org.junit.Test;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.sap.sailing.domain.abstractlog.race.RaceLogCourseDesignChangedEvent;
import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.persistence.impl.DomainObjectFactoryImpl;
import com.sap.sailing.domain.persistence.impl.FieldNames;
import com.sap.sse.common.impl.AbstractColor;
import com.sap.sse.mongodb.MongoDBConfiguration;

public class TestParseAndMigratePassingSideAndControlPointTwoMarks {
    public TestParseAndMigratePassingSideAndControlPointTwoMarks() {
        super();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void test() {
        DomainObjectFactoryImpl dof = new DomainObjectFactoryImpl(MongoDBConfiguration.getDefaultTestConfiguration()
                .getService().getDB(), com.sap.sailing.domain.base.DomainFactory.INSTANCE);
        DBObject waypoint1 = new BasicDBObject();
        waypoint1.put(FieldNames.WAYPOINT_PASSINGSIDE.name(), "Gate");
        DBObject cp = new BasicDBObject();
        cp.put(FieldNames.CONTROLPOINT_CLASS.name(), "Gate");
        DBObject cpValue = new BasicDBObject();
        cpValue.put(FieldNames.GATE_NAME.name(), "Name");
        cpValue.put(FieldNames.GATE_ID.name(), (Serializable) 0);
        Mark mark1 = new MarkImpl(0, "Left", MarkType.BUOY, AbstractColor.getCssColor("blue"), "square", "checkers");
        DBObject dbMark1 = new BasicDBObject();
        dbMark1.put(FieldNames.MARK_ID.name(), mark1.getId());
        dbMark1.put(FieldNames.MARK_COLOR.name(), mark1.getColor().getAsHtml());
        dbMark1.put(FieldNames.MARK_NAME.name(), mark1.getName());
        dbMark1.put(FieldNames.MARK_PATTERN.name(), mark1.getPattern());
        dbMark1.put(FieldNames.MARK_SHAPE.name(), mark1.getShape());
        dbMark1.put(FieldNames.MARK_TYPE.name(), mark1.getType().name());
        Mark mark2 = new MarkImpl(0, "Right", MarkType.BUOY, AbstractColor.getCssColor("blue"), "square", "checkers");
        DBObject dbMark2 = new BasicDBObject();
        dbMark2.put(FieldNames.MARK_ID.name(), mark2.getId());
        dbMark2.put(FieldNames.MARK_COLOR.name(), mark2.getColor().getAsHtml());
        dbMark2.put(FieldNames.MARK_NAME.name(), mark2.getName());
        dbMark2.put(FieldNames.MARK_PATTERN.name(), mark2.getPattern());
        dbMark2.put(FieldNames.MARK_SHAPE.name(), mark2.getShape());
        dbMark2.put(FieldNames.MARK_TYPE.name(), mark2.getType().name());
        cpValue.put(FieldNames.GATE_RIGHT.name(), dbMark2);
        cpValue.put(FieldNames.GATE_LEFT.name(), dbMark1);
        cp.put(FieldNames.CONTROLPOINT_VALUE.name(), cpValue);
        waypoint1.put(FieldNames.CONTROLPOINT.name(), cp);
        DBObject waypoint2 = new BasicDBObject();
        waypoint2.put(FieldNames.WAYPOINT_PASSINGSIDE.name(), "Gate");
        waypoint2.put(FieldNames.CONTROLPOINT.name(), cp);
        DBObject waypoint3 = new BasicDBObject();
        waypoint3.put(FieldNames.WAYPOINT_PASSINGSIDE.name(), "Gate");
        waypoint3.put(FieldNames.CONTROLPOINT.name(), cp);
        BasicDBList list = new BasicDBList();
        list.add(waypoint1);
        list.add(waypoint2);
        list.add(waypoint3);
        DBObject raceLogEvent = new BasicDBObject();
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_CLASS.name(), RaceLogCourseDesignChangedEvent.class.getSimpleName());
        raceLogEvent.put(FieldNames.RACE_LOG_COURSE_DESIGN.name(), list);
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_CREATED_AT.name(), System.currentTimeMillis());
        raceLogEvent.put("TIME_AS_MILLIS", System.currentTimeMillis());
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_INVOLVED_BOATS.name(), new BasicDBList());
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_PASS_ID.name(), 1);
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_AUTHOR_NAME.name(), "Test Author");
        raceLogEvent.put(FieldNames.RACE_LOG_EVENT_AUTHOR_PRIORITY.name(), 1);
        RaceLogCourseDesignChangedEvent event = (RaceLogCourseDesignChangedEvent) dof.loadRaceLogEvent(raceLogEvent).getA();
        assertEquals(event.getCourseDesign().getFirstWaypoint().getPassingInstructions(), PassingInstruction.Line);
        assertTrue(waypoint1.containsField(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name())
                && !waypoint1.containsField(FieldNames.WAYPOINT_PASSINGSIDE.name()));
        assertTrue(waypoint2.get(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name()).equals("Gate"));
        assertTrue(waypoint3.get(FieldNames.WAYPOINT_PASSINGINSTRUCTIONS.name()).equals("Line"));
        assertTrue(cp.get(FieldNames.CONTROLPOINT_CLASS.name()).equals(ControlPointWithTwoMarks.class.getSimpleName()));
        assertTrue(cpValue.containsField(FieldNames.CONTROLPOINTWITHTWOMARKS_ID.name())
                && cpValue.containsField(FieldNames.CONTROLPOINTWITHTWOMARKS_NAME.name())
                && cpValue.containsField(FieldNames.CONTROLPOINTWITHTWOMARKS_LEFT.name())
                && cpValue.containsField(FieldNames.CONTROLPOINTWITHTWOMARKS_RIGHT.name())
                && !cpValue.containsField(FieldNames.GATE_NAME.name())
                && !cpValue.containsField(FieldNames.GATE_ID.name())
                && !cpValue.containsField(FieldNames.GATE_LEFT.name())
                && !cpValue.containsField(FieldNames.GATE_RIGHT.name()));
    }
}
