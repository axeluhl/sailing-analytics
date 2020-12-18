package com.sap.sailing.selenium.pages.adminconsole.security;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.pages.common.DataEntryDialogPO;

public class AclPopupPO extends DataEntryDialogPO {

    public static final String INPUT_SUGGEST_BOX = "InputSuggestBox";
    public static final String ALLOWED_ACTIONS_CONTAINER = "allowedActionsContainer";
    public static final String DENIED_ACTIONS_CONTAINER = "deniedActionsContainer";
    public static final String ADD_USER_GROUP_BUTTON = "AddUserGroupButton";
    public static final String SUGGEST_USER_GROUP_INPUT = "SuggestUserGroupInput";

    public AclPopupPO(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public void addUserGroup(String groupName) {
        driver.findElement(new BySeleniumId(SUGGEST_USER_GROUP_INPUT)).sendKeys(groupName);
        driver.findElement(new BySeleniumId(ADD_USER_GROUP_BUTTON)).click();
    }

    public AclActionInputPO getDeniedActionsInput() {
        return new AclActionInputPO(driver, context.findElement(new BySeleniumId(DENIED_ACTIONS_CONTAINER)));
    }

    public AclActionInputPO getAllowedActionsInput() {
        return new AclActionInputPO(driver, context.findElement(new BySeleniumId(ALLOWED_ACTIONS_CONTAINER)));
    }

    @Override
    public void clickOkButtonOrThrow() {
        // On Hudson the limited screen height may cause notifications to cover the buttons
        dismissAllExistingNotifications();
        super.clickOkButtonOrThrow();
        waitForAjaxRequests();
    }
}