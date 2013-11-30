package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import static com.sap.sailing.selenium.pages.gwt.query.Alias.$;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.Actions;
import com.sap.sailing.selenium.pages.gwt.CellTable2;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;
import com.sap.sailing.selenium.pages.gwt.query.Alias;
import com.sap.sailing.selenium.pages.gwt.query.TableQuery;

public class LeaderboardGroupConfigurationPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "CreateLeaderboardGroupButton")
    private WebElement createLeaderboardGroupButton;

    @FindBy(how = BySeleniumId.class, using = "LeaderboardGroupsTable")
    private WebElement leaderboardGroupsTable;

    @FindBy(how = BySeleniumId.class, using = "LeaderboardsCaptionPanel")
    private WebElement leaderboardsCaptionPanel;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsTable")
    private WebElement leaderboardsTable;
    
    @FindBy(how = BySeleniumId.class, using = "RefreshLeaderboardsButton")
    private WebElement refreshLeaderboardsButton;

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
    
    public CellTable2<DataEntry> getLeaderboardGroupsTable() {
        return new GenericCellTable<DataEntry>(driver, leaderboardGroupsTable, DataEntry.class);
    }
    
    

    public void deleteLeaderboardGroup(String name) {
        CellTable2<DataEntry> table = getLeaderboardGroupsTable();
        final DataEntry alias = Alias.alias(DataEntry.class);
        TableQuery<DataEntry> query = new TableQuery<>();
        query.from(table).where($(alias.getColumnContent("Name")).eq(name));
        DataEntry result = query.firstResult();
        if (result != null) {
            Actions.findRemoveAction(result.getWebElement()).click();
            Actions.acceptAlert(this.driver);
        }
    }

    public void refreshLeaderboards() {
        refreshLeaderboardsButton.click();
        
    }

    public CellTable2<DataEntry> getLeaderboardsTable() {
        return new GenericCellTable<DataEntry>(driver, leaderboardsTable, DataEntry.class);
    }

    public DataEntry findLeaderboardEntry(String name) {
        CellTable2<DataEntry> table = getLeaderboardsTable();
        final DataEntry alias = Alias.alias(DataEntry.class);
        TableQuery<DataEntry> query = new TableQuery<>();
        query.from(table).where($(alias.getColumnContent("Leaderboard Name")).eq(name));
        return query.firstResult();
    }

    public void filterLeaderboards(String filterText) {
        WebElement leaderboardFilterTextField = leaderboardsCaptionPanel.findElement(new BySeleniumId("FilterTextField"));
        leaderboardFilterTextField.clear();
        leaderboardFilterTextField.sendKeys(filterText);
    }
}
