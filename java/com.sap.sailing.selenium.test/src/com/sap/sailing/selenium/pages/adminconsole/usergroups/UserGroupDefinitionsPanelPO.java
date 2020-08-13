package com.sap.sailing.selenium.pages.adminconsole.usergroups;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class UserGroupDefinitionsPanelPO extends PageArea {
    private static final String CREATE_ROLE_DIALOG = "UserGroupDefinitionCreationDialog";
    
    @FindBy(how = BySeleniumId.class, using = "UserGroupWithSecurityDTOTable")
    private WebElement groupTable;
    
    @FindBy(how = BySeleniumId.class, using = "CreateGroupButton")
    private WebElement createGroupButton;
    
    public UserGroupDefinitionsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getRoleTable() {
        return new GenericCellTablePO<>(this.driver, this.groupTable, DataEntryPO.class);
    }

    public DataEntryPO findRole(final String username) {
        final CellTablePO<DataEntryPO> table = getRoleTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent("Group Name");
            if (username.equals(name)) {
                return entry;
            }
        }
        return null;
    }
    public UserGroupDefinitionCreationDialogPO getCreateGroupDialog() {
        createGroupButton.click();
        final WebElement dialog = findElementBySeleniumId(this.driver, CREATE_ROLE_DIALOG);
        return new UserGroupDefinitionCreationDialogPO(this.driver, dialog);
    }
}
