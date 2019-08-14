package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
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
                MARK_PROPERTIES_TYPE);
        assertNotNull("read: no MarkProperties returnded", markProperties);
        assertDefaultValues(markProperties);
    }

    @Test
    public void createAndGetMarkPropertiesWithoutDeviceUuidTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        MarkProperties createdMarkProperties = markPropertiesApi.createMarkProperties(ctx, MARK_PROPERTIES_NAME,
                MARK_PROPERTIES_SHORTNAME, null, MARK_PROPERTIES_COLOR, MARK_PROPERTIES_SHAPE, MARK_PROPERTIES_PATTERN,
                MARK_PROPERTIES_TYPE);
        assertNotNull("create: no MarkProperties returnded", createdMarkProperties);
        assertNotNull("create: MarkProperties.id is missing", createdMarkProperties.getId());

        MarkProperties foundMarkProperties = markPropertiesApi.getMarkProperties(ctx, createdMarkProperties.getId());
        assertDefaultValues(foundMarkProperties);
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
                MARK_PROPERTIES_TYPE);
        assertNotNull("create: no MarkProperties returnded", createdMarkProperties);
        assertNotNull("create: MarkProperties.id is missing", createdMarkProperties.getId());

        markPropertiesApi.deleteMarkProperties(ctx, createdMarkProperties.getId());
        markPropertiesApi.getMarkProperties(ctx, createdMarkProperties.getId());
    }
}
