package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.CreateUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestUserCreation extends AbstractSeleniumTest {
    private static final String TEST_USER_PASSWORD = "test1";
    private static final String TEST_USER_WRONG_PASSWORD = "test2";
    private static final String TEST_USER_MAIL = "test@test.com";
    private static final String TEST_USER_NAME = "testUser";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testOpenCreateBoatDialog() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        assertNull(userManagementPanel.findUser(TEST_USER_NAME));
        final CreateUserDialogPO createUserdialog = userManagementPanel.getCreateUserDialog();
        assertNotNull(createUserdialog);
        createUserdialog.setValues(TEST_USER_NAME, TEST_USER_MAIL, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
        createUserdialog.clickOkButtonOrThrow();
        assertNotNull(userManagementPanel.findUser(TEST_USER_NAME));
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
