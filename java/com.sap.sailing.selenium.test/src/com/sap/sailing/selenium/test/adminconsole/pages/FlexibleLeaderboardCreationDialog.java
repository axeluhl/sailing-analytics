package com.sap.sailing.selenium.test.adminconsole.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.gwt.widgets.AbstractDialog;

public class FlexibleLeaderboardCreationDialog extends AbstractDialog {
    @FindBy(how = BySeleniumId.class, using = "LeaderboardNameField")
    private WebElement nameField;
    @FindBy(how = BySeleniumId.class, using = "LeaderboardDisplayNameField")
    private WebElement displayNameField;

    public FlexibleLeaderboardCreationDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setName(String name) {
        this.nameField.clear();
        this.nameField.sendKeys(name);
        // now move the focus away from the nameField to ensure the onchange event is fired; see https://code.google.com/p/selenium/wiki/FrequentlyAskedQuestions
        this.displayNameField.sendKeys(" ");
        this.displayNameField.clear();
    }
}
