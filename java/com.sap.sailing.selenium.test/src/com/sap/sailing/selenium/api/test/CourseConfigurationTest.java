package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.coursetemplate.CourseConfiguration;
import com.sap.sailing.selenium.api.coursetemplate.CourseConfigurationApi;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplate;
import com.sap.sailing.selenium.api.coursetemplate.CourseTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkConfiguration;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.WaypointTemplate;
import com.sap.sailing.selenium.api.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.helper.CourseTemplateDataFactory;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class CourseConfigurationTest extends AbstractSeleniumTest {

    private final CourseConfigurationApi courseConfigurationApi = new CourseConfigurationApi();
    private final CourseTemplateApi courseTemplateApi = new CourseTemplateApi();
    private final MarkTemplateApi markTemplateApi = new MarkTemplateApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi LeaderboardApi = new LeaderboardApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void testCreateCourseFromCourseTemplateWithoutChanges() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final CourseTemplateDataFactory ctdf = new CourseTemplateDataFactory(ctx);
        
        final Map<MarkTemplate, String> associatedRoles = new HashMap<>();
        associatedRoles.put(ctdf.sb, "Startboat");
        associatedRoles.put(ctdf.pe, "Pinend");
        associatedRoles.put(ctdf.b1, "1");
        associatedRoles.put(ctdf.b4s, "4s");
        associatedRoles.put(ctdf.b4p, "4p");
        
        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                ctdf.constructCourseTemplate(new Pair<>(1, 3), 2, associatedRoles));
        
        CourseConfiguration courseConfiguration = courseConfigurationApi.createCourseConfigurationFromCourseTemplate(
                ctx, createdCourseTemplate.getId(), /* optionalRegattaName */ null, /* tags */ null);
        
        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        courseConfigurationApi.createCourse(ctx, courseConfiguration, regattaName, race.getRaceName(), "Default");
        CourseConfiguration createdCourseAsConfiguration = courseConfigurationApi.createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName(), "Default", null);
        assertEquals(6, Util.size(createdCourseAsConfiguration.getMarkConfigurations()));
        // TODO assert reference to CourseTemplate ID
        // TODO assert course sequence
        // TODO assert that for every Mark we get the correct originating MarkTemplate ID
        // TODO assert that number of laps is correctly restored
    }

    @Test
    public void testCreateCourseFromFreestyleConfigurationWithPositioning() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];
        
        MarkConfiguration sb = MarkConfiguration.createFreestyle(null, null, "role_sb", "startboat", "sb", null, null, null, null);
        sb.setFixedPosition(5.5, 7.1);
        MarkConfiguration pe = MarkConfiguration.createFreestyle(null, null, "role_pe", "pin end", "pe", null, null, null, null);
        UUID randomDeviceId = UUID.randomUUID();
        pe.setTrackingDeviceId(randomDeviceId);
        MarkConfiguration bl = MarkConfiguration.createFreestyle(null, null, "role_bl", "1", null, "#0000FF", null, null, null);
        WaypointWithMarkConfiguration wp1 = new WaypointWithMarkConfiguration("start/end", "s/e", PassingInstruction.Line, Arrays.asList(sb.getId(), pe.getId()));
        WaypointWithMarkConfiguration wp2 = new WaypointWithMarkConfiguration(null, null, PassingInstruction.Port, Arrays.asList(bl.getId()));
        
        CourseConfiguration courseConfiguration = new CourseConfiguration("my-freestyle-course", Arrays.asList(sb, pe, bl), Arrays.asList(wp1, wp2, wp1));
        
        courseConfigurationApi.createCourse(ctx, courseConfiguration, regattaName, race.getRaceName(), "Default");
        CourseConfiguration createdCourseAsConfiguration = courseConfigurationApi.createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName(), "Default", null);
        // TODO assert roles
        // TODO assert course sequence
        // TODO assert positioning
        // TODO assert short names
    }

    @Test
    public void testCreateCourseConfigurationFromTemplate() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final List<MarkTemplate> markTemplates = new ArrayList<>();
        MarkTemplate mt1 = markTemplateApi.createMarkTemplate(ctx, "mark template 1", "mt1", "#FFFFFF", "Cylinder", "Checkered", MarkType.BUOY.name());
        markTemplates.add(mt1);
        
        final Map<MarkTemplate, String> roleMapping = new HashMap<>();

        final List<WaypointTemplate> waypointTemplates = new ArrayList<>();
        WaypointTemplate wpt1 = new WaypointTemplate("wpt1", PassingInstruction.FixedBearing, markTemplates);
        waypointTemplates.add(wpt1);
        
        final List<String> tags = new ArrayList<>();
        CourseTemplate courseTemplate = new CourseTemplate("test", markTemplates, roleMapping, waypointTemplates, null,
                tags, null, null);

        CourseTemplate srcCourseTemplate = courseTemplateApi.createCourseTemplate(ctx, courseTemplate);
        CourseConfiguration courseConfiguration = courseConfigurationApi.createCourseConfigurationFromCourseTemplate(
                ctx, srcCourseTemplate.getId(), /* optionalRegattaName */ null, /* tags */ null);
        assertNotNull(courseConfiguration);
    }

    @Test
    public void testCreateCourseConfigurationFromCourse() {
        final String regattaName = "test";
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];
        LeaderboardApi.startRaceLogTracking(ctx, regattaName, race.getRaceName(), "Default");

        courseConfigurationApi.createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName(), "Default",
                /* tags */ null);
    }

    @Test
    public void testCreateCourseFromCourseConfiguration() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        courseConfigurationApi.createCourse(ctx, createSimpleCourseConfiguration(ctx), regattaName, race.getRaceName(), "Default");
    }

    @Test
    public void testCreateCourseTemplateFromCourseConfiguration() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        courseConfigurationApi.createCourseTemplate(ctx, createSimpleCourseConfiguration(ctx),
                /* optionalRegattaName */ null);
    }

    private CourseConfiguration createSimpleCourseConfiguration(final ApiContext ctx) {
        List<MarkConfiguration> markConfigurations = new ArrayList<MarkConfiguration>();
        MarkTemplate markTemplate = markTemplateApi.createMarkTemplate(ctx, "test", "test", "#ffffff", "shape",
                "pattern", MarkType.LANDMARK.name());
        MarkConfiguration markConfiguration = MarkConfiguration.createMarkTemplateBased(markTemplate.getId(), "test");
        markConfigurations.add(markConfiguration);
        List<String> markConfigurationIds = markConfigurations.stream().map(mc -> mc.getId())
                .collect(Collectors.toList());

        List<WaypointWithMarkConfiguration> waypoints = new ArrayList<>();
        WaypointWithMarkConfiguration waypoint = new WaypointWithMarkConfiguration("test", null, PassingInstruction.Line,
                markConfigurationIds);
        waypoints.add(waypoint);

        return new CourseConfiguration("test-course", markConfigurations, waypoints);
    }
}
