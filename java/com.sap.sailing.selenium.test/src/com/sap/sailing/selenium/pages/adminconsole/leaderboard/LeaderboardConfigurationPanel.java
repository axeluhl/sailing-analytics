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

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListComposite.RegattaDescriptor;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;

public class LeaderboardConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateFlexibleLeaderboardButton")
    private WebElement createFlexibleLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "CreateRegattaLeaderboardButton")
    private WebElement createRegattaLeaderboardButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveLeaderboardsButton")
    private WebElement removeLeaderboardsButton;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsFilterTextBox")
    private WebElement leaderboardsFilterTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsCellTable")
    private WebElement leaderboardsCellTable;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardDetailsPanel")
    private WebElement leaderboardDetailsPanel;
    
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
        DataEntry entry = findLeaderboard(leaderboard);
        
        if(entry != null) {
            WebElement removeAction = Actions.findRemoveAction(entry.getWebElement());
            removeAction.click();
            
            Actions.acceptAlert(this.driver);
            
            waitForAjaxRequests();
        }
    }
    
    public String getLeaderboardURL(String leaderboard) {
        DataEntry entry = findLeaderboard(leaderboard);
        
        if(entry != null) {
            WebElement link = entry.getWebElement().findElement(By.xpath(".//td/div/a"));
            
            return link.getAttribute("href");
        }
        
        return null;
    }
    
    public List<String> getAvailableLeaderboards() {
        List<String> leaderboards = new ArrayList<>();
        
        CellTable<DataEntry> table = getLeaderboardTable();
        List<DataEntry> entries = table.getEntries();
        
        for(DataEntry entry : entries) {
            leaderboards.add(entry.getColumnContent(0));
        }
        
        return leaderboards;
    }
    
    public LeaderboardDetailsPanel getLeaderboardDetails(String leaderboard) {
        CellTable<DataEntry> table = getLeaderboardTable();
        List<DataEntry> entries = table.getEntries();
        
        for(DataEntry entry : entries) {
            String name = entry.getColumnContent(0);
            
            if(leaderboard.equals(name)) {
                entry.select();
            
                return new LeaderboardDetailsPanel(this.driver, this.leaderboardDetailsPanel);
            }
        }
        
        return null;
    }
    
    private CellTable<DataEntry> getLeaderboardTable() {
        return new GenericCellTable<>(this.driver, this.leaderboardsCellTable, DataEntry.class);
    }
    
    private DataEntry findLeaderboard(String leaderboard) {
        CellTable<DataEntry> table = getLeaderboardTable();
        
        for(DataEntry entry : table.getEntries()) {
            String name = entry.getColumnContent(0);
            
            if(leaderboard.equals(name)) {
                return entry;
            }
        }
        
        return null;
    }
}
