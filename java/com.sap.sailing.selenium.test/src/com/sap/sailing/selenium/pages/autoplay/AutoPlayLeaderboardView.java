package com.sap.sailing.selenium.pages.autoplay;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO;

public class AutoPlayLeaderboardView extends PageArea {
    public AutoPlayLeaderboardView(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public LeaderboardTablePO getLeaderBoard() {
        WebElement leaderboardCellTable = new WebDriverWait(driver, 30).until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                WebElement leaderboardCellTable = findElementOrNullBySeleniumId("LeaderboardCellTable");
                if (leaderboardCellTable != null) {
                    try {
                        if (leaderboardCellTable.isDisplayed()) {
                            final int windowWidth = driver.manage().window().getSize().getWidth();
                            if (windowWidth >= leaderboardCellTable.getLocation().x
                                    + leaderboardCellTable.getSize().width) {
                                return leaderboardCellTable;
                            } else {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        // The element may currently only partially visible which makes some of the calls fail
                        // In this case it is necessary to wait for the next loop
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
        return new LeaderboardTablePO(this.driver, leaderboardCellTable);
    }

}
