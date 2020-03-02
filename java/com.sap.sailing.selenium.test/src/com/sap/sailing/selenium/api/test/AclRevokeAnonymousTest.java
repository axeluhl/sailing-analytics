package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.pages.adminconsole.AclPopupPO;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO.EventEntryPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class AclRevokeAnonymousTest extends AbstractSeleniumTest {

    private ApiContext ownerCtx;

    private final SecurityApi securityApi = new SecurityApi();
    private final EventApi eventApi = new EventApi();
    
    private static final String EVENT_NAME = "Super exclusive regatta - invited competitors only";
    private static final String BOAT_CLASS = "GC 32";
    private AdminConsolePage adminConsole;
    
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
        final Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED,
                "Some special place");
    }

    @Test
    public void test() {
        EventConfigurationPanelPO eventPanel = adminConsole.goToEvents();
        EventEntryPO eventEntry = eventPanel.getEventEntry(EVENT_NAME);
        AclPopupPO aclPopup = eventEntry.openAclPopup();
        aclPopup.addUserGroup(""); // add empty user group -> anonymous group
        WebElement deniedInput = aclPopup.getDeniedActionsInput();
        WebElement allowedInput = aclPopup.getAllowedActionsInput();
        Assert.assertThat(deniedInput.isEnabled(), Matchers.equalTo(false));
        Assert.assertThat(allowedInput.isEnabled(), Matchers.equalTo(true));
        
        aclPopup.addUserGroup("admin-tenant");
        Assert.assertThat(deniedInput.isEnabled(), Matchers.equalTo(true));
        Assert.assertThat(allowedInput.isEnabled(), Matchers.equalTo(true));
    }

}
