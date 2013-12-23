package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import static com.sap.sailing.selenium.pages.gwt.query.Alias.$;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.Actions;
import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;
import com.sap.sailing.selenium.pages.gwt.query.Alias;
import com.sap.sailing.selenium.pages.gwt.query.TableQuery;

public class LeaderboardGroupConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateLeaderboardGroupButton")
    private WebElement createLeaderboardGroupButton;

    @FindBy(how = BySeleniumId.class, using = "RemoveLeaderboardsButton")
    private WebElement removeButton;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardGroupsCellTable")
    private WebElement leaderboardGroupsTable;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardGroupDetailsPanel")
    private WebElement leaderboardGroupDetailsPanel;
    
    public LeaderboardGroupConfigurationPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public LeaderboardGroupCreateDialog startCreatingLeaderboardGroup() {
        this.createLeaderboardGroupButton.click();
        
        WebElement dialog = findElementBySeleniumId(this.driver, "LeaderboardGroupCreateDialog");
        
        return new LeaderboardGroupCreateDialog(this.driver, dialog);
    }
    
    public void createLeaderboardGroup(String name, String description) {
        LeaderboardGroupCreateDialog dialog = startCreatingLeaderboardGroup();
        dialog.setName(name);
        dialog.setDescription(description);
        dialog.pressOk();
    }
    
    public CellTable<DataEntry> getLeaderboardGroupsTable() {
        return new GenericCellTable<DataEntry>(this.driver, this.leaderboardGroupsTable, DataEntry.class);
    }
    
    public void deleteLeaderboardGroup(String name) {
        DataEntry result = findLeaderboardGroup(name);
        
        if (result != null) {
            Actions.findRemoveAction(result.getWebElement()).click();
            Actions.acceptAlert(this.driver);
            
            waitForAjaxRequests();
        }
    }
    
    public LeaderboardGroupDetailsPanel getLeaderboardGroupDetails(String name) {
        DataEntry result = findLeaderboardGroup(name);
        
        if (result != null) {
            result.select();
        }
        
        return getLeaderboardGroupDetails();
    }
    
    public LeaderboardGroupDetailsPanel getLeaderboardGroupDetails() {
        if(!this.leaderboardGroupDetailsPanel.isDisplayed()) {
            return null; 
        }
        
        return new LeaderboardGroupDetailsPanel(this.driver, this.leaderboardGroupDetailsPanel);
    }
    
    private DataEntry findLeaderboardGroup(String name) {
        CellTable<DataEntry> table = getLeaderboardGroupsTable();
        final DataEntry alias = Alias.alias(DataEntry.class);
        TableQuery<DataEntry> query = new TableQuery<>();
        query.from(table)
            .where(
                $(alias.getColumnContent("Name")).eq(name)
            );
        
        return query.firstResult();
    }
}
