package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.UserGroupApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class DefaultTenantApiTest extends AbstractSeleniumTest {

    private static final String TENANT_GROUP_NAME = "NewGroup";
    private final SecurityApi securityApi = new SecurityApi();
    private final UserGroupApi userGroupApi = new UserGroupApi();
    private ApiContext adminSecurityCtx;
    private AdminConsolePage adminConsole;
    private UUID tenantGroupId;

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
        tenantGroupId = userGroupApi.createUserGroup(adminSecurityCtx, TENANT_GROUP_NAME).getGroupId();
    }

    @Test
    public void testEventApiWithUserTenant() {
        EventApi.create("testEvent2", "GC 32", CompetitorRegistrationType.CLOSED, "somewhere")
                .auth("donald", "daisy0815").run();
        assertEquals("donald-tenant", adminConsole.goToEvents().getEventEntry("testEvent").getColumnContent("Group"));
    }

    @Test
    public void testEventApiWithDefaultTenant() {
        EventApi.create("testEvent2", "GC 32", CompetitorRegistrationType.CLOSED, "somewhere")
                .header("tenantGroupId", tenantGroupId.toString()).auth("donald", "daisy0815").run();
        assertEquals(TENANT_GROUP_NAME,
                adminConsole.goToEvents().getEventEntry("testEvent2").getColumnContent("Group"));
    }
}
