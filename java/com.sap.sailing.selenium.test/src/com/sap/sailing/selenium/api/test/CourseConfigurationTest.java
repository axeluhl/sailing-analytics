package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseConfiguration;
import com.sap.sailing.selenium.api.coursetemplate.CourseConfigurationApi;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.WaypointTemplate;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class CourseConfigurationTest extends AbstractSeleniumTest {

    private final CourseConfigurationApi courseConfigurationApi = new CourseConfigurationApi();
    private final CourseTemplateApi courseTemplateApi = new CourseTemplateApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headeless */ true);
    }

    @Test
    public void testCreateCourseConfigurationFromTemplate() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final List<MarkTemplate> markTemplates = new ArrayList<>();
        final Map<MarkTemplate, String> roleMapping = new HashMap<>();
        final List<WaypointTemplate> waypointTemplates = new ArrayList<>();
        final List<String> tags = new ArrayList<>();
        CourseTemplate courseTemplate = new CourseTemplate("test", markTemplates, roleMapping, waypointTemplates, null,
                tags, null);

        CourseTemplate srcCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplate);
        CourseConfiguration courseConfiguration = courseConfigurationApi.createCourseConfigurationFromCourseTemplate(
                ctx, srcCourseTemplate.getId(), /* optionalRegattaName */ null);
        assertNotNull(courseConfiguration);
    }

    public void testCreateCourseConfigurationFromCourse() {
        final String regattaName = "test";
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        courseConfigurationApi.createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName());
    }
}
