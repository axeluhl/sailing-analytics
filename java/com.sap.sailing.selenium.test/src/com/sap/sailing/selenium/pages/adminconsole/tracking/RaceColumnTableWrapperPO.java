package com.sap.sailing.selenium.pages.adminconsole.tracking;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class RaceColumnTableWrapperPO extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "WrappedTable")
    private WebElement raceColumnTable;
    
    public RaceColumnTableWrapperPO(WebDriver driver, WebElement element) {
        super(driver,element);
    }
    
    public CellTablePO<DataEntryPO> getRaceColumnTable() {
        return new GenericCellTablePO<>(this.driver, raceColumnTable, DataEntryPO.class);
    }
}
