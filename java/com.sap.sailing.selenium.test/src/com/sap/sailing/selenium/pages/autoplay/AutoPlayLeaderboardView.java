package com.sap.sailing.selenium.pages.autoplay;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO;

public class AutoPlayLeaderboardView extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "LeaderboardCellTable")
    private WebElement leaderboard;

    public AutoPlayLeaderboardView(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public LeaderboardTablePO getLeaderBoard() {
        return new LeaderboardTablePO(this.driver, leaderboard);
    }

}
