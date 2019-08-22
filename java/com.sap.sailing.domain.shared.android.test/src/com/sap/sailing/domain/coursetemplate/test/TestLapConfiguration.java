package com.sap.sailing.domain.coursetemplate.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.ControlPointTemplate;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.impl.CourseTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkPairTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.RepeatablePartImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointTemplateImpl;
import com.sap.sse.common.Util;

public class TestLapConfiguration {
    private CourseTemplate courseTemplate;
    private MarkTemplate startBoat;
    private MarkTemplate pin;
    private MarkTemplate top;
    private MarkTemplate gateLeft;
    private MarkTemplate gateRight;
    private ControlPointTemplate startFinish;
    private ControlPointTemplate gate;
    
    @Before
    public void setUp() {
        startBoat = new MarkTemplateImpl("Start Boat", "SB", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.STARTBOAT);
        pin = new MarkTemplateImpl("Pin End", "Pin", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        top = new MarkTemplateImpl("Windward Mark", "1", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        gateLeft = new MarkTemplateImpl("Leeward Gate Port", "4p", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        gateRight = new MarkTemplateImpl("Leeward Gate Starboard", "4s", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        startFinish = new MarkPairTemplateImpl("Start/Finish", startBoat, pin);
        gate = new MarkPairTemplateImpl("Leeward Gate", gateLeft, gateRight);
        courseTemplate = new CourseTemplateImpl(UUID.randomUUID(), "L",
                /* marks */ Arrays.asList(startBoat, pin, top, gateLeft, gateRight),
                /* waypoints */ Arrays.asList(new WaypointTemplateImpl(startFinish, PassingInstruction.Line),
                                              new WaypointTemplateImpl(top, PassingInstruction.Port),
                                              new WaypointTemplateImpl(gate, PassingInstruction.Gate),
                                              new WaypointTemplateImpl(top, PassingInstruction.Port),
                                              new WaypointTemplateImpl(startFinish, PassingInstruction.Line)),
                /* associatedRoles */ Collections.emptyMap(),
                /* optionaImageURL */ null,
                new RepeatablePartImpl(/* zeroBasedIndexOfRepeatablePartStart */ 1,
                        /* zeroBasedIndexOfRepeatablePartEnd */ 3));
    }
    
    @Test
    public void testSimpleCourseWithOneLap() {
        final Iterable<WaypointTemplate> waypointsOfOneLapper = courseTemplate.getWaypointTemplates(1);
        final List<ControlPointTemplate> expected = Arrays.asList(startFinish, top, startFinish);
        verify(waypointsOfOneLapper, expected);
    }

    private void verify(final Iterable<WaypointTemplate> waypointsOfOneLapper,
            final List<ControlPointTemplate> expected) {
        assertEquals(expected.size(), Util.size(waypointsOfOneLapper));
        final List<ControlPointTemplate> controlPoints = new ArrayList<>();
        for (final WaypointTemplate waypoint : waypointsOfOneLapper) {
            controlPoints.add(waypoint.getControlPointTemplate());
        }
        assertEquals(expected, controlPoints);
    }

    @Test
    public void testSimpleCourseWithTwoLaps() {
        final Iterable<WaypointTemplate> waypointsOfOneLapper = courseTemplate.getWaypointTemplates(2);
        final List<ControlPointTemplate> expected = Arrays.asList(startFinish, top, gate, top, startFinish);
        verify(waypointsOfOneLapper, expected);
    }

    @Test
    public void testSimpleCourseWithThreeLaps() {
        final Iterable<WaypointTemplate> waypointsOfOneLapper = courseTemplate.getWaypointTemplates(3);
        final List<ControlPointTemplate> expected = Arrays.asList(startFinish, top, gate, top, gate, top, startFinish);
        verify(waypointsOfOneLapper, expected);
    }

    @Test
    public void testExceptionInCaseMarkIsMissing() {
        startBoat = new MarkTemplateImpl("Start Boat", "SB", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.STARTBOAT);
        pin = new MarkTemplateImpl("Pin End", "Pin", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        startFinish = new MarkPairTemplateImpl("Start/Finish", startBoat, pin);
        try {
            courseTemplate = new CourseTemplateImpl("Test",
                    /* marks */ Arrays.asList(startBoat),
                    /* waypoints */ Arrays.asList(new WaypointTemplateImpl(startFinish, PassingInstruction.Line)),
                    /* associatedRoles */ Collections.emptyMap(),
                    /* optionaImageURL */ null);
            fail("Expected an IllegalArgumentException due to missing mark <pin> but it wasn't thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testZeroLapsOkIfNoRepeatablePart() {
        startBoat = new MarkTemplateImpl("Start Boat", "SB", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.STARTBOAT);
        pin = new MarkTemplateImpl("Pin End", "Pin", /* color */ null, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        startFinish = new MarkPairTemplateImpl("Start/Finish", startBoat, pin);
        courseTemplate = new CourseTemplateImpl("Test", /* marks */ Arrays.asList(startBoat, pin),
                /* waypoints */ Arrays.asList(new WaypointTemplateImpl(startFinish, PassingInstruction.Line)),
                /* associatedRoles */ Collections.emptyMap(),
                /* optionaImageURL */ null);
        try {
            courseTemplate.getWaypointTemplates(0);
        } catch (IllegalArgumentException e) {
            assumeNoException("No IllegalArgumentException should have been thrown for zero laps because the course has no repeatable part", e);
        }
    }
    
    @Test
    public void testIllegalArgumentExceptionForZeroLaps() {
        try {
            courseTemplate
                    .getWaypointTemplates(/* illegal to request 0 laps if course template defines repeatable part */ 0);
            fail("Expected an IllegalArgumentException but none was thrown");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
