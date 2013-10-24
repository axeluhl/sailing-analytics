package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.Actions;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesList.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.gwt.CellTable;

public class LeaderboardDetails extends PageArea {
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
    
    @FindBy(how = BySeleniumId.class, using = "RacesTable")
    private WebElement racesTable;
    
    @FindBy(how = ByXPath.class, using = "./../..//*[@selenium-id='TrackedRacesPanel']")
    private WebElement trackedRacesPanel;
    
    public LeaderboardDetails(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public List<RaceDescriptor> getRaces() {
        List<RaceDescriptor> result = new ArrayList<>();
        
        CellTable table = new CellTable(this.driver, this.racesTable);
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            
            String name = columns.get(0).getText();
            String fleet = columns.get(1).getText();
            String medalRace = columns.get(2).getText();
            String linked = columns.get(3).getText();
            String factor = columns.get(4).getText();
            
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
        WebElement action = Actions.findUnlinkRaceAction(findRace(race));
        
        action.click();
    }
    
    public void refreshRaceLog(RaceDescriptor race) {
        WebElement action = Actions.findRefreshAction(findRace(race));
        
        action.click();
        
        // QUESTION [D049941]: What else can happen?
        Actions.acceptAlert(this.driver);
    }
    
    public void linkRace(RaceDescriptor race, TrackedRaceDescriptor tracking) {
        CellTable raceTable = new CellTable(this.driver, this.racesTable);
        WebElement raceRow = findRace(race);
        
        CellTable trackingTable = getTrackedRacesTable();
        WebElement trackingRow = findTracking(tracking);
        
        if(raceRow == null || trackingRow == null) {
        }
        
        raceTable.selectRow(raceRow);
        trackingTable.selectRow(trackingRow);
    }
    
    private WebElement findRace(RaceDescriptor race) {
        CellTable table = new CellTable(this.driver, this.racesTable);
        List<WebElement> rows = table.getRows();
        
        for(WebElement row : rows) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            
            String name = columns.get(0).getText();
            String fleet = columns.get(1).getText();
            String medalRace = columns.get(2).getText();
            String linked = columns.get(3).getText();
            String factor = columns.get(4).getText();
            
            RaceDescriptor descriptor = new RaceDescriptor(name, fleet, false, false, 0.0);
            
            if(descriptor.equals(race))
                return row;
        }
        
        return null;
    }
    
    private WebElement findTracking(TrackedRaceDescriptor tracking) {
        CellTable trackingTable = getTrackedRacesTable();
        
        for(WebElement row : trackingTable.getRows()) {
            List<WebElement> columns = row.findElements(By.tagName("td"));
            String regatta = columns.get(0).getText();
            String boatClass = columns.get(1).getText();
            String raceName = columns.get(2).getText();
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, raceName);
            
            if(descriptor.equals(tracking))
                return row;
        }
        
        return null;
    }
    
    private CellTable getTrackedRacesTable() {
        return new CellTable(this.driver, findElementBySeleniumId(this.trackedRacesPanel, "TrackedRacesTable"));
    }
}
