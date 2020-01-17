package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SHARED_SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkRole;
import com.sap.sailing.selenium.api.coursetemplate.MarkRoleApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.helper.CourseTemplateDataFactory;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class CourseTemplateApiTest extends AbstractSeleniumTest {

    private final CourseTemplateApi courseTemplateApi = new CourseTemplateApi();
    private final MarkRoleApi markRoleApi = new MarkRoleApi();
    private ApiContext ctx;
    private CourseTemplateDataFactory ctdf;

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);

        ctx = createAdminApiContext(getContextRoot(), SHARED_SERVER_CONTEXT);
        ctdf = new CourseTemplateDataFactory(ctx);
    }

    @Test
    public void createSimpleCourseTemplateTest() {
        final CourseTemplate courseTemplateToSave = ctdf.constructCourseTemplate();

        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(courseTemplateToSave.getName(), createdCourseTemplate.getName());
        assertEquals(ctdf.waypointSequence.size(), Util.size(createdCourseTemplate.getWaypoints()));
    }

    @Test
    public void cantUseOthersMarkTemplatesTest() {
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        new SecurityApi().createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        final ApiContext otherUserCtx = createApiContext(getContextRoot(), SHARED_SERVER_CONTEXT, "donald", "daisy0815");

        try {
            courseTemplateApi.createCourseTemplate(otherUserCtx, ctdf.constructCourseTemplate());
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Subject does not have permission [MARK_TEMPLATE:READ:"));
        }
    }

    @Test
    public void createCourseTemplateWithRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 3);
        final CourseTemplate courseTemplateToSave = ctdf.constructCourseTemplate(repeatablePart,
                /* defaultNumberOfLaps */null);

        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(repeatablePart, createdCourseTemplate.getOptionalRepeatablePart());
    }

    @Test
    public void createCourseTemplateWithInvalidRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 6);
        final CourseTemplate courseTemplateToSave = ctdf.constructCourseTemplate(repeatablePart,
                /* defaultNumberOfLaps */null);
        try {
            courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Repeatable part (1, 6) is out of range for sequence of length 5"));
        }
    }

    @Test
    public void createCourseTemplateWithRoleMappingTest() {
        final Map<MarkRole, MarkTemplate> associatedRoles = new HashMap<>();
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "Startboat", /* shortName */ null), ctdf.sb);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "Pinend", /* shortName */ null), ctdf.pe);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "1", /* shortName */ null), ctdf.b1);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "4s", /* shortName */ null), ctdf.b4s);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "4p", /* shortName */ null), ctdf.b4p);
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                ctdf.constructCourseTemplate(null, /* defaultNumberOfLaps */null, associatedRoles));
        assertEquals(new TreeSet<>(associatedRoles.values()),
                new TreeSet<MarkRole>(createdCourseTemplate.getRoleMapping().keySet().stream()
                        .map(r -> markRoleApi.getMarkRole(ctx, r.getAssociatedMarkRoleId())).collect(Collectors.toSet())));
    }

    @Test
    public void createCourseTemplateWithImplicitRoleMappingTest() {
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                ctdf.constructCourseTemplate());
        assertEquals(
                new HashSet<>(Arrays.asList(ctdf.sb.getName(), ctdf.pe.getName(), ctdf.b1.getName(), ctdf.b4s.getName(),
                        ctdf.b4p.getName())),
                new HashSet<>(createdCourseTemplate.getRoleMapping().keySet().stream()
                        .map(r -> markRoleApi.getMarkRole(ctx, r.getAssociatedMarkRoleId()).getName())
                        .collect(Collectors.toSet())));
    }

    @Test
    public void createCourseTemplateWithPartialRoleMappingTest() {
        final Map<MarkRole, MarkTemplate> associatedRoles = new HashMap<>();
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "1", /* shortName */ null), ctdf.b1);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "4s", /* shortName */ null), ctdf.b4s);
        associatedRoles.put(markRoleApi.createMarkRole(ctx, "4p", /* shortName */ null), ctdf.b4p);
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                ctdf.constructCourseTemplate(null, /* defaultNumberOfLaps */null, associatedRoles));
        assertEquals(new HashSet<>(Arrays.asList(ctdf.sb.getName(), ctdf.pe.getName(), "1", "4s", "4p")),
                new HashSet<>(createdCourseTemplate.getRoleMapping().keySet().stream()
                        .map(r -> markRoleApi.getMarkRole(ctx, r.getAssociatedMarkRoleId()).getName())
                        .collect(Collectors.toSet())));
    }

    @Test
    public void createCourseTemplateWithInvalidRoleMappingTest() {
        final Map<MarkRole, MarkTemplate> associatedRoles = new HashMap<>();
        final MarkRole markRole = markRoleApi.createMarkRole(ctx, "4", /* shortName */ null);
        associatedRoles.put(markRole, ctdf.b4s);
        associatedRoles.put(markRole, ctdf.b4p);
        final CourseTemplate courseTemplateToSave = ctdf.constructCourseTemplate(null, /* defaultNumberOfLaps */null, associatedRoles);
        try {
            courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Role name 4 can't be used twice in a course template"));
        }
    }
}
