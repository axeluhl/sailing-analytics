package com.sap.sailing.selenium.test.adminconsole.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.test.PageArea;
import com.sap.sailing.selenium.test.gwt.widgets.CellTable;

public class LeaderboardConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateFlexibleLeaderboardButton")
    private WebElement createFlexibleLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateRegattaLeaderboardButton")
    private WebElement createRegattaLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "AvailableLeaderboardsTable")
    private WebElement availableLeaderboardsTable;
    
    @FindBy(how = BySeleniumId.class, using = "AddRaceColumnsButton")
    private WebElement addRaceColumnsButton;

    protected LeaderboardConfigurationPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    
    public List<String> getAvailableLeaderboards() {
        List<String> leaderboards = new ArrayList<>();
        
        CellTable table = getLeaderboardTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            leaderboards.add(name.getText());
        }
        
        return leaderboards;
    }
    
    public void selectLeaderboard(String leaderboardName) {
        WebElement row = getLeaderboardRow(leaderboardName);
        row.click();
    }
    
    private WebElement getLeaderboardRow(String leaderboardName) {
        for (WebElement row : getLeaderboardTable().getRows()) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            if (leaderboardName.equals(name.getText())) {
                return row;
            }
        }
        return null;
    }
    
    public void deleteLeaderboard(String leaderboard) {
        CellTable table = getLeaderboardTable();
        List<WebElement> rows = table.getRows();
        for (WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            if (leaderboard.equals(name.getText())) {
                WebElement removeAction = row.findElement(By.xpath(".//td/div/div[@title='Remove']/img"));
                removeAction.click();
                this.driver.switchTo().alert().accept();
            }
        }
    }

    public FlexibleLeaderboardCreationDialog startCreatingFlexibleLeaderboard() {
        this.createFlexibleLeaderboardButton.click();
        // Wait, since we trigger an AJAX-request to get the available events
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "CreateFlexibleLeaderboardDialog");
        return new FlexibleLeaderboardCreationDialog(this.driver, dialog);
    }
    
    private CellTable getLeaderboardTable() {
        return new CellTable(this.driver, this.availableLeaderboardsTable);
    }
    
    protected void initElements() {
        super.initElements();
        
        //this.availableLeaderboardsTable = new CellTable(this.driver, findElementBySeleniumId("AvailableLeaderboardsTable"));
    }

    /**
     * Assuming that the leaderboard is already selected, clicks the "Add Races..." button 
     * @param i
     */
    public void addRacesToFlexibleLeaderboard(int i) {
        addRaceColumnsButton.click();
        WebElement dialog = findElementBySeleniumId(this.driver, "RaceColumnsInLeaderboardDialog");
        RaceColumnsInLeaderboardDialog raceColumnsInLeaderboardDialog = new RaceColumnsInLeaderboardDialog(this.driver, dialog);
        raceColumnsInLeaderboardDialog.addRaces(2);
        selectRaceColumn("R1", "Default");
    }
    
    public void selectRaceColumn(String raceColumnName, String fleetName) {
        CellTable table = getRaceColumnsTable();
        for (WebElement row : table.getRows()) {
            List<WebElement> fields = row.findElements(By.tagName("td"));
            if (raceColumnName.equals(fields.get(0).getText()) && fleetName.equals(fields.get(1).getText())) {
                row.click();
                break;
            }
        }
    }

    public TrackedRacesPanel getTrackedRacesPanel() {
        return new TrackedRacesPanel(driver, findElementBySeleniumId(context, "TrackedRaces"));
    }
    
    private CellTable getRaceColumnsTable() {
        return new CellTable(this.driver, findElementBySeleniumId(this.driver, "RaceColumnTable"));
    }
}
