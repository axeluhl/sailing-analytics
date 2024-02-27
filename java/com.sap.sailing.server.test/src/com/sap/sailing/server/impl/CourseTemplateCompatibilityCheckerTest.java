package com.sap.sailing.server.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.base.ControlPointWithTwoMarks;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.ControlPointWithTwoMarksImpl;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.base.impl.WaypointImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkRole;
import com.sap.sailing.domain.coursetemplate.MarkRolePair;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.domain.coursetemplate.WaypointTemplate;
import com.sap.sailing.domain.coursetemplate.impl.CourseTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkRoleImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkRolePairImpl;
import com.sap.sailing.domain.coursetemplate.impl.MarkTemplateImpl;
import com.sap.sailing.domain.coursetemplate.impl.WaypointTemplateImpl;
import com.sap.sailing.server.impl.CourseAndMarkConfigurationFactoryImpl.CourseTemplateCompatibilityCheckerForCourseBase;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.RepeatablePartImpl;

public class CourseTemplateCompatibilityCheckerTest {
    private CourseTemplateCompatibilityCheckerForCourseBase checker;
    private MarkRole startFinishBoatRole, startFinishPinRole, windwardMarkRole, leewardGatePortRole, leewardGateStarboardRole;
    private MarkTemplate startFinishBoatTemplate, startFinishPinTemplate, windwardMarkTemplate, leewardGatePortTemplate, leewardGateStarboardTemplate;
    private MarkRolePair startFinishLineRoles, leewardGateRoles;
    private WaypointTemplate startWaypointTemplate, firstWindwardMarkWaypointTemplate, leewardGateWaypointTemplate, repeatableWindwardMarkWaypointTemplate, finishWaypointTemplate; 
    private CourseTemplate windwardLeewardWithLeewardFinishCourseTemplate;
    
