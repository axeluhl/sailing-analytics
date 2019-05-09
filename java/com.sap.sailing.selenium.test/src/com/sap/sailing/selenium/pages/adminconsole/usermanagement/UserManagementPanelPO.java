package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.ActionsHelper;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class UserManagementPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "UsersTable")
    private WebElement userTable;

    public UserManagementPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserTable() {
        return new GenericCellTablePO<>(this.driver, this.userTable, DataEntryPO.class);
    }

    private DataEntryPO findUser(final String username) {
        final CellTablePO<DataEntryPO> table = getUserTable();

        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent("Username");
            if (username.equals(name)) {
                return entry;
            }
        }

        return null;
    }

    public EditUserDialogPO getEditUserDialog(final String username) {
        DataEntryPO entry = findUser(username);
        if (entry != null) {
            final WebElement action = ActionsHelper.findUpdateAction(entry.getWebElement());
            action.click();
            final WebElement dialog = findElementBySeleniumId(this.driver, "UserEditDialog");
            return new EditUserDialogPO(this.driver, dialog);
        }
        return null;
    }

    public ChangePasswordDialogPO getChangePasswordDialog() {
        final WebElement dialog = findElementBySeleniumId(this.driver, "ChangePasswordDialog");
        return new ChangePasswordDialogPO(this.driver, dialog);
    }

}
