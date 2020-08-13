package com.sap.sailing.selenium.test.adminconsole.usermanagement;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupDefinitionCreationDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.usergroups.UserGroupDefinitionsPanelPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestUserGroupCreation extends AbstractSeleniumTest {
    private static final String TEST_GROUP_NAME = "testGroup";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void testOpenCreateBoatDialog() {
        final UserGroupDefinitionsPanelPO userGroupManagementPanel = goToUserGroupDefinitionsPanel();
        assertNull(userGroupManagementPanel.findGroup(TEST_GROUP_NAME));
        final UserGroupDefinitionCreationDialogPO createUserdialog = userGroupManagementPanel.getCreateGroupDialog();
        assertNotNull(createUserdialog);
        createUserdialog.setName(TEST_GROUP_NAME);
        createUserdialog.clickOkButtonOrThrow();
        assertNotNull(userGroupManagementPanel.findGroup(TEST_GROUP_NAME));
    }

    private UserGroupDefinitionsPanelPO goToUserGroupDefinitionsPanel() {
        final AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        return adminConsole.goToUserGroupDefinitions();
    }
}
