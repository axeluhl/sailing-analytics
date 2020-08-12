package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

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

public class WildcardPermissionPanelPO extends PageArea {
    private static final String TABLE_PERMISSION_COLUMN = "Permission";
    @FindBy(how = BySeleniumId.class, using = "addPermissionButton")
    private WebElement addRoleButton;
    @FindBy(how = BySeleniumId.class, using = "suggestPermission")
    private WebElement roleNameInput;
    @FindBy(how = BySeleniumId.class, using = "WildcardPermissionWithSecurityDTOTable")
    private WebElement roleTable;
    
    public WildcardPermissionPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getUserTable() {
        return new GenericCellTablePO<>(this.driver, this.roleTable, DataEntryPO.class);
    }

    public DataEntryPO findPermission(final String permissionName) {
        final CellTablePO<DataEntryPO> table = getUserTable();
        for (DataEntryPO entry : table.getEntries()) {
            final String name = entry.getColumnContent(TABLE_PERMISSION_COLUMN);
            if (permissionName.equals(name)) {
                return entry;
            }
        }
        return null;
    }
    
    public void addPermission(String permissionName) {
        enterNewPermissionValue(permissionName);
        clickAddButtonOrThrow();
    }

    public void enterNewPermissionValue(String permissionName) {
        SuggestBoxPO.create(driver, roleNameInput).appendText(permissionName);
    }
    
    public void clickAddButtonOrThrow() {
        if (!addRoleButton.isEnabled()) {
            throw new ElementNotSelectableException("Add Button was disabled");
        } else {
            addRoleButton.click();
        }
    }
}
