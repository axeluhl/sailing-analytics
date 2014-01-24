package com.sap.sailing.selenium.test.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.test.PageArea;
import com.sap.sailing.selenium.test.gwt.widgets.CellTable;

public class LeaderboardTable extends PageArea {

    public LeaderboardTable(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public Iterable<String> getColumnNames() {
        CellTable table = new CellTable(driver, (WebElement) context);
        return table.getHeaders();
    }
}
