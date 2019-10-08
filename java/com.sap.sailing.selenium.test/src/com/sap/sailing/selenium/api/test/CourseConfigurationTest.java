package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import com.sap.sailing.selenium.api.coursetemplate.MarkAppearance;
import com.sap.sailing.selenium.api.coursetemplate.MarkConfiguration;
import com.sap.sailing.selenium.api.coursetemplate.MarkProperties;
import com.sap.sailing.selenium.api.coursetemplate.MarkPropertiesApi;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplate;
import com.sap.sailing.selenium.api.coursetemplate.MarkTemplateApi;
import com.sap.sailing.selenium.api.coursetemplate.Positioning;
import com.sap.sailing.selenium.api.coursetemplate.RepeatablePart;
import com.sap.sailing.selenium.api.coursetemplate.WaypointTemplate;
import com.sap.sailing.selenium.api.coursetemplate.WaypointWithMarkConfiguration;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.event.MarkApi;
import com.sap.sailing.selenium.api.event.MarkApi.Mark;
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
    private final MarkPropertiesApi markPropertiesApi = new MarkPropertiesApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi LeaderboardApi = new LeaderboardApi();
    private final MarkApi markApi = new MarkApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    private void assertConsistentCourseConfiguration(final CourseConfiguration courseConfiguration) {
        for (final WaypointWithMarkConfiguration wp : courseConfiguration.getWaypoints()) {
            for (final String mcId : wp.getMarkConfigurationIds()) {
                boolean found = false;
                for (final MarkConfiguration mc : courseConfiguration.getMarkConfigurations()) {
                    if (mc.getId().equals(mcId)) {
                        found = true;
                    }
                }
                assertTrue("No markconfiguration with id " + mcId + " for waypoint", found);
            }
        }
    }

    private void assertCourseConfigurationCompared(final ApiContext ctx,
            final CourseConfiguration srcCourseConfiguration, final CourseConfiguration trgtCourseConfiguration) {
        final Iterable<MarkConfiguration> srcMarkConfigurations = srcCourseConfiguration.getMarkConfigurations();
        final Iterable<MarkConfiguration> trgtMarkConfigurations = trgtCourseConfiguration.getMarkConfigurations();
        final Iterable<WaypointWithMarkConfiguration> srcWaypoints = srcCourseConfiguration.getWaypoints();
        final Iterable<WaypointWithMarkConfiguration> trgtWaypoints = trgtCourseConfiguration.getWaypoints();
        assertEquals("number of markconfiguration is different", Util.size(srcMarkConfigurations),
                Util.size(trgtMarkConfigurations));
        assertEquals("number of waypoints is different", Util.size(srcWaypoints), Util.size(trgtWaypoints));
        for (final MarkConfiguration markConfiguration : srcMarkConfigurations) {
            final UUID markTemplateId = markConfiguration.getMarkTemplateId();
            MarkAppearance srcAppearance = markConfiguration.getEffectiveProperties();
            if (srcAppearance == null) {
                srcAppearance = markConfiguration.getFreestyleProperties();
            }
            Positioning srcPositioning = markConfiguration.getPositioning();
            if (srcPositioning == null) {
                srcPositioning = markConfiguration.getEffectivePositioning();
            }
            boolean found = false;
            for (final MarkConfiguration trgtMarkConfiguration : trgtMarkConfigurations) {
                final MarkAppearance trgtAppearance = trgtMarkConfiguration.getEffectiveProperties();
                if (srcAppearance == null && trgtAppearance == null) {
                    return;
                }
                final boolean matchByName = srcAppearance != null && trgtAppearance != null
                        && srcAppearance.getName().equals(trgtAppearance.getName());
                final boolean matchByTemplateId = markTemplateId != null
                        && markTemplateId.equals(trgtMarkConfiguration.getMarkTemplateId());
                final boolean matchByMarkId = markConfiguration.getMarkId() != null
                        && markConfiguration.getMarkId().equals(trgtMarkConfiguration.getMarkId());
                // final boolean matchByMarkId =
                if (matchByName || matchByTemplateId || matchByMarkId) {
                    found = true;
                    final String msgIdentifier = matchByName ? "markconfiguration with name " + srcAppearance.getName()
                            : matchByTemplateId ? "markconfiguration with markTemplateID " + markTemplateId
                                    : matchByMarkId ? "markconfiguration with markId " + markConfiguration.getMarkId()
                                            : "unknown";
                    if (markTemplateId != null && trgtMarkConfiguration.getMarkTemplateId() != null) {
                        assertEquals("markTemplateId is different for " + msgIdentifier, markTemplateId,
                                trgtMarkConfiguration.getMarkTemplateId());
                    }
                    final String srcAssociatedRole = markConfiguration.getAssociatedRole();
                    if (srcAssociatedRole != null) {
                        assertEquals("associated role is different for " + msgIdentifier, srcAssociatedRole,
                                trgtMarkConfiguration.getAssociatedRole());
                    }
                    if (matchByName) {
                        assertEquals("shortName is different for " + msgIdentifier, srcAppearance.getShortName(),
                                trgtAppearance.getShortName());
                    }
                    boolean hasDeviceUUID = false;
                    if (srcPositioning != null) {
                        final Positioning trgtPositioning = trgtMarkConfiguration.getEffectivePositioning();
                        assertEquals("position.lat is different for " + msgIdentifier, srcPositioning.getLatitudeDeg(),
                                trgtPositioning.getLatitudeDeg());
                        assertEquals("position.lng is different for " + msgIdentifier,
                                trgtPositioning.getLongitudeDeg(), trgtPositioning.getLongitudeDeg());
                        if (srcPositioning.getDeviceId() != null) {
                            hasDeviceUUID = true;
                            assertEquals("position.type is wrong for " + msgIdentifier, "DEVICE",
                                    trgtPositioning.getType());
                            assertNull("deviceId should be empty for " + msgIdentifier, trgtPositioning.getDeviceId());
                        }
                    }
                    if (markConfiguration.isStoreToInventory()) {
                        final UUID markPropertiesId = trgtMarkConfiguration.getMarkPropertiesId();
                        assertNotNull(markPropertiesId);
                        final MarkProperties markProperties = markPropertiesApi.getMarkProperties(ctx,
                                markPropertiesId);
                        assertNotNull(markProperties);
                        if (matchByName) {
                            assertEquals(srcAppearance.getName(), markProperties.getName());
                            assertEquals(srcAppearance.getShortName(), markProperties.getShortName());
                        }
                        assertEquals(markProperties.hasDevice(), hasDeviceUUID);
                    }
                }
            }
            assertTrue("No matching markconfiguration found for markconfiguration " + markConfiguration.getId(), found);
        }
        for (final WaypointWithMarkConfiguration srcWaypoint : srcWaypoints) {
            final String controlPointName = srcWaypoint.getControlPointName();
            if (controlPointName != null) {
                boolean found = false;
                for (final WaypointWithMarkConfiguration trgtWaypoint : trgtWaypoints) {
                    if (controlPointName.equals(trgtWaypoint.getControlPointName())) {
                        found = true;
                        assertEquals("Waypoint controlpoint shortname is different",
                                srcWaypoint.getControlPointShortName(), trgtWaypoint.getControlPointShortName());
                    }
                }
                assertTrue("Waypoint with control point name " + controlPointName + " not found", found);
            }
        }
        final RepeatablePart srcRepeatablePart = srcCourseConfiguration.getRepeatablePart();
        if (srcRepeatablePart != null) {
            final RepeatablePart tgrtRepeatablePart = trgtCourseConfiguration.getRepeatablePart();
            assertEquals("repeatablePart.start is different",
                    srcRepeatablePart.getZeroBasedIndexOfRepeatablePartStart(),
                    new Integer(tgrtRepeatablePart.getZeroBasedIndexOfRepeatablePartStart()));
            assertEquals("repeatablePart.end is different", srcRepeatablePart.getZeroBasedIndexOfRepeatablePartEnd(),
                    tgrtRepeatablePart.getZeroBasedIndexOfRepeatablePartEnd());
        }
    }

    @Test
    public void testCreateCourseFromCourseTemplateWithoutChanges() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final int numberOfLaps = 2;
        final CourseTemplateDataFactory ctdf = new CourseTemplateDataFactory(ctx);

        final Map<MarkTemplate, String> associatedRoles = new HashMap<>();
        associatedRoles.put(ctdf.sb, "Startboat");
        associatedRoles.put(ctdf.pe, "Pinend");
        associatedRoles.put(ctdf.b1, "1");
        associatedRoles.put(ctdf.b4s, "4s");
        associatedRoles.put(ctdf.b4p, "4p");

        final CourseTemplate createdCourseTemplate = courseTemplateApi.createCourseTemplate(ctx,
                ctdf.constructCourseTemplate(new Pair<>(1, 3), numberOfLaps, associatedRoles));

        CourseConfiguration courseConfiguration = courseConfigurationApi.createCourseConfigurationFromCourseTemplate(
                ctx, createdCourseTemplate.getId(), /* optionalRegattaName */ null, /* tags */ null);

        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        courseConfigurationApi.createCourse(ctx, courseConfiguration, regattaName, race.getRaceName(), "Default");
        CourseConfiguration createdCourseAsConfiguration = courseConfigurationApi
                .createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName(), "Default", null);

        assertEquals(6, Util.size(createdCourseAsConfiguration.getMarkConfigurations()));
        assertEquals(createdCourseTemplate.getId(), createdCourseAsConfiguration.getOptionalCourseTemplateId());
        assertCourseConfigurationCompared(ctx, courseConfiguration, createdCourseAsConfiguration);
        assertConsistentCourseConfiguration(createdCourseAsConfiguration);
        assertEquals(numberOfLaps, createdCourseAsConfiguration.getNumberOfLaps());
    }

    @Test
    public void testCreateCourseFromFreestyleConfigurationWithPositioning() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        MarkConfiguration sb = MarkConfiguration.createFreestyle(null, null, "role_sb", "startboat", "sb", null, null,
                null, null);
        sb.setFixedPosition(5.5, 7.1);
        MarkConfiguration pe = MarkConfiguration.createFreestyle(null, null, "role_pe", "pin end", "pe", null, null,
                null, null);
        UUID randomDeviceId = UUID.randomUUID();
        pe.setTrackingDeviceId(randomDeviceId);
        MarkConfiguration bl = MarkConfiguration.createFreestyle(null, null, "role_bl", "1", null, "#0000FF", null,
                null, null);
        WaypointWithMarkConfiguration wp1 = new WaypointWithMarkConfiguration("start/end", "s/e",
                PassingInstruction.Line, Arrays.asList(sb.getId(), pe.getId()));
        WaypointWithMarkConfiguration wp2 = new WaypointWithMarkConfiguration(null, null, PassingInstruction.Port,
                Arrays.asList(bl.getId()));

        CourseConfiguration courseConfiguration = new CourseConfiguration("my-freestyle-course",
                Arrays.asList(sb, pe, bl), Arrays.asList(wp1, wp2, wp1));

        courseConfigurationApi.createCourse(ctx, courseConfiguration, regattaName, race.getRaceName(), "Default");
        CourseConfiguration createdCourseAsConfiguration = courseConfigurationApi
                .createCourseConfigurationFromCourse(ctx, regattaName, race.getRaceName(), "Default", null);

        assertConsistentCourseConfiguration(createdCourseAsConfiguration);
        assertCourseConfigurationCompared(ctx, courseConfiguration, createdCourseAsConfiguration);
    }

    @Test
    public void testCreateCourseTemplateWithPositioningIncluded() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        MarkConfiguration sb = MarkConfiguration.createFreestyle(null, null, "role_sb", "startboat", "sb", null, null,
                null, null);
        sb.setFixedPosition(5.5, 7.1);
        MarkConfiguration pe = MarkConfiguration.createFreestyle(null, null, "role_pe", "pin end", "pe", null, null,
                null, null);
        UUID randomDeviceId = UUID.randomUUID();
        pe.setTrackingDeviceId(randomDeviceId);
        MarkConfiguration bl = MarkConfiguration.createFreestyle(null, null, "role_bl", "1", null, "#0000FF", null,
                null, null);
        WaypointWithMarkConfiguration wp1 = new WaypointWithMarkConfiguration("start/end", "s/e",
                PassingInstruction.Line, Arrays.asList(sb.getId(), pe.getId()));
        WaypointWithMarkConfiguration wp2 = new WaypointWithMarkConfiguration(null, null, PassingInstruction.Port,
                Arrays.asList(bl.getId()));

        CourseConfiguration courseConfiguration = new CourseConfiguration("my-freestyle-course",
                Arrays.asList(sb, pe, bl), Arrays.asList(wp1, wp2, wp1));

        CourseConfiguration courseConfigurationResult = courseConfigurationApi.createCourseTemplate(ctx,
                courseConfiguration, null);

        assertConsistentCourseConfiguration(courseConfigurationResult);
        assertCourseConfigurationCompared(ctx, courseConfiguration, courseConfigurationResult);
        // TODO assert positioning is contained in result and identical to the given values
    }

    @Test
    public void testCreateCourseConfigurationFromTemplate() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final List<MarkTemplate> markTemplates = new ArrayList<>();
        MarkTemplate mt1 = markTemplateApi.createMarkTemplate(ctx, "mark template 1", "mt1", "#FFFFFF", "Cylinder",
                "Checkered", MarkType.BUOY.name());
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
        assertConsistentCourseConfiguration(courseConfiguration);
    }

    @Test
    public void testCreateCourseConfigurationWithStoreToInvventory() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        MarkConfiguration sb = MarkConfiguration.createFreestyle(null, null, "role_sb", "startboat", "sb", null, null,
                null, null);
        sb.setFixedPosition(5.5, 7.1);
        sb.setStoreToInventory(true);
        MarkConfiguration pe = MarkConfiguration.createFreestyle(null, null, "role_pe", "pin end", "pe", null, null,
                null, null);
        pe.setStoreToInventory(true);
        UUID randomDeviceId = UUID.randomUUID();
        pe.setTrackingDeviceId(randomDeviceId);
        MarkConfiguration bl = MarkConfiguration.createFreestyle(null, null, "role_bl", "1", null, "#0000FF", null,
                null, null);
        MarkTemplate mtb2 = markTemplateApi.createMarkTemplate(ctx, "mark template 1", "mt1", "#FFFFFF", "Cylinder",
                "Checkered", MarkType.BUOY.name());
        MarkConfiguration b2 = MarkConfiguration.createMarkTemplateBased(mtb2.getId(), "role_b2");
        b2.setStoreToInventory(true);

        eventApi.createEvent(ctx, "testregatta", "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, "testregatta", null, 1)[0];
        Mark mark = markApi.addMarkToRegatta(ctx, "testregatta", "mymark");
        markApi.addMarkFix(ctx, "testregatta", race.getRaceName(), "Default", mark.getMarkId(),
                /* markTemplateId */ UUID.randomUUID(), /* markPropertiesId */ UUID.randomUUID(), 9.12, .599,
                currentTimeMillis());
        MarkConfiguration b3 = MarkConfiguration.createMarkBased(mark.getMarkId(), "role_b3");
        b3.setStoreToInventory(true);

        WaypointWithMarkConfiguration wp1 = new WaypointWithMarkConfiguration("start/end", "s/e",
                PassingInstruction.Line, Arrays.asList(sb.getId(), pe.getId()));
        WaypointWithMarkConfiguration wp2 = new WaypointWithMarkConfiguration(null, null, PassingInstruction.Port,
                Arrays.asList(bl.getId()));
        WaypointWithMarkConfiguration wp3 = new WaypointWithMarkConfiguration(null, null,
                PassingInstruction.Single_Unknown, Arrays.asList(b2.getId()));
        WaypointWithMarkConfiguration wp4 = new WaypointWithMarkConfiguration(null, null,
                PassingInstruction.Single_Unknown, Arrays.asList(b3.getId()));

        CourseConfiguration courseConfiguration = new CourseConfiguration("my-course",
                Arrays.asList(sb, pe, bl, b2, b3), Arrays.asList(wp1, wp2, wp1, wp3, wp4));

        CourseConfiguration courseConfigurationResult = courseConfigurationApi.createCourseTemplate(ctx,
                courseConfiguration, "testregatta");
        assertCourseConfigurationCompared(ctx, courseConfiguration, courseConfigurationResult);
    }

    @Test
    public void testCreateCourseConfigurationFromCourse() {
        final String regattaName = "test";
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];
        LeaderboardApi.startRaceLogTracking(ctx, regattaName, race.getRaceName(), "Default");

        CourseConfiguration createdCourseConfiguration = courseConfigurationApi.createCourseConfigurationFromCourse(ctx,
                regattaName, race.getRaceName(), "Default", /* tags */ null);
        assertConsistentCourseConfiguration(createdCourseConfiguration);
    }

    @Test
    public void testCreateCourseFromCourseConfiguration() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        final String regattaName = "test";
        eventApi.createEvent(ctx, regattaName, "", CompetitorRegistrationType.CLOSED, "");
        final RaceColumn race = regattaApi.addRaceColumn(ctx, regattaName, /* prefix */ null, 1)[0];

        courseConfigurationApi.createCourse(ctx, createSimpleCourseConfiguration(ctx), regattaName, race.getRaceName(),
                "Default");
    }

    @Test
    public void testCreateCourseTemplateFromCourseConfiguration() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        CourseConfiguration courseConfiguration = courseConfigurationApi.createCourseTemplate(ctx,
                createSimpleCourseConfiguration(ctx), /* optionalRegattaName */ null);
        assertConsistentCourseConfiguration(courseConfiguration);
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
        WaypointWithMarkConfiguration waypoint = new WaypointWithMarkConfiguration("test", null,
                PassingInstruction.Line, markConfigurationIds);
        waypoints.add(waypoint);

        return new CourseConfiguration("test-course", markConfigurations, waypoints);
    }
}
