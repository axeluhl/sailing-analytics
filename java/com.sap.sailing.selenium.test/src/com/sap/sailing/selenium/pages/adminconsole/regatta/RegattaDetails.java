package com.sap.sailing.selenium.pages.adminconsole.regatta;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.Actions;

import com.sap.sailing.selenium.pages.gwt.CellTable2;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;

// TODO: Implement if needed
public class RegattaDetails extends PageArea {
    @FindBy(how = BySeleniumId.class, using = "RegattaNameLabel")
    private WebElement regattaNameLabel;
    
    @FindBy(how = BySeleniumId.class, using = "BoatClassLabel")
    private WebElement boatClassLabel;
    
    @FindBy(how = BySeleniumId.class, using = "CourseAreaLabel")
    private WebElement courseAreaLabel;
    
    @FindBy(how = BySeleniumId.class, using = "ScoringSystemLabel")
    private WebElement scoringSystemLabel;
    
    @FindBy(how = BySeleniumId.class, using = "SeriesTable")
    private WebElement seriesTable;
    
    public RegattaDetails(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public String getRegattaName() {
        return this.regattaNameLabel.getText();
    }
    
    public String getBoatClass() {
        return this.boatClassLabel.getText();
    }
    
    public String getCouresArea() {
        return this.courseAreaLabel.getText();
    }
    
    public String getScoringSystem() {
        return this.scoringSystemLabel.getText();
    }
    
//    public void getSeries() {
//        
//    }
    
    public SeriesEditDialog editSeries(String series) {
        DataEntry entry = findSeries(series);
        
        if(entry != null) {
            WebElement action = Actions.findEditAction(entry.getWebElement());
            action.click();
            
            WebElement dialog = findElementBySeleniumId(this.driver, "SeriesEditDialog");
            
            return new SeriesEditDialog(this.driver, dialog);
        }
        
        return null;
    }
    
    public void deleteSeries(String series) {
        DataEntry entry = findSeries(series);
        
        if(entry != null) {
            WebElement removeAction = Actions.findRemoveAction(entry.getWebElement());
            removeAction.click();
            
            Actions.acceptAlert(this.driver);
            
            //waitForAjaxRequests();
        }
    }
    
    private DataEntry findSeries(String series) {
        CellTable2<DataEntry> table = getSeriesTable();
        
        for(DataEntry entry : table.getEntries()) {
            String name = entry.getColumnContent(0);
            
            if(series.equals(name))
               return entry;
        }
        
        return null;
    }
    
    private CellTable2<DataEntry> getSeriesTable() {
        return new GenericCellTable<>(this.driver, this.seriesTable, DataEntry.class);
    }
}
