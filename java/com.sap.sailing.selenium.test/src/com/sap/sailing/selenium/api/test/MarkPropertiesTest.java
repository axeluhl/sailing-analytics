package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.HttpException;
import com.sap.sailing.selenium.api.coursetemplate.MarkProperties;
import com.sap.sailing.selenium.api.coursetemplate.MarkPropertiesApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MarkPropertiesTest extends AbstractSeleniumTest {

    private static final String MARK_PROPERTIES_NAME = "testname";
    private static final String MARK_PROPERTIES_SHORTNAME = "testshortname";
    private static final String MARK_PROPERTIES_COLOR = "#FF0000";
    private static final String MARK_PROPERTIES_SHAPE = "shape";
    private static final String MARK_PROPERTIES_PATTERN = "pattern";
    private static final String MARK_PROPERTIES_TYPE = "STARTBOAT";
    private static final List<String> MARK_PROPERTIES_TAGS;
    private static final Double MARK_PROPERTIES_LATDEG = 41.456;
    private static final Double MARK_PROPERTIES_LONDEG = 9.123;

    static {
        final List<String> tags = new ArrayList<>();
        tags.add("tag1");
        MARK_PROPERTIES_TAGS = tags;
    }

    private final MarkPropertiesApi markPropertiesApi = new MarkPropertiesApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void createMarkPropertyWithDeviceUuidTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final UUID deviceUuid = randomUUID();
        MarkProperties markProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, deviceUuid.toString(), MARK_PROPERTIES_COLOR, "shape", "pattern",
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, null, null);
        assertNotNull("read: no MarkProperties returnded", markProperties);
        assertDefaultValues(markProperties);
        assertTrue(markProperties.hasDevice());
    }

    @Test
    public void createAndGetMarkPropertiesWithoutDeviceUuidTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties createdMarkProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
        assertNotNull("create: no MarkProperties returnded", createdMarkProperties);
        assertNotNull("create: MarkProperties.id is missing", createdMarkProperties.getId());

        MarkProperties foundMarkProperties = markPropertiesApi.getMarkProperties(ctx, createdMarkProperties.getId());
        assertDefaultValues(foundMarkProperties);
        assertEquals("read: MarkProperties.latDeg is different", MARK_PROPERTIES_LATDEG, foundMarkProperties.getLatDeg());
        assertEquals("read: MarkProperties.lonDeg is different", MARK_PROPERTIES_LONDEG, foundMarkProperties.getLonDeg());
    }

    @Test
    public void createSeveralMarkPropertiesAndGetAll() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties markProperties1 = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
        MarkProperties markProperties2 = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);

        final Iterable<MarkProperties> markPropertiesResult = markPropertiesApi.getAllMarkProperties(ctx,
                MARK_PROPERTIES_TAGS);
        for (MarkProperties markProperties : markPropertiesResult) {
            assertTrue(markProperties.getId().equals(markProperties1.getId())
                    || markProperties.getId().equals(markProperties2.getId()));
        }
    }

    @Ignore
    public void createAndUpdateMarkProperties() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties createdMarkProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
        final UUID deviceUuid = randomUUID();
        MarkProperties updatedMarkProperties = markPropertiesApi.updateMarkPropertiesPositioning(ctx,
                createdMarkProperties.getId(), deviceUuid, 1.0, 2.0);
        assertTrue("read: MarkProperties.latDeg is different", 1.0 == updatedMarkProperties.getLatDeg().doubleValue());
        assertTrue("read: MarkProperties.lonDeg is different", 2.0 == updatedMarkProperties.getLonDeg().doubleValue());
    }

    private void assertDefaultValues(MarkProperties markProperties) {
        assertNotNull("read: MarkProperties.id is missing", markProperties.getId());
        assertEquals("read: MarkProperties.name is different", MARK_PROPERTIES_NAME, markProperties.getName());
        assertEquals("read: MarkProperties.shortName is different", MARK_PROPERTIES_SHORTNAME,
                markProperties.getShortName());
        assertEquals("read: MarkProperties.color is different", MARK_PROPERTIES_COLOR, markProperties.getColor());
        assertEquals("read: MarkProperties.shape is different", MARK_PROPERTIES_SHAPE, markProperties.getShape());
        assertEquals("read: MarkProperties.pattern is different", MARK_PROPERTIES_PATTERN, markProperties.getPattern());
        assertEquals("read: MarkProperties.type is different", MARK_PROPERTIES_TYPE,
                markProperties.getMarkType().name());
    }

    @Test(expected = HttpException.NotFound.class)
    public void createAndDeleteMarkPropertiesTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties createdMarkProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
        assertNotNull("create: no MarkProperties returnded", createdMarkProperties);
        assertNotNull("create: MarkProperties.id is missing", createdMarkProperties.getId());

        markPropertiesApi.deleteMarkProperties(ctx, createdMarkProperties.getId());
        markPropertiesApi.getMarkProperties(ctx, createdMarkProperties.getId());
    }

    @Test
    public void testExclusionOfDeviceUuidAndFixedPositioning() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties createdMarkProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
        assertEquals(MARK_PROPERTIES_LATDEG, createdMarkProperties.getLatDeg());
        assertEquals(MARK_PROPERTIES_LONDEG, createdMarkProperties.getLonDeg());
        assertFalse(createdMarkProperties.hasDevice());

        final UUID deviceUuid = UUID.randomUUID();
        MarkProperties updatedMarkProperties = markPropertiesApi.updateMarkProperties(ctx,
                createdMarkProperties.getId(), MARK_PROPERTIES_NAME, MARK_PROPERTIES_SHORTNAME, deviceUuid.toString(),
                MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN, MARK_PROPERTIES_TYPE,
                MARK_PROPERTIES_TAGS, null, null);
        assertNull(updatedMarkProperties.getLatDeg());
        assertNull(updatedMarkProperties.getLonDeg());
        assertTrue(updatedMarkProperties.hasDevice());
    }
    
    @Test(expected = HttpException.class)
    public void testOverlapOfDeviceUuidAndFixedPositioning() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final UUID deviceUuid = UUID.randomUUID();
        markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, deviceUuid.toString(), MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE, MARK_PROPERTIES_TAGS, MARK_PROPERTIES_LATDEG, MARK_PROPERTIES_LONDEG);
    }
}
