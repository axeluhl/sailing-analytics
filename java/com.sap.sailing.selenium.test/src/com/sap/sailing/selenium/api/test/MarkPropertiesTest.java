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
import com.sap.sailing.selenium.api.coursetemplate.MarkProperties;
import com.sap.sailing.selenium.api.coursetemplate.MarkPropertiesApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class MarkPropertiesTest extends AbstractSeleniumTest {

    private final MarkPropertiesApi markPropertiesApi = new MarkPropertiesApi();

    @Before
    public void setUp() {
        clearState(getContextRoot(), /* headless */ true);
    }

    @Test
    public void createMarkPropertyWithDeviceUuidTest() {
        final ApiContext ctx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String markPropertiesName = "testname";
        final String markPropertiesShortName = "testshortname";
        final UUID deviceUuid = randomUUID();
        MarkProperties markProperties = markPropertiesApi.createMarkProperties(ctx, markPropertiesName,
                markPropertiesShortName, deviceUuid.toString(), "#FF0000", "shape", "pattern", "STARTBOAT");
        assertNotNull("read: no MarkProperties returnded", markProperties);
        assertNotNull("read: MarkProperties.id is missing");
        assertEquals("read: MarkProperties.name is different", markPropertiesName, markProperties.getName());
        assertEquals("read: MarkProperties.shortName is different", markPropertiesShortName,
                markProperties.getShortName());
    }
}