    @Before
    public void setUp() {
        // mark roles:
        startFinishBoatRole = new MarkRoleImpl(UUID.randomUUID(), "Start/Finish Boat", "SFB");
        startFinishPinRole = new MarkRoleImpl(UUID.randomUUID(), "Start/Finish Pin", "SFP");
        windwardMarkRole = new MarkRoleImpl(UUID.randomUUID(), "Windward Mark", "WW");
        leewardGatePortRole = new MarkRoleImpl(UUID.randomUUID(), "Leeward Gate Port", "LGP");
        leewardGateStarboardRole = new MarkRoleImpl(UUID.randomUUID(), "Leeward Gate Starboard", "LGS");
        // mark templates:
        startFinishBoatTemplate = new MarkTemplateImpl("Start/Finish Boat", "SFB", Color.BROWN, /* shape */ null, /* pattern */ null, MarkType.STARTBOAT);
        startFinishPinTemplate = new MarkTemplateImpl("Start/Finish Pin", "SFP", Color.BROWN, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        windwardMarkTemplate = new MarkTemplateImpl("Windward Mark", "WW", Color.BROWN, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        leewardGatePortTemplate = new MarkTemplateImpl("Leeward Gate Port", "LGP", Color.BROWN, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        leewardGateStarboardTemplate = new MarkTemplateImpl("Leeward Gate Starboard", "LGS", Color.BROWN, /* shape */ null, /* pattern */ null, MarkType.BUOY);
        // MarkRolePairs:
        startFinishLineRoles = new MarkRolePairImpl("Start/Finish Line", "SFL", startFinishPinRole, startFinishBoatRole);
        leewardGateRoles = new MarkRolePairImpl("Leeward Gate", "LG", leewardGatePortRole, leewardGateStarboardRole);
        // role/mark-template associations:
        final Map<MarkRole, MarkTemplate> defaultMarkTemplatesForMarkRoles = new HashMap<>();
        defaultMarkTemplatesForMarkRoles.put(startFinishBoatRole, startFinishBoatTemplate);
        defaultMarkTemplatesForMarkRoles.put(startFinishPinRole, startFinishPinTemplate);
        defaultMarkTemplatesForMarkRoles.put(windwardMarkRole, windwardMarkTemplate);
        defaultMarkTemplatesForMarkRoles.put(leewardGatePortRole, leewardGatePortTemplate);
        defaultMarkTemplatesForMarkRoles.put(leewardGateStarboardRole, leewardGateStarboardTemplate);
        final Map<MarkTemplate, MarkRole> defaultMarkRolesForMarkTemplates = new HashMap<>();
        defaultMarkRolesForMarkTemplates.put(startFinishBoatTemplate, startFinishBoatRole);
        defaultMarkRolesForMarkTemplates.put(startFinishPinTemplate, startFinishPinRole);
        defaultMarkRolesForMarkTemplates.put(windwardMarkTemplate, windwardMarkRole);
        defaultMarkRolesForMarkTemplates.put(leewardGatePortTemplate, leewardGatePortRole);
        defaultMarkRolesForMarkTemplates.put(leewardGateStarboardTemplate, leewardGateStarboardRole);
        // waypoint templates:
        startWaypointTemplate = new WaypointTemplateImpl(startFinishLineRoles, PassingInstruction.Line);
        firstWindwardMarkWaypointTemplate = new WaypointTemplateImpl(windwardMarkRole, PassingInstruction.Port);
        leewardGateWaypointTemplate = new WaypointTemplateImpl(leewardGateRoles, PassingInstruction.Gate);
        repeatableWindwardMarkWaypointTemplate = new WaypointTemplateImpl(windwardMarkRole, PassingInstruction.Port);
        finishWaypointTemplate = new WaypointTemplateImpl(startFinishLineRoles, PassingInstruction.Line);
        // assemble course template:
        windwardLeewardWithLeewardFinishCourseTemplate = new CourseTemplateImpl(UUID.randomUUID(),
                "Windward/Leeward with Leeward Finish", "L",
                Arrays.asList(startFinishBoatTemplate, startFinishPinTemplate, windwardMarkTemplate,
                        leewardGatePortTemplate, leewardGateStarboardTemplate),
                Arrays.asList(startWaypointTemplate, firstWindwardMarkWaypointTemplate, leewardGateWaypointTemplate,
                        repeatableWindwardMarkWaypointTemplate, finishWaypointTemplate),
                defaultMarkTemplatesForMarkRoles, defaultMarkRolesForMarkTemplates, /* optionalImageURL */ null,
                /* optionalRepeatablePart */ new RepeatablePartImpl(2, 4), /* defaultNumberOfLaps */ 1);
    }
    
    @Test
    public void testSimpleCourseComplianceWithNoRepeatablePart() {
        final CourseBase courseBase = new CourseDataImpl("L2", windwardLeewardWithLeewardFinishCourseTemplate.getId());
        final Mark startBoat, startPin, windward, gatePort, gateStarboard;
        final ControlPointWithTwoMarks startFinishLine, gate;
        final Waypoint startWP, windward1WP, gateWP, windward2WP, finishWP;
        // marks:
        startBoat = new MarkImpl(UUID.randomUUID(), "Start Boat", MarkType.STARTBOAT, /* color */ null, /* shape */ null, /* pattern */ null);
        startPin = new MarkImpl(UUID.randomUUID(), "Start Pin");
        windward = new MarkImpl(UUID.randomUUID(), "Winward");
        gatePort = new MarkImpl(UUID.randomUUID(), "Gate Port");
        gateStarboard = new MarkImpl(UUID.randomUUID(), "Gate Starboard");
        // control points with two marks:
        startFinishLine = new ControlPointWithTwoMarksImpl(startPin, startBoat, "Start/Finish Line", "SFL");
        gate = new ControlPointWithTwoMarksImpl(gatePort, gateStarboard, "Leeward Gate", "LG");
        // waypoints:
        startWP = new WaypointImpl(startFinishLine, PassingInstruction.Line);
        windward1WP = new WaypointImpl(windward, PassingInstruction.Port);
        gateWP = new WaypointImpl(gate, PassingInstruction.Gate);
        windward2WP = new WaypointImpl(windward, PassingInstruction.Port);
        finishWP = new WaypointImpl(startFinishLine, PassingInstruction.Line);
        // course:
        courseBase.addWaypoint(0, startWP);
        courseBase.addRoleMapping(startBoat, startFinishBoatRole.getId());
        courseBase.addRoleMapping(startPin, startFinishPinRole.getId());
        courseBase.addWaypoint(1, windward1WP);
        courseBase.addRoleMapping(windward, windwardMarkRole.getId());
        courseBase.addWaypoint(2, gateWP);
        courseBase.addRoleMapping(gatePort, leewardGatePortRole.getId());
        courseBase.addRoleMapping(gateStarboard, leewardGateStarboardRole.getId());
        courseBase.addWaypoint(3, windward2WP);
        courseBase.addWaypoint(4, finishWP);
        checker = new CourseAndMarkConfigurationFactoryImpl(/* sharedSailingDataTracker */ null, /* sensorFixStore */ null, /* raceLogResolver */ null, DomainFactory.INSTANCE)
                .new CourseTemplateCompatibilityCheckerForCourseBase(courseBase, windwardLeewardWithLeewardFinishCourseTemplate);
        final Integer numberOfLaps = checker.isCourseInstanceOfCourseTemplate();
        assertEquals(2, (int) numberOfLaps);
    }
}
