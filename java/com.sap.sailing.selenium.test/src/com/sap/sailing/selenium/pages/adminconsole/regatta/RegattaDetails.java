package com.sap.sailing.selenium.pages.adminconsole.regatta;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.Actions;
import com.sap.sailing.selenium.pages.gwt.CellTable;

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
        CellTable table = getSeriesTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div"));
            
            if(!series.equals(name.getText()))
                continue;
            
            WebElement action = Actions.findEditAction(row);
            action.click();
            
            WebElement dialog = findElementBySeleniumId(this.driver, "SeriesEditDialog");
            
            return new SeriesEditDialog(this.driver, dialog);
        }
        
        return null;
    }
    
    public void deleteSeries(String series) {
        CellTable table = getSeriesTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div"));
            
            if(!series.equals(name.getText()))
                continue;
            
            WebElement removeAction = Actions.findRemoveAction(row);
            removeAction.click();
            
            Actions.acceptAlert(this.driver);
            
            //waitForAjaxRequests();
        }
    }
    
    private WebElement findSeries(String series) {
        CellTable table = getSeriesTable();
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            WebElement name = row.findElement(By.xpath(".//td/div"));
            
            if(series.equals(name.getText()))
               return row;
        }
        
        return null;
    }
    
    private CellTable getSeriesTable() {
        return new CellTable(this.driver, this.seriesTable);
    }
}
