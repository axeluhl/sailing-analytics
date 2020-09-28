package com.sap.sailing.selenium.pages.adminconsole.usermanagement;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class EditRolesAndPermissionsForUserDialogPO extends DataEntryDialogPO {

    @FindBy(how = BySeleniumId.class, using = "ChangePasswordButton")
    private WebElement changePasswordButton;

    protected EditRolesAndPermissionsForUserDialogPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public WildcardPermissionPanelPO getUserPermissions() {
        return getChildPO(WildcardPermissionPanelPO::new, "WildcardPermissionPanel");
    }
    
    public UserRoleDefinitionPanelPO getUserRoles() {
        return getChildPO(UserRoleDefinitionPanelPO::new, "UserRoleDefinitionPanel");
    }

}
