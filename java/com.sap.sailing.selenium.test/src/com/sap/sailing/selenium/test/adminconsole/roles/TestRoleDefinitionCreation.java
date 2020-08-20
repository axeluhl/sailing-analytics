package com.sap.sailing.selenium.test.adminconsole.roles;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.roles.RoleDefinitionCreationAndUpdateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.roles.RoleDefinitionsPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestRoleDefinitionCreation extends AbstractSeleniumTest {
    private static final String TEST_ROLE = "test";
    private static final String TEST_PERMISSION = "USER:READ";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testRoleCreation() throws InterruptedException {
        final RoleDefinitionsPanelPO roleManagementPanel = goToRoleDefinitionPanel();
        assertNull(roleManagementPanel.findRole(TEST_ROLE));
        createRole(roleManagementPanel);
        assertNotNull(roleManagementPanel.findRole(TEST_ROLE));
    }
    
    @Test
    public void testRoleDeletion() throws InterruptedException {
        final RoleDefinitionsPanelPO roleManagementPanel = goToRoleDefinitionPanel();
        createRole(roleManagementPanel);
        roleManagementPanel.deleteRole(TEST_ROLE);
        getWebDriver().switchTo().alert().accept();
        assertNull(roleManagementPanel.findRole(TEST_ROLE));
    }

    private void createRole(final RoleDefinitionsPanelPO roleManagementPanel) {
        final RoleDefinitionCreationAndUpdateDialogPO createRoleDialog = roleManagementPanel.getCreateRoleDialog();
        assertNotNull(createRoleDialog);
        createRoleDialog.setName(TEST_ROLE);
        createRoleDialog.addPermission(TEST_PERMISSION);
        createRoleDialog.clickOkButtonOrThrow();
    }

    private RoleDefinitionsPanelPO goToRoleDefinitionPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToRoleDefinitions();
    }
}
