package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import static com.sap.sailing.selenium.pages.gwt.query.Alias.$;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;

import com.sap.sailing.selenium.pages.gwt.query.Alias;
import com.sap.sailing.selenium.pages.gwt.query.TableQuery;

public class LeaderboardGroupDetailsPanel extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsFilterTextBox")
    private WebElement leaderboardsFilterTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsCellTable")
    private WebElement leaderboardsCellTable;
    
    @FindBy(how = BySeleniumId.class, using = "RefreshLeaderboardsButton")
    private WebElement refreshLeaderboardsButton;
    
    public LeaderboardGroupDetailsPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void refreshLeaderboards() {
        this.refreshLeaderboardsButton.click();
    }
    
    public CellTable<DataEntry> getLeaderboardsTable() {
        return new GenericCellTable<DataEntry>(this.driver, this.leaderboardsCellTable, DataEntry.class);
    }
    
    public DataEntry findLeaderboardEntry(String name) {
        CellTable<DataEntry> table = getLeaderboardsTable();
        DataEntry alias = Alias.alias(DataEntry.class);
        
        TableQuery<DataEntry> query = new TableQuery<>();
        query.from(table).
            where($(alias.getColumnContent("Leaderboard Name")).eq(name));
        
        return query.firstResult();
    }
    
    public void filterLeaderboards(String filterText) {
        this.leaderboardsFilterTextBox.clear();
        this.leaderboardsFilterTextBox.sendKeys(filterText);
    }
}
