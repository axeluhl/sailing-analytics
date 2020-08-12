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
    private WebElement addButton;
    @FindBy(how = BySeleniumId.class, using = "suggestPermission")
    private WebElement permissionInput;
    @FindBy(how = BySeleniumId.class, using = "WildcardPermissionWithSecurityDTOTable")
    private WebElement permissionTable;
    
    public WildcardPermissionPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    private CellTablePO<DataEntryPO> getPermissionTable() {
        return new GenericCellTablePO<>(this.driver, this.permissionTable, DataEntryPO.class);
    }

    public DataEntryPO findPermission(final String permissionName) {
        final CellTablePO<DataEntryPO> table = getPermissionTable();
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
        SuggestBoxPO.create(driver, permissionInput).appendText(permissionName);
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
        waitForAlertContainingMessageAndAccept("Not permitted to grant permission");
    }
}
