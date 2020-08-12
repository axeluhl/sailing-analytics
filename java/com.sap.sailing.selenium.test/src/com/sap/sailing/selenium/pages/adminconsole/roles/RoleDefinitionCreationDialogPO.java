package com.sap.sailing.selenium.pages.adminconsole.roles;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;
import com.sap.sailing.selenium.pages.gwt.StringListEditorCompositePO;
import com.sap.sailing.selenium.pages.gwt.TextBoxPO;

public class RoleDefinitionCreationDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "name")
    private WebElement roleNameInput;
    @FindBy(how = BySeleniumId.class, using = "permissions")
    private WebElement permissionsInput;

    protected RoleDefinitionCreationDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        TextBoxPO.create(driver, roleNameInput).appendText(name);
    }
    
    public void addPermission(String permission) {
        StringListEditorCompositePO.create(driver, permissionsInput).addNewValue(permission);
    }
}
