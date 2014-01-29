package com.sap.sailing.selenium.test.adminconsole.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.gwt.widgets.AbstractDialog;

public class RaceColumnsInLeaderboardDialog extends AbstractDialog {
    @FindBy(how = BySeleniumId.class, using = "AddRacesListBox")
    private WebElement addRacesListBox;

    @FindBy(how = BySeleniumId.class, using = "AddRacesButton")
    private WebElement addRacesButton;

    public RaceColumnsInLeaderboardDialog(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void addRaces(int i) {
        addRacesListBox.sendKeys("2");
        addRacesButton.click();
        pressOk();
    }
}
