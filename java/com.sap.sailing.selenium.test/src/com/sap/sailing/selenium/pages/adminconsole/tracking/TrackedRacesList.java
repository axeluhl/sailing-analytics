package com.sap.sailing.selenium.pages.adminconsole.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTable;


public class TrackedRacesList extends PageArea {
    public enum Status {
        LOADING,
        TRACKING,
        FINISHED,
        UNKOWN;
        
        public static Status fromString(String string) {
            if (string != null && !string.isEmpty()) {
                for (Status status : Status.values()) {
                    if (string.startsWith(status.name())) {
                        return status;
                    }
                }
            }
            
            return UNKOWN;
        }
    }
    
    public static class TrackedRaceDescriptor {
        public String regatta;
        public String boatClass;
        public String race;
        
        public TrackedRaceDescriptor(String regatta, String boatClass, String race) {
            this.regatta = regatta;
            this.boatClass = boatClass;
            this.race = race;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.regatta, this.boatClass, this.race);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            
            if (object == null)
                return false;
            
            if (getClass() != object.getClass())
                return false;
            
            TrackedRaceDescriptor other = (TrackedRaceDescriptor) object;
            
            if(!Objects.equals(this.regatta, other.regatta))
                return false;
            
            if(!Objects.equals(this.boatClass, other.boatClass))
                return false;
            
            if(!Objects.equals(this.race, other.race))
                return false;
            
            return true;
        }
    }
    
    @FindBy(how = BySeleniumId.class, using = "FilterRacesTextBox")
    private WebElement filterTrackedRacesField;
    
    //@FindBy(how = BySeleniumId.class, using = "NoTrackedRacesLabel")

    @FindBy(how = BySeleniumId.class, using = "TrackedRacesTable")
    private WebElement trackedRacesTable;
    
    @FindBy(how = BySeleniumId.class, using = "RefreshButton")
    private WebElement refreshButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveButton")
    private WebElement removeButton;
    
    @FindBy(how = BySeleniumId.class, using = "StopTrackingButton")
    private WebElement stopTrackingButton;
    
    @FindBy(how = BySeleniumId.class, using = "SetDelayToLiveButton")
    private WebElement setDelayToLiveButton;
    
    @FindBy(how = BySeleniumId.class, using = "ExportButton")
    private WebElement exportButton;
    
    //@FindBy(how = BySeleniumId.class, using = "ExportPopup")
    
    protected TrackedRacesList(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setFilterForTrackedRaces(String filter) {
        this.filterTrackedRacesField.clear();
        this.filterTrackedRacesField.sendKeys(filter);
    }
    
    public List<TrackedRaceDescriptor> getTrackedRaces() {
        List<TrackedRaceDescriptor> descriptors = new LinkedList<>();
        CellTable table = new CellTable(this.driver, this.trackedRacesTable);
        
        for(WebElement row : table.getRows()) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String regatta = columns.get(0).getText();
            String boatClass = columns.get(1).getText();
            String race = columns.get(2).getText();
            
            descriptors.add(new TrackedRaceDescriptor(regatta, boatClass, race));
        }
        
        return descriptors;
    }
    
    public Object getStatus(TrackedRaceDescriptor race) {
        List<Status> status = getStatus(Arrays.asList(race));
        
        return status.get(0);
    }
    
    public List<Status> getStatus(List<TrackedRaceDescriptor> races) {
        List<Status> result = new ArrayList<>(Collections.<Status>nCopies(races.size(), null));
        
        CellTable table = new CellTable(this.driver, this.trackedRacesTable);
        List<WebElement> rows = table.getRows();
        Iterator<WebElement> iterator = rows.iterator();
        
        while(iterator.hasNext()) {
            WebElement row = iterator.next();
            
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String regatta = columns.get(0).getText();
            String boatClass = columns.get(1).getText();
            String race = columns.get(2).getText();
            String status = columns.get(6).getText();
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, race);
            
            if(races.contains(descriptor)) {
                result.set(races.indexOf(descriptor), Status.fromString(status));
            }
        }
        
        return result;
    }
    
    public void stopTracking(List<TrackedRaceDescriptor> races) {
        
    }
    
    public void remove(List<TrackedRaceDescriptor> races) {
        
    }
    
    public void refresh() {
        this.refreshButton.click();
        
        waitForAjaxRequests();
    }
    
//    private List<WebElement> getTrackedRaces(List<TrackedRaceDescriptor> races) {
//        List<WebElement> result = new ArrayList<>(Collections.<WebElement>nCopies(races.size(), null));
//        
//        CellTable table = new CellTable(this.driver, this.trackedRacesTable);
//        List<WebElement> rows = table.getRows();
//        Iterator<WebElement> iterator = rows.iterator();
//        
//        while(iterator.hasNext()) {
//            WebElement row = iterator.next();
//            
//            List<WebElement> columns = row.findElements(By.tagName("td"));
//            String regatta = columns.get(0).getText();
//            String boatClass = columns.get(1).getText();
//            String race = columns.get(2).getText();
//            
//            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, race);
//            
//            if(races.contains(descriptor)) {
//                result.add(row);
//            }
//        }
//        
//        return result;
//    }
}
