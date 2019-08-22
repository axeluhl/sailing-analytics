package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.WaypointTemplate;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class CourseTemplateApiTest extends AbstractSeleniumTest {

    private final MarkTemplateApi markTemplateApi = new MarkTemplateApi();
    private final CourseTemplateApi courseTemplateApi = new CourseTemplateApi();
    private ApiContext ctx;
    private MarkTemplate sb;
    private MarkTemplate pe;
    private MarkTemplate b1;
    private MarkTemplate b4s;
    private MarkTemplate b4p;
    private MarkTemplate spare;
    private List<WaypointTemplate> waypointSequence;

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);

        ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        sb = markTemplateApi.createMarkTemplate(ctx, "Startboat", "sb", null, null, null, MarkType.STARTBOAT.name());
        pe = markTemplateApi.createMarkTemplate(ctx, "Pinend", "pe", "#000000", null, null, MarkType.BUOY.name());
        b1 = markTemplateApi.createMarkTemplate(ctx, "blue", "blue", "#0000FF", null, null, MarkType.BUOY.name());
        b4s = markTemplateApi.createMarkTemplate(ctx, "red", "red", "#FF0000", null, null, MarkType.BUOY.name());
        b4p = markTemplateApi.createMarkTemplate(ctx, "green", "green", "#00FF00", null, null, MarkType.BUOY.name());
        spare = markTemplateApi.createMarkTemplate(ctx, "spare", "spare", "#00FFFF", null, null, MarkType.BUOY.name());

        waypointSequence = Arrays.asList(
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1)),
                new WaypointTemplate(null, PassingInstruction.Gate, Arrays.asList(b4p, b4s)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1)),
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)));
    }

    @Test
    public void createSimpleCourseTemplateTest() {
        final CourseTemplate courseTemplateToSave = constructCourseTemplate();

        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(courseTemplateToSave.getName(), createdCourseTemplate.getName());
        assertEquals(waypointSequence.size(), Util.size(createdCourseTemplate.getWaypoints()));
    }
    
    @Test
    public void cantUseOthersMarkTemplatesTest() {
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        new SecurityApi().createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        final ApiContext otherUserCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        
        try {
            courseTemplateApi.createCourseTemplate(otherUserCtx, constructCourseTemplate());
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Subject does not have permission [MARK_TEMPLATE:READ:"));
        }
    }

    @Test
    public void createCourseTemplateWithRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 3);
        final CourseTemplate courseTemplateToSave = constructCourseTemplate(repeatablePart);
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(repeatablePart, createdCourseTemplate.getOptionalRepeatablePart());
    }
    
    @Test
    public void createCourseTemplateWithInvalidRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 6);
        final CourseTemplate courseTemplateToSave = constructCourseTemplate(repeatablePart);
        
        try {
            courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Repeatable part (1, 6) is out of range for sequence of length 5"));
        }
    }
    
    @Test
    public void createCourseTemplateWithRoleMappingTest() {
        final Map<MarkTemplate, String> associatedRoles = new HashMap<>();
        associatedRoles.put(sb, "Startboat");
        associatedRoles.put(pe, "Pinend");
        associatedRoles.put(b1, "1");
        associatedRoles.put(b4s, "4s");
        associatedRoles.put(b4p, "4p");
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                constructCourseTemplate(null, associatedRoles));
        
        assertEquals(new HashSet<>(associatedRoles.values()),
                new HashSet<>(createdCourseTemplate.getRoleMapping().values()));
    }
    
    @Test
    public void createCourseTemplateWithImplicitRoleMappingTest() {
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, constructCourseTemplate());
        
        assertEquals(new HashSet<>(Arrays.asList(sb.getShortName(), pe.getShortName(), b1.getShortName(),
                b4s.getShortName(), b4p.getShortName())),
                new HashSet<>(createdCourseTemplate.getRoleMapping().values()));
    }
    
    @Test
    public void createCourseTemplateWithPartialRoleMappingTest() {
        final Map<MarkTemplate, String> associatedRoles = new HashMap<>();
        associatedRoles.put(b1, "1");
        associatedRoles.put(b4s, "4s");
        associatedRoles.put(b4p, "4p");
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                constructCourseTemplate(null, associatedRoles));
        
        assertEquals(new HashSet<>(Arrays.asList(sb.getShortName(), pe.getShortName(), "1",
                "4s", "4p")),
                new HashSet<>(createdCourseTemplate.getRoleMapping().values()));
    }
    
    @Test
    public void createCourseTemplateWithInvalidRoleMappingTest() {
        final Map<MarkTemplate, String> associatedRoles = new HashMap<>();
        associatedRoles.put(b4s, "4");
        associatedRoles.put(b4p, "4");
        
        final CourseTemplate courseTemplateToSave = constructCourseTemplate(null, associatedRoles);
        
        try {
            courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Role name 4 can't be used twice in a course template"));
        }
    }
    
    private CourseTemplate constructCourseTemplate() {
        return constructCourseTemplate(null);
    }
    
    private CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart) {
        return this.constructCourseTemplate(optionalRepeatablePart, Collections.emptyMap());
    }
    
    private CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart, Map<MarkTemplate, String> associatedRoles) {
        return new CourseTemplate("my-special-course-template",
                Arrays.asList(sb, pe, b1, b4s, b4p, spare), associatedRoles, waypointSequence, optionalRepeatablePart,
                Collections.emptySet(), null);
    }
}
