package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserRoleDefinitionPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestRoleCreation extends AbstractSeleniumTest {
    private static final String TEST_USER_NAME = "admin";
    private static final String TEST_ROLE = "spectator";
    private static final String TEST_GROUP = "";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testOpenCreateBoatDialog() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        assertNotNull(userManagementPanel.findUser(TEST_USER_NAME));
        userManagementPanel.selectUser(TEST_USER_NAME);
        final UserRoleDefinitionPanelPO userRolesPO = userManagementPanel.getUserRoles();
        assertNull(userRolesPO.findRole(TEST_ROLE));
        userRolesPO.enterNewRoleValues(TEST_ROLE, null, TEST_USER_NAME);
        userRolesPO.clickAddButtonOrThrow();
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNotNull(userRolesPO.findRole(TEST_ROLE + ":"+ TEST_GROUP + ":" + TEST_USER_NAME));
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
