package com.sap.sailing.selenium.pages.autoplay;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO;

public class AutoPlayLeaderboardView extends PageArea {
    public AutoPlayLeaderboardView(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public LeaderboardTablePO getLeaderBoardWithData() {
        WebElement leaderboardCellTable = new WebDriverWait(driver, 30).until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                WebElement leaderboardCellTable = findElementOrNullBySeleniumId("LeaderboardCellTable");
                if (leaderboardCellTable != null) {
                    if (isElementEntirelyVisible(leaderboardCellTable)) {
                        return leaderboardCellTable;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
        LeaderboardTablePO leaderboardTablePO = new LeaderboardTablePO(this.driver, leaderboardCellTable);
        leaderboardTablePO.waitForTableToShowData();
        
        // After the data is loaded it could be possible that the table isn't completely visible anymore
        new WebDriverWait(driver, 30).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver arg0) {
                return isElementEntirelyVisible(leaderboardCellTable);
            }
        });
        return leaderboardTablePO;
    }

}
