package com.sap.sailing.selenium.pages.adminconsole.tractrac;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.core.BySeleniumId;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaList.RegattaDescriptor;

import com.sap.sailing.selenium.pages.gwt.CellTable2;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;


/**
 * <p>The page object representing the TracTrac Events tab.</p>
 * 
 * @author
 *   D049941
 */
public class TracTracEventManagementPanel extends PageArea {
    public static class TrackableRaceDescriptor {
        public final String eventName;
        public final String raceName;
        public final String boatClass;
        //public Object startTime;
        //public Object raceStatus;
        
        public TrackableRaceDescriptor(String eventName, String raceName, String boatClass) {
            this.eventName = eventName;
            this.raceName = raceName;
            this.boatClass = boatClass;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.boatClass, this.eventName, this.raceName);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            
            if (object == null)
                return false;
            
            if (getClass() != object.getClass())
                return false;
            
            TrackableRaceDescriptor other = (TrackableRaceDescriptor) object;
            
            if(!Objects.equals(this.boatClass, other.boatClass))
                return false;
            
            if(!Objects.equals(this.eventName, other.eventName))
                return false;
            
            if(!Objects.equals(this.raceName, other.raceName))
                return false;
            
            return true;
        }
    }
    
    // TODO [D049941]: Prefix the Ids with the component (e.g. "Button")
    @FindBy(how = BySeleniumId.class, using = "LiveURI")
    private WebElement liveURITextField;
    
    @FindBy(how = BySeleniumId.class, using = "StoredURI")
    private WebElement storedURITextField;
    
    @FindBy(how = BySeleniumId.class, using = "JSONURL")
    private WebElement jsonURLTextField;
    
    @FindBy(how = BySeleniumId.class, using = "ListRaces")
    private WebElement listRacesButton;
    
    @FindBy(how = BySeleniumId.class, using = "AvailableRegattas")
    private WebElement availableRegattasDropDown;
    
    @FindBy(how = BySeleniumId.class, using = "TrackWind")
    private WebElement trackWindCheckBox;

    @FindBy(how = BySeleniumId.class, using = "CorrectWind")
    private WebElement correctWindCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "SimulateWithStartTimeNow")
    private WebElement simulateWithNowCheckbox;
    
    @FindBy(how = BySeleniumId.class, using = "FilterRaces")
    private WebElement filterTrackableRacesTextField;
    
    @FindBy(how = BySeleniumId.class, using = "RacesTable")
    private WebElement trackableRacesTable;
    
    @FindBy(how = BySeleniumId.class, using = "StartTracking")
    private WebElement startTrackingButton;
    
    /**
     * <p></p>
     * 
     * @param driver
     *   
     * @param element
     *   
     */
    public TracTracEventManagementPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    
    /**
     * <p>Lists all available trackable races for the given URL. The list of the races can be obtained via
     *   {@link #getTrackableRaces()}.</p>
     * 
     * @param url
     *   The URL for which the races are to list.
     */
    public void listTrackableRaces(String url) {
        listTrackableRaces("", "", url);  //$NON-NLS-1$//$NON-NLS-2$
    }
    
    /**
     * <p>Lists all available trackable races for the given URL. The list of the races can be obtained via
     *   {@link #getTrackableRaces()}.</p>
     * 
     * @param url
     *   The URL for which the races are to list.
     */
    public void listTrackableRaces(String liveURI, String storedURI, String jsonURL) {
        this.liveURITextField.clear();
        this.liveURITextField.sendKeys(liveURI);
        
        this.storedURITextField.clear();
        this.storedURITextField.sendKeys(storedURI);
        
        this.jsonURLTextField.clear();
        this.jsonURLTextField.sendKeys(jsonURL);
        
        this.listRacesButton.click();
        
        waitForAjaxRequests();
    }
    
    /**
     * <p>Returns the list of all available trackable races. This list will be empty if no race is available or if no
     *   race was specified before.</p>
     * 
     * @return
     *   The list of all available trackable races.
     */
    public List<TrackableRaceDescriptor> getTrackableRaces() {
        List<TrackableRaceDescriptor> descriptors = new LinkedList<>();
        CellTable2<DataEntry> table = getTrackableRacesTable();
        
        for(DataEntry entry : table.getEntries()) {
            String event = entry.getColumnContent(0);
            String race = entry.getColumnContent(1);
            String boatClass = entry.getColumnContent(2);
            
            descriptors.add(new TrackableRaceDescriptor(event, race, boatClass));
        }
        
        return descriptors;
    }
    
    public List<RegattaDescriptor> getAvailableReggatasForTracking() {
        List<RegattaDescriptor> result = new ArrayList<>();
        
        Select select = new Select(this.availableRegattasDropDown);
        
        for(WebElement option : select.getOptions()) {
            RegattaDescriptor regatta = RegattaDescriptor.fromString(option.getAttribute("value"));
            
            result.add(regatta);
        }
        
        return result;
    }
    
    public RegattaDescriptor getReggataForTracking() {
        Select select = new Select(this.availableRegattasDropDown);
        WebElement option = select.getFirstSelectedOption();
        RegattaDescriptor regatta = RegattaDescriptor.fromString(option.getAttribute("value"));
        
        return regatta;
    }
    
    public void setReggataForTracking(RegattaDescriptor regatta) {
        Select select = new Select(this.availableRegattasDropDown);
        
        select.selectByValue(regatta.toString());
    }
    
    /**
     * <p>Sets the filter for the trackable races. After the filter is set you can obtain the new resulting list via
     *   {@link #getTrackableRaces}</p>
     * 
     * @param filter
     *   The filter to apply to the trackable races.
     */
    public void setFilterForTrackableRaces(String filter) {
        this.filterTrackableRacesTextField.clear();
        this.filterTrackableRacesTextField.sendKeys(filter);
    }
    
    public void setTrackSettings(boolean trackWind, boolean correctWind, boolean simulateWithNow) {
        setSelection(this.trackWindCheckBox, trackWind);
        setSelection(this.correctWindCheckbox, correctWind);
        setSelection(this.simulateWithNowCheckbox, simulateWithNow);
    }
    
    public void startTrackingForRaces(List<TrackableRaceDescriptor> races) {
        CellTable2<DataEntry> table = getTrackableRacesTable();
        List<DataEntry> entries = table.getEntries();
        Iterator<DataEntry> iterator = entries.iterator();
        
        while(iterator.hasNext()) {
            DataEntry entry = iterator.next();
            
            String event = entry.getColumnContent(0);
            String race = entry.getColumnContent(1);
            String boatClass = entry.getColumnContent(2);
            
            TrackableRaceDescriptor descriptor = new TrackableRaceDescriptor(event, race, boatClass);
            
            if(!races.contains(descriptor))
                iterator.remove();
        }
                
        table.selectEntries(entries);
        
        this.startTrackingButton.click();
        
        waitForAjaxRequests();
    }
    
    private CellTable2<DataEntry> getTrackableRacesTable() {
        return new GenericCellTable<>(this.driver, this.trackableRacesTable, DataEntry.class);
    }
    
    private void setSelection(WebElement checkbox, boolean selected) {
        WebElement input = checkbox.findElement(By.tagName("input"));
        
        if(input.isSelected() != selected)
            input.click();
    }
}
