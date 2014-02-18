package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import static com.sap.sailing.selenium.pages.gwt.query.Alias.$;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

import com.sap.sailing.selenium.pages.gwt.query.Alias;
import com.sap.sailing.selenium.pages.gwt.query.TableQuery;

public class LeaderboardGroupDetailsPanelPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsFilterTextBox")
    private WebElement leaderboardsFilterTextBox;
    
    @FindBy(how = BySeleniumId.class, using = "LeaderboardsCellTable")
    private WebElement leaderboardsCellTable;
    
    @FindBy(how = BySeleniumId.class, using = "RefreshLeaderboardsButton")
    private WebElement refreshLeaderboardsButton;
    
    public LeaderboardGroupDetailsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void refreshLeaderboards() {
        this.refreshLeaderboardsButton.click();
    }
    
    public CellTablePO<DataEntryPO> getLeaderboardsTable() {
        return new GenericCellTablePO<DataEntryPO>(this.driver, this.leaderboardsCellTable, DataEntryPO.class);
    }
    
    public DataEntryPO findLeaderboardEntry(String name) {
        CellTablePO<DataEntryPO> table = getLeaderboardsTable();
        DataEntryPO alias = Alias.alias(DataEntryPO.class);
        
        TableQuery<DataEntryPO> query = new TableQuery<>();
        query.from(table).
            where($(alias.getColumnContent("Leaderboard Name")).eq(name));
        
        return query.firstResult();
    }
    
    public void filterLeaderboards(String filterText) {
        this.leaderboardsFilterTextBox.clear();
        this.leaderboardsFilterTextBox.sendKeys(filterText);
    }
}
