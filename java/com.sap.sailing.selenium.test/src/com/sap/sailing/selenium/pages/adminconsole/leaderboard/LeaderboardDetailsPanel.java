package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

import com.sap.sailing.selenium.pages.adminconsole.Actions;

import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList.TrackedRaceDescriptor;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;
import com.sap.sailing.selenium.pages.gwt.GenericCellTable;

public class LeaderboardDetailsPanel extends PageArea {
    public static class RaceDescriptor {
        private final String name;
        private final String fleet;
        private final boolean medalRace;
        private final boolean linked;
        private final double factor;
                
        public RaceDescriptor(String name, String fleet, boolean medalRace, boolean linked, double factor) {
            this.name = name;
            this.fleet = fleet;
            this.medalRace = medalRace;
            this.linked = linked;
            this.factor = factor;
        }
        
        public String getName() {
            return this.name;
        }
        
        public String getFleet() {
            return this.fleet;
        }
        
        public boolean isMedalRace() {
            return this.medalRace;
        }
        
        public boolean isLinked() {
            return this.linked;
        }
        
        public double getFactor() {
            return this.factor;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.fleet);
        }

        @Override
        public boolean equals(Object object) {
            if (this == object)
                return true;
            
            if (object == null)
                return false;
            
            if (getClass() != object.getClass())
                return false;
            
            RaceDescriptor other = (RaceDescriptor) object;
            
            if(!Objects.equals(this.name, other.name))
                return false;
            
            if(!Objects.equals(this.fleet, other.fleet))
                return false;
            
            return true;
        }
    }
    
    @FindBy(how = BySeleniumId.class, using = "RacesCellTable")
    private WebElement racesCellTable;
    
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListComposite;
    
    public LeaderboardDetailsPanel(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public List<RaceDescriptor> getRaces() {
        List<RaceDescriptor> result = new ArrayList<>();
        
        CellTable<DataEntry> racesTable = getRacesTable();
        
        for(DataEntry entry : racesTable.getEntries()) {
            String name = entry.getColumnContent(0);
            String fleet = entry.getColumnContent(1);
            //String medalRace = entry.getColumnContent(2);
            //String linked = entry.getColumnContent(3);
            //String factor = entry.getColumnContent(4);
            
            result.add(new RaceDescriptor(name, fleet, false, false, 0.0));
        }
        
        return result;
    }
    
    
    
//    public RaceEditDialog editRace(RaceDescriptor race) {
//        
//        WebElement action = Actions.findEditAction(findRace(race));
//        
//        action.click();
//    }
    
    public void unlinkRace(RaceDescriptor race) {
        DataEntry entry = findRace(race);
        
        if(entry != null) {
            WebElement action = Actions.findUnlinkRaceAction(entry.getWebElement());
            
            action.click();
        }
    }
    
    public void refreshRaceLog(RaceDescriptor race) {
        DataEntry entry = findRace(race);
        
        if(entry != null) {
            WebElement action = Actions.findRefreshAction(entry.getWebElement());
            
            action.click();
            
            // QUESTION [D049941]: What else can happen?
            Actions.acceptAlert(this.driver);
        }
    }
    
    public void linkRace(RaceDescriptor race, TrackedRaceDescriptor tracking) {
        DataEntry raceRow = findRace(race);
        DataEntry trackingRow = findTracking(tracking);
        
        if(raceRow == null || trackingRow == null) {
        }
        
        raceRow.select();
        trackingRow.select();
    }
    
    private DataEntry findRace(RaceDescriptor race) {
        CellTable<DataEntry> racesTable = getRacesTable();
        
        for(DataEntry entry : racesTable.getEntries()) {
            String name = entry.getColumnContent(0);
            String fleet = entry.getColumnContent(1);
            //String medalRace = entry.getColumnContent(2);
            //String linked = entry.getColumnContent(3);
            //String factor = entry.getColumnContent(4);
            
            RaceDescriptor descriptor = new RaceDescriptor(name, fleet, false, false, 0.0);
            
            if(descriptor.equals(race))
                return entry;
        }
        
        return null;
    }
    
    private DataEntry findTracking(TrackedRaceDescriptor tracking) {
        CellTable<DataEntry> trackingTable = getTrackedRacesTable();
        
        for(DataEntry entry : trackingTable.getEntries()) {
            String regatta = entry.getColumnContent(0);
            String boatClass = entry.getColumnContent(1);
            String raceName = entry.getColumnContent(2);
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, raceName);
            
            if(descriptor.equals(tracking))
                return entry;
        }
        
        return null;
    }
    
    private CellTable<DataEntry> getRacesTable() {
        return new GenericCellTable<>(this.driver, this.racesCellTable, DataEntry.class);
    }
    
    private CellTable<DataEntry> getTrackedRacesTable() {
        WebElement table = findElementBySeleniumId(this.trackedRacesListComposite, "TrackedRacesCellTable");
        
        return new GenericCellTable<>(this.driver, table, DataEntry.class);
    }
}
