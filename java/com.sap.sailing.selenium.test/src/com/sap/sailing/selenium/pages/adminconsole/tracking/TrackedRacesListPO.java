package com.sap.sailing.selenium.pages.adminconsole.tracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;

public class TrackedRacesListPO extends PageArea {
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
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (getClass() != object.getClass()) {
                return false;
            }
            TrackedRaceDescriptor other = (TrackedRaceDescriptor) object;
            if (!Objects.equals(this.regatta, other.regatta)) {
                return false;
            }
            if (!Objects.equals(BoatClassMasterdata.resolveBoatClass(this.boatClass), BoatClassMasterdata.resolveBoatClass(other.boatClass))) {
                return false;
            }
            if (!Objects.equals(this.race, other.race)) {
                return false;
            }
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
    
    public TrackedRacesListPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public void setFilterForTrackedRaces(String filter) {
        this.trackedRacesFilterTextBox.clear();
        this.trackedRacesFilterTextBox.sendKeys(filter);
    }
    
    public List<DataEntryPO> getTrackedRaces() {
//        List<TrackedRaceDescriptor> descriptors = new LinkedList<>();
        CellTablePO<DataEntryPO> table = getTrackedRacesTable();
        
//        for(DataEntryPO entry : table.getEntries()) {
//            String regatta = entry.getColumnContent(0);
//            String boatClass = entry.getColumnContent(1);
//            String race = entry.getColumnContent(2);
//            
//            descriptors.add(new TrackedRaceDescriptor(regatta, boatClass, race));
//        }
        
        return table.getEntries();
    }
    
//    public WebElement getTrackedRace(String regatta, String race) {
//        for (WebElement raceRow : getTrackedRaces()) {
//            List<WebElement> fields = raceRow.findElements(By.tagName("td"));
//            if (regattaName.equals(fields.get(0).getText()) && raceName.equals(fields.get(2).getText())) {
//                return raceRow;
//            }
//        }
//        return null;
//    }
    
    public Status getStatus(TrackedRaceDescriptor race) {
        List<Status> status = getStatus(Arrays.asList(race));
        
        return status.get(0);
    }
    
    public List<Status> getStatus(List<TrackedRaceDescriptor> races) {
        List<Status> result = new ArrayList<>(Collections.<Status>nCopies(races.size(), null));
        
        CellTablePO<DataEntryPO> table = getTrackedRacesTable();
        
        final int regattaColumnIndex = table.getColumnIndex("Regatta");
        final int boatClassColumnIndex = table.getColumnIndex("Boat Class");
        final int raceColumnIndex = table.getColumnIndex("Race");
        final int statusColumnIndex = table.getColumnIndex("Status");
        for(DataEntryPO entry : table.getEntries()) {
            String regatta = entry.getColumnContent(regattaColumnIndex);
            String boatClass = entry.getColumnContent(boatClassColumnIndex);
            String race = entry.getColumnContent(raceColumnIndex);
            String status = entry.getColumnContent(statusColumnIndex);
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, race);
            
            if(races.contains(descriptor)) {
                result.set(races.indexOf(descriptor), Status.fromString(status));
            }
        }
        
        return result;
    }
    
    public void stopTracking(TrackedRaceDescriptor race) {
        stopTracking(Arrays.asList(race), false);
    }
    
    public void stopTrackingAndWaitForFinshed(TrackedRaceDescriptor race) {
        stopTracking(Arrays.asList(race), true);
    }
    
    public void stopTracking(List<TrackedRaceDescriptor> races) {
        stopTracking(races, false);
    }
    
    public void stopTrackingAndWaitForFinshed(List<TrackedRaceDescriptor> races) {
        stopTracking(races, true);
    }
    
    public void remove(TrackedRaceDescriptor race) {
        remove(Arrays.asList(race));
    }
    
    public void remove(List<TrackedRaceDescriptor> races) {
        selectRaces(races);
        
        removeSelectedTrackedRacesAndWaitForAjaxRequests();
    }
    
    public void removeAll() {
        getTrackedRacesTable().selectAllEntries();
        
        removeSelectedTrackedRacesAndWaitForAjaxRequests();
    }

    private void removeSelectedTrackedRacesAndWaitForAjaxRequests() {
        this.removeButton.click();
        
        waitForAjaxRequests();
    }
    
    public void refresh() {
        this.refreshButton.click();
        
        waitForAjaxRequests();
    }
    
    public void waitForTrackedRace(TrackedRaceDescriptor race, Status status) {
        waitForTrackedRaces(Arrays.asList(race), status);
    }
    
    public void waitForTrackedRace(TrackedRaceDescriptor race, Status status, int timeout) {
        waitForTrackedRaces(Arrays.asList(race), status, timeout);
    }
    
    public void waitForTrackedRaces(List<TrackedRaceDescriptor> races, Status status) {
        waitForTrackedRaces(races, status, DEFAULT_WAIT_TIMEOUT_SECONDS);
    }
    
    public void waitForTrackedRaces(List<TrackedRaceDescriptor> races, Status status, int timeout) {
        final Status statusToWaitFor = status;
        FluentWait<List<TrackedRaceDescriptor>> wait = createFluentWait(races, timeout, DEFAULT_POLLING_INTERVAL);
        wait.until(new Function<List<TrackedRaceDescriptor>, Object>() {
            @Override
            public Object apply(List<TrackedRaceDescriptor> races) {
                try {
                    refresh();
                    for (Status status : getStatus(races)) {
                        if (status != statusToWaitFor) {
                            return Boolean.FALSE;
                        }
                    }
                } catch (TimeoutException exception) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
    }
    
//    public void waitForTrackedRaceLoadingFinished(String regattaName, String raceName, long timeoutInMillis) throws InterruptedException {
//        waitForAjaxRequests(); // wait for the Start Tracking request to succeed; then check Tracked races table and keep refreshing until we time out
//        TrackedRacesPanel trp = getTrackedRacesPanel();
//        long started = System.currentTimeMillis();
//        WebElement raceRow = trp.getTrackedRace(regattaName, raceName);
//        while ((raceRow == null || !"TRACKING".equals(raceRow.findElements(By.tagName("td")).get(6).getText()))
//                && System.currentTimeMillis()-started < timeoutInMillis) {
//            Thread.sleep(2000); // wait 2s for the race to appear
//            trp.refresh();
//            raceRow = trp.getTrackedRace(regattaName, raceName);
//        }
//    }
    
    public void stopTracking(List<TrackedRaceDescriptor> races, boolean waitForFinished) {
        selectRaces(races);
        this.stopTrackingButton.click();
        waitForAjaxRequests();
        if(waitForFinished) {
            waitForTrackedRaces(races, Status.FINISHED);
        }
    }
    
    public CellTablePO<DataEntryPO> getTrackedRacesTable() {
//        if(!this.trackedRacesTable.isDisplayed()) {
//            return null;
//        }
        
        return new GenericCellTablePO<>(this.driver, this.trackedRacesTable, DataEntryPO.class);
    }
    
    private List<DataEntryPO> getTrackedRaces(List<TrackedRaceDescriptor> races) {
        List<DataEntryPO> result = new ArrayList<>(Collections.<DataEntryPO>nCopies(races.size(), null));
        
        CellTablePO<DataEntryPO> table = getTrackedRacesTable();
        List<DataEntryPO> rows = table.getEntries();
        Iterator<DataEntryPO> iterator = rows.iterator();
        
        final int regattaColumnIndex = table.getColumnIndex("Regatta");
        final int boatClassColumnIndex = table.getColumnIndex("Boat Class");
        final int raceColumnIndex = table.getColumnIndex("Race");
        while(iterator.hasNext()) {
            DataEntryPO entry = iterator.next();
            
            String regatta = entry.getColumnContent(regattaColumnIndex);
            String boatClass = entry.getColumnContent(boatClassColumnIndex);
            String race = entry.getColumnContent(raceColumnIndex);
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, race);
            
            if(races.contains(descriptor)) {
                result.set(races.indexOf(descriptor), entry);
            }
        }
        
        return result;
    }
    
    private void selectRaces(List<TrackedRaceDescriptor> races) {
        CellTablePO<DataEntryPO> table = getTrackedRacesTable();
        table.selectEntries(() -> getTrackedRaces(races).stream().filter(tr -> tr != null));
    }
}
