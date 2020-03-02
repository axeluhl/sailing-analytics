package com.sap.sailing.selenium.pages.adminconsole;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.pages.PageArea;

public class AclPopupPO extends PageArea {

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

    public WebElement getDeniedActionsInput() {
        WebElement parent = context.findElement(new BySeleniumId(DENIED_ACTIONS_CONTAINER));
        return parent.findElement(new BySeleniumId(INPUT_SUGGEST_BOX));
    }

    public WebElement getAllowedActionsInput() {
        WebElement parent = context.findElement(new BySeleniumId(ALLOWED_ACTIONS_CONTAINER));
        return parent.findElement(new BySeleniumId(INPUT_SUGGEST_BOX));
    }

}