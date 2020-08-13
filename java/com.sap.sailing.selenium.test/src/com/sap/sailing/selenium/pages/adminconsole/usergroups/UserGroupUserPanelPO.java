package com.sap.sailing.selenium.pages.adminconsole.usergroups;

import org.openqa.selenium.ElementNotSelectableException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;
import com.sap.sailing.selenium.pages.gwt.SuggestBoxPO;

public class UserGroupUserPanelPO extends PageArea {
    private static final String TABLE_PERMISSION_COLUMN = "Username";
    @FindBy(how = BySeleniumId.class, using = "AddUserButton")
    private WebElement addButton;
    @FindBy(how = BySeleniumId.class, using = "UserSuggestion")
    private WebElement permissionInput;
    @FindBy(how = BySeleniumId.class, using = "UserGroupUserDTOTable")
    private WebElement permissionTable;
    
    public UserGroupUserPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserTable() {
        return new GenericCellTablePO<>(this.driver, this.permissionTable, DataEntryPO.class);
    }

    public DataEntryPO findUser(final String userName) {
        final CellTablePO<DataEntryPO> table = getUserTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent(TABLE_PERMISSION_COLUMN);
            if (userName.equals(name)) {
                return entry;
            }
        }
        return null;
    }
    
    public void addUser(String name) {
        enterNewUser(name);
        clickAddButtonOrThrow();
        waitUntil(() -> findUser(name) != null);
    }

    public void enterNewUser(String name) {
        SuggestBoxPO.create(driver, permissionInput).appendText(name);
    }
    
    public void clickAddButtonOrThrow() {
        if (!addButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addButton.click();
        }
    }
    
    public void clickAddButtonAndExpectPermissionError() {
        if (!addButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addButton.click();
        }
        waitForAlertContainingMessageAndAccept("Not permitted to add user");
    }
}
