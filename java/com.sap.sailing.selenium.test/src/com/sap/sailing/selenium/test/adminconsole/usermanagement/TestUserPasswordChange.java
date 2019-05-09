package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.ChangePasswordDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.EditUserDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usermanagement.UserManagementPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestUserPasswordChange extends AbstractSeleniumTest {
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testOpenCreateBoatDialog() {
        final UserManagementPanelPO userManagementPanel = goToUserManagementPanel();
        final EditUserDialogPO editUserDialog = userManagementPanel.getEditUserDialog("admin");
        assertNotNull(editUserDialog);
        editUserDialog.clickChangePasswordButton();
        final ChangePasswordDialogPO changePasswordDialog = userManagementPanel.getChangePasswordDialog();
        changePasswordDialog.setNewPassword("supersecure");
        changePasswordDialog.clickOkButtonOrThrow();
    }

    private UserManagementPanelPO goToUserManagementPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserManagement();
    }
}
