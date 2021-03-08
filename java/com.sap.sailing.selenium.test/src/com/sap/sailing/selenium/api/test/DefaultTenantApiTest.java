package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.UserGroupApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class DefaultTenantApiTest extends AbstractSeleniumTest {

    private static final String TENANT_GROUP_NAME = "NewGroup";
    private static final String EVENT_NAME = "TestEvent2";
    private AdminConsolePage adminConsole;
    private UUID tenantGroupId;
    private String userToken;

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        userToken = SecurityApi.createUser("donald", "daisy0815").run().getAccessToken();
        adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
        tenantGroupId = UserGroupApi.createUserGroup(TENANT_GROUP_NAME).auth(userToken).run().getGroupId();
    }

    @Test
    public void testEventApiWithUserTenant() {
        EventApi.create(EVENT_NAME, "GC 32", CompetitorRegistrationType.CLOSED, "somewhere").auth(userToken).run();
        assertEquals("donald-tenant", adminConsole.goToEvents().getEventEntry(EVENT_NAME).getColumnContent("Group"));
    }

    @Test
    public void testEventApiWithDefaultTenant() {
        EventApi.create(EVENT_NAME, "GC 32", CompetitorRegistrationType.CLOSED, "somewhere")
                .header("tenantGroupId", tenantGroupId.toString()).auth(userToken).run();
        assertEquals(TENANT_GROUP_NAME, adminConsole.goToEvents().getEventEntry(EVENT_NAME).getColumnContent("Group"));
    }
}
