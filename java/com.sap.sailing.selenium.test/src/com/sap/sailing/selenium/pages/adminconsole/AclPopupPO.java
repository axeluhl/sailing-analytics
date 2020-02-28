package com.sap.sailing.selenium.pages.adminconsole;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.pages.PageArea;

public class AclPopupPO extends PageArea {

    public AclPopupPO(WebDriver driver, WebElement root) {
        super(driver, root);
    }

    public void addUserGroup(String groupName) {
        driver.findElement(new BySeleniumId("SuggestUserGroupInput")).sendKeys(groupName);
        driver.findElement(new BySeleniumId("AddUserGroupButton")).click();
    }
    
    public List<WebElement> getInputSuggestionBoxes() {
        return context.findElements(new BySeleniumId("InputSuggestBox"));
    }
    
}