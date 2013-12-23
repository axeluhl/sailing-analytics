package com.sap.sailing.selenium.pages.adminconsole.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;

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
    
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesFilterTextBox")
    private WebElement trackedRacesFilterTextBox;
    
    //@FindBy(how = BySeleniumId.class, using = "NoTrackedRacesLabel")

    @FindBy(how = BySeleniumId.class, using = "TrackedRacesCellTable")
    private WebElement trackedRacesTable;
    
    @FindBy(how = BySeleniumId.class, using = "RefreshButton")
    private WebElement refreshButton;
    
    @FindBy(how = BySeleniumId.class, using = "RemoveRaceButton")
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
        this.trackedRacesFilterTextBox.clear();
        this.trackedRacesFilterTextBox.sendKeys(filter);
    }
    
    public List<TrackedRaceDescriptor> getTrackedRaces() {
        List<TrackedRaceDescriptor> descriptors = new LinkedList<>();
        CellTable<DataEntry> table = getTrackedRacesTable();
        
        for(DataEntry entry : table.getEntries()) {
            String regatta = entry.getColumnContent(0);
            String boatClass = entry.getColumnContent(1);
            String race = entry.getColumnContent(2);
            
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
        
        CellTable<DataEntry> table = getTrackedRacesTable();
        
        for(DataEntry entry : table.getEntries()) {
            String regatta = entry.getColumnContent(0);
            String boatClass = entry.getColumnContent(1);
            String race = entry.getColumnContent(2);
            String status = entry.getColumnContent(6);
            
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
    
    private CellTable<DataEntry> getTrackedRacesTable() {
        return new GenericCellTable<>(this.driver, this.trackedRacesTable, DataEntry.class);
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
