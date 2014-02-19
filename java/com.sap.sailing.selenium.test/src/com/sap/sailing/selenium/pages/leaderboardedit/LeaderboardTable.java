package com.sap.sailing.selenium.pages.leaderboardedit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class LeaderboardTable extends PageArea {

    private final CellTablePO<DataEntryPO> table;

    public LeaderboardTable(WebDriver driver, WebElement element) {
        super(driver, element);
        this.table = new GenericCellTablePO<>(driver, element, DataEntryPO.class);
    }

    public Iterable<String> getColumnNames() {
        return table.getColumnHeaders();
    }
    
    public Iterable<DataEntryPO> getRows() {
        return table.getEntries();
    }
}
