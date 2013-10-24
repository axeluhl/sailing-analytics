package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.Actions;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;

import com.sap.sailing.selenium.pages.gwt.CellTable;

public class LeaderboardConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateFlexibleLeaderboardButton")
    private WebElement createFlexibleLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateRegattaLeaderboardButton")
    private WebElement createRegattaLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "AvailableLeaderboardsTable")
    private WebElement availableLeaderboardsTable;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardDetails")
    private WebElement leaderboardDetails;
    
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesList")
    private WebElement trackedRacesList;
    
    public LeaderboardConfigurationPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public FlexibleLeaderboardCreateDialog startCreatingFlexibleLeaderboard() {
        this.createFlexibleLeaderboardButton.click();
        // Wait, since we trigger an AJAX-request to get the available events
        waitForAjaxRequests();
        WebElement dialog = findElementBySeleniumId(this.driver, "FlexibleLeaderboardCreateDialog");
        return new FlexibleLeaderboardCreateDialog(this.driver, dialog);
    }
    
    // TODO: Extend with display name, scoring system, event, course area and discars
    public void createFlexibleLeaderboard(String name) {
        FlexibleLeaderboardCreateDialog dialog = startCreatingFlexibleLeaderboard();
        dialog.setName(name);
        dialog.pressOk();
    }
    
    public RegattaLeaderboardCreateDialog startCreatingRegattaLeaderboard() {
        this.createRegattaLeaderboardButton.click();
                                                                  
        WebElement dialog = findElementBySeleniumId(this.driver, "RegattaLeaderboardCreateDialog");
        
        return new RegattaLeaderboardCreateDialog(this.driver, dialog);
    }
    
    public void createRegattaLeaderboard(RegattaDescriptor regatta) {
        RegattaLeaderboardCreateDialog dialog = startCreatingRegattaLeaderboard();
        dialog.selectRegatta(regatta);
        dialog.pressOk();
    }
    
    public void deleteLeaderboard(String leaderboard) {
        CellTable table = getLeaderboardTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            
            if(!leaderboard.equals(name.getText()))
                continue;
            
            WebElement removeAction = Actions.findRemoveAction(row);//row.findElement(By.xpath(".//td/div/div[@title='Remove']/img"));
            removeAction.click();
            
            Actions.acceptAlert(this.driver);
            
            waitForAjaxRequests();
        }
    }
    
    public String getLeaderboardURL(String leaderboard) {
        CellTable table = getLeaderboardTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            
            if(!leaderboard.equals(name.getText()))
                continue;
            
            return name.getAttribute("href");
        }
        
        return null;
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
    
    public LeaderboardDetails getLeaderboardDetails(String leaderboard) {
        CellTable table = getLeaderboardTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div/a"));
            
            if(leaderboard.equals(name.getText())) {
                table.selectRow(row);
            
                return new LeaderboardDetails(this.driver, this.leaderboardDetails);
            }
        }
        
        return null;
    }
    
    private CellTable getLeaderboardTable() {
        return new CellTable(this.driver, this.availableLeaderboardsTable);
    }
}
