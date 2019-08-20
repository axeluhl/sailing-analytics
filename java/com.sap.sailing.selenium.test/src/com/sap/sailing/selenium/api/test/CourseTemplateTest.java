package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class CourseTemplateTest extends AbstractSeleniumTest {

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
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)));
    }

    @Test
    public void createSimpleCourseTemplateTest() {
        final CourseTemplate courseTemplateToSave = constructCourseTemplateWithoutRepeatablePart();

        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(courseTemplateToSave.getName(), createdCourseTemplate.getName());
        assertEquals(waypointSequence.size(), Util.size(createdCourseTemplate.getWaypoints()));
    }

    @Test
    public void createCourseTemplateWithRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 2);
        final CourseTemplate courseTemplateToSave = constructCourseTemplate(repeatablePart);
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);

        assertEquals(repeatablePart, createdCourseTemplate.getOptionalRepeatablePart());
    }
    
    @Test
    public void createCourseTemplateWithInvalidRepeatablePartTest() {
        final Pair<Integer, Integer> repeatablePart = new Pair<>(1, 5);
        final CourseTemplate courseTemplateToSave = constructCourseTemplate(repeatablePart);
        
        try {
            courseTemplateApi.createCourseTemplate(ctx, courseTemplateToSave);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Repeatable part (1, 5) is out of range for sequence of length 4"));
        }
    }
    
    private CourseTemplate constructCourseTemplateWithoutRepeatablePart() {
        return constructCourseTemplate(null);
    }
    
    private CourseTemplate constructCourseTemplate(Pair<Integer, Integer> optionalRepeatablePart) {
        return new CourseTemplate("my-special-course-template",
                Arrays.asList(sb, pe, b1, b4s, b4p, spare), Collections.emptyMap(), waypointSequence, optionalRepeatablePart,
                Collections.emptySet(), null);
    }
}
