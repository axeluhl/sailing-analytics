package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class LeaderboardDetails extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "RacesTable")
    private WebElement racesTable;
    
    public LeaderboardDetails(WebDriver driver, WebElement element) {
        super(driver, element);
    }

}
