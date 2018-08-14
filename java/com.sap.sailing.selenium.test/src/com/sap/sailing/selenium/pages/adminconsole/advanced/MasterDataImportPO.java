package com.sap.sailing.selenium.pages.adminconsole.advanced;

import java.util.function.BooleanSupplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class MasterDataImportPO extends PageArea {
    public MasterDataImportPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @FindBy(how = BySeleniumId.class, using = "fetchLeaderboardGroupList")
    private WebElement fetchbutton;

    @FindBy(how = BySeleniumId.class, using = "overrideExisting")
    private WebElement override;

    @FindBy(how = BySeleniumId.class, using = "wind")
    private WebElement wind;

    @FindBy(how = BySeleniumId.class, using = "import")
    private WebElement importBtn;

    @FindBy(how = BySeleniumId.class, using = "LeaderBoardGroupListBox")
    private WebElement leaderBoardGroupListBox;

    @FindBy(how = BySeleniumId.class, using = "overallProgressBar")
    private WebElement overallProgressBar;

    public void fetchLeaderBoards() {
        fetchbutton.click();
        waitUntil(new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() {
                // some result is loaded
                return leaderBoardGroupListBox.getSize().getHeight() > 100;
            }
        });

    }

    public void importData(String eventName, boolean override, boolean wind) throws InterruptedException {
        if (override) {
            this.override.click();
        }
        if (!wind) {
            this.wind.click();
        }
        new Select(leaderBoardGroupListBox).selectByValue(eventName);
        importBtn.click();
        waitForNotificationAndDismiss();
    }
}
