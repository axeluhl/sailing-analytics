package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.ActionsHelper;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;

import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class LeaderboardConfigurationPanelPO extends PageArea {
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
    
    public LeaderboardConfigurationPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public FlexibleLeaderboardCreateDialogPO startCreatingFlexibleLeaderboard() {
        this.createFlexibleLeaderboardButton.click();
        // Wait, since we trigger an AJAX-request to get the available events
        waitForAjaxRequests();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "FlexibleLeaderboardCreateDialog");
        
        return new FlexibleLeaderboardCreateDialogPO(this.driver, dialog);
    }
    
    // TODO: Extend with display name, scoring system, event, course area and discars
    public void createFlexibleLeaderboard(String name) {
        FlexibleLeaderboardCreateDialogPO dialog = startCreatingFlexibleLeaderboard();
        dialog.setName(name);
        dialog.pressOk();
    }
    
    public RegattaLeaderboardCreateDialogPO startCreatingRegattaLeaderboard() {
        this.createRegattaLeaderboardButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "RegattaLeaderboardCreateDialog");
        
        return new RegattaLeaderboardCreateDialogPO(this.driver, dialog);
    }
    
    public void createRegattaLeaderboard(RegattaDescriptor regatta) {
        RegattaLeaderboardCreateDialogPO dialog = startCreatingRegattaLeaderboard();
        dialog.selectRegatta(regatta);
        dialog.pressOk();
    }
    
    public void deleteLeaderboard(String leaderboard) {
        DataEntryPO entry = findLeaderboard(leaderboard);
        
        if(entry != null) {
            WebElement removeAction = ActionsHelper.findRemoveAction(entry.getWebElement());
            removeAction.click();
            
            ActionsHelper.acceptAlert(this.driver);
            
            waitForAjaxRequests();
        }
    }
    
    public String getLeaderboardURL(String leaderboard) {
        DataEntryPO entry = findLeaderboard(leaderboard);
        
        if(entry != null) {
            WebElement link = entry.getWebElement().findElement(By.xpath(".//td/div/a"));
            
            return link.getAttribute("href");
        }
        
        return null;
    }
    
    public List<String> getAvailableLeaderboards() {
        List<String> leaderboards = new ArrayList<>();
        
        CellTablePO<DataEntryPO> table = getLeaderboardTable();
        List<DataEntryPO> entries = table.getEntries();
        
        for(DataEntryPO entry : entries) {
            leaderboards.add(entry.getColumnContent(0));
        }
        
        return leaderboards;
    }
    
    public LeaderboardDetailsPanelPO getLeaderboardDetails(String leaderboard) {
        CellTablePO<DataEntryPO> table = getLeaderboardTable();
        List<DataEntryPO> entries = table.getEntries();
        
        for(DataEntryPO entry : entries) {
            String name = entry.getColumnContent(0);
            
            if(leaderboard.equals(name)) {
                entry.select();
            
                return new LeaderboardDetailsPanelPO(this.driver, this.leaderboardDetailsPanel);
            }
        }
        
        return null;
    }
    
    private CellTablePO<DataEntryPO> getLeaderboardTable() {
        return new GenericCellTablePO<>(this.driver, this.leaderboardsCellTable, DataEntryPO.class);
    }
    
    private DataEntryPO findLeaderboard(String leaderboard) {
        CellTablePO<DataEntryPO> table = getLeaderboardTable();
        
        for(DataEntryPO entry : table.getEntries()) {
            String name = entry.getColumnContent(0);
            
            if(leaderboard.equals(name)) {
                return entry;
            }
        }
        
        return null;
    }
}
