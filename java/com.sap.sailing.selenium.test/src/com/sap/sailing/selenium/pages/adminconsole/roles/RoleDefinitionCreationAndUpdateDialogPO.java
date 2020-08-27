package com.sap.sailing.selenium.pages.adminconsole.roles;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.adminconsole.common.ErrorDialogPO;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.StringListEditorCompositePO;
import com.sap.sailing.selenium.pages.gwt.StringListEditorCompositePO.ValueEntryPO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class RoleDefinitionCreationAndUpdateDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "name")
    private WebElement roleNameInput;
    @FindBy(how = BySeleniumId.class, using = "permissions")
    private WebElement permissionsInput;

    protected RoleDefinitionCreationAndUpdateDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        TextBoxPO.create(driver, roleNameInput).appendText(name);
    }
    
    public StringListEditorCompositePO getPermissionsList() {
        return StringListEditorCompositePO.create(driver, permissionsInput);
    }
    
    public void addPermission(String permission) {
        getPermissionsList().addNewValue(permission);
    }
    
    public boolean isPermissionPresent(String permission) {
        ValueEntryPO findValue = getPermissionsList().findValue(permission);
        return findValue != null ? true : false;
    }
    
    public void removePermission(String permission) {
        getPermissionsList().removeValueByName(permission);
    }
    
    public void clickOkButtonAndExpectPermissionError() {
        clickOkButtonOrThrow();
        waitForPO(ErrorDialogPO::new, "ErrorDialog", 10).assertTitleContainsTextAndClose("Not permitted to grant permissions for role");
    }
}
