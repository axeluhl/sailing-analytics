package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
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
        final UUID deviceUuid = randomUUID();
        markPropertiesApi.createMarkProperties(ctx, "test", "testshort", deviceUuid.toString(), "#FF0000", "shape",
                "pattern", "STARTBOAT");
    }
}
