package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.WildcardPermissionPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestPermissionCreation extends AbstractSeleniumTest {
    private static final String TEST_USER_NAME = "admin";
    private static final String TEST_PERMISSION = "USER:READ";

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
        final WildcardPermissionPanelPO wildcardPermissionPanelPO = userManagementPanel.getUserPermissionsPO();
        assertNull(wildcardPermissionPanelPO.findPermission(TEST_PERMISSION));
        wildcardPermissionPanelPO.enterNewPermissionValue(TEST_PERMISSION);
        wildcardPermissionPanelPO.clickAddButtonOrThrow();
        userManagementPanel.selectUser(TEST_USER_NAME);
        assertNotNull(wildcardPermissionPanelPO.findPermission(TEST_PERMISSION));
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
