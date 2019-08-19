package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;

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

public class CourseTemplateTest extends AbstractSeleniumTest {

    private final MarkTemplateApi markTemplateApi = new MarkTemplateApi();
    private final CourseTemplateApi courseTemplateApi = new CourseTemplateApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void createCourseTemplateTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkTemplate sb = markTemplateApi.createMarkTemplate(ctx, "Startboat", "sb",
                null, null, null, MarkType.STARTBOAT.name());
        MarkTemplate pe = markTemplateApi.createMarkTemplate(ctx, "Pinend", "pe",
                "#000000", null, null, MarkType.BUOY.name());
        MarkTemplate b1 = markTemplateApi.createMarkTemplate(ctx, "blue", "blue",
                "#0000FF", null, null, MarkType.BUOY.name());
        MarkTemplate b4s = markTemplateApi.createMarkTemplate(ctx, "red", "red",
                "#FF0000", null, null, MarkType.BUOY.name());
        MarkTemplate b4p = markTemplateApi.createMarkTemplate(ctx, "green", "green",
                "#00FF00", null, null, MarkType.BUOY.name());
        MarkTemplate spare = markTemplateApi.createMarkTemplate(ctx, "spare", "spare",
                "#00FFFF", null, null, MarkType.BUOY.name());
        
        final List<WaypointTemplate> waypointSequence = Arrays.asList(new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)),
                new WaypointTemplate(null, PassingInstruction.Port, Arrays.asList(b1)),
                new WaypointTemplate(null, PassingInstruction.Gate, Arrays.asList(b4p, b4s)),
                new WaypointTemplate("Start/End", PassingInstruction.Line, Arrays.asList(sb, pe)));
        
        final CourseTemplate courseTemplateToSave = new CourseTemplate("my-special-course-template", Arrays.asList(sb, pe, b1, b4s, b4p, spare),
                Collections.emptyMap(),
                waypointSequence,
                null, Collections.emptySet(), null);
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                courseTemplateToSave);
        
        assertEquals(courseTemplateToSave.getName(), createdCourseTemplate.getName());
        assertEquals(waypointSequence.size(), Util.size(createdCourseTemplate.getWaypoints()));
        
    }
}
