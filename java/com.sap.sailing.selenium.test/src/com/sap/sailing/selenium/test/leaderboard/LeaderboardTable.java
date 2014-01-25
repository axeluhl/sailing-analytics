package com.sap.sailing.selenium.test.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.test.PageArea;
import com.sap.sailing.selenium.test.gwt.widgets.CellTable;

public class LeaderboardTable extends PageArea {

    private final CellTable table;

    public LeaderboardTable(WebDriver driver, WebElement element) {
        super(driver, element);
        this.table = new CellTable(driver, (WebElement) context);
    }

    public Iterable<String> getColumnNames() {
        return table.getHeaders();
    }
    
    public Iterable<WebElement> getRows() {
        return table.getRows();
    }
}
