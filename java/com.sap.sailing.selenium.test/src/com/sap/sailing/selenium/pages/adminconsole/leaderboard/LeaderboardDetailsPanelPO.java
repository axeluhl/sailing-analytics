package com.sap.sailing.selenium.pages.adminconsole.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.adminconsole.ActionsHelper;
import com.sap.sailing.selenium.pages.adminconsole.tracking.TrackedRacesListPO.TrackedRaceDescriptor;
import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.gwt.GenericCellTablePO;
import com.sap.sse.common.Util;

public class LeaderboardDetailsPanelPO extends PageArea {
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
    
    // TODO: Only available for flexible leader boards
    @FindBy(how = BySeleniumId.class, using = "AddRacesButton")
    private WebElement addRacesButton;
    
    @FindBy(how = BySeleniumId.class, using = "TrackedRacesListComposite")
    private WebElement trackedRacesListComposite;
    
    public LeaderboardDetailsPanelPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
//    /**
//     * Assuming that the leaderboard is already selected, clicks the "Add Races..." button 
//     * @param i
//     */
//    // TODO: Must be a flexible leaderboard
    public void addRacesToFlexibleLeaderboard(int i) {
        this.addRacesButton.click();
        WebElement dialog = findElementBySeleniumId(this.driver, "RaceColumnsInLeaderboardDialog");
        RaceColumnsInLeaderboardDialog raceColumnsInLeaderboardDialog = new RaceColumnsInLeaderboardDialog(this.driver, dialog);
        raceColumnsInLeaderboardDialog.addRaces(2);
        //selectRaceColumn("R1", "Default");
    }
//    
//    public void selectRaceColumn(String raceColumnName, String fleetName) {
//        CellTable table = getRaceColumnsTable();
//        for (WebElement row : table.getRows()) {
//            List<WebElement> fields = row.findElements(By.tagName("td"));
//            if (raceColumnName.equals(fields.get(0).getText()) && fleetName.equals(fields.get(1).getText())) {
//                fields.get(1).click();
//                break;
//            }
//        }
//    }
    
    public List<RaceDescriptor> getRaces() {
        List<RaceDescriptor> result = new ArrayList<>();
        CellTablePO<DataEntryPO> racesTable = getRacesTable();
        for(DataEntryPO entry : racesTable.getEntries()) {
            result.add(createRaceDescriptor(entry));
        }
        return result;
    }

    private RaceDescriptor createRaceDescriptor(DataEntryPO entry) {
        String name = entry.getColumnContent("Race");
        String fleet = entry.getColumnContent("Fleet");
        String medalRace = entry.getColumnContent("Medal Race");
        String linked = entry.getColumnContent("Is linked");
        String factorColumnContent = entry.getColumnContent("Factor");
        double factor = factorColumnContent.trim().isEmpty() ? 0.0 : Double.valueOf(factorColumnContent);
        return new RaceDescriptor(name, fleet, Util.equalsWithNull("x", medalRace), Util.equalsWithNull("Yes", linked), factor);
    }
    
    
//    public RaceEditDialog editRace(RaceDescriptor race) {
//        
//        WebElement action = Actions.findEditAction(findRace(race));
//        
//        action.click();
//    }
    
    public void unlinkRace(RaceDescriptor race) {
        DataEntryPO entry = findRace(race);
        if (entry != null) {
            WebElement action = ActionsHelper.findUnlinkRaceAction(entry.getWebElement());
            action.click();
            waitForAjaxRequests(); // unlinking update the leaderboard details table; wait for callback to finish
        }
    }
    
    public void refreshRaceLog(RaceDescriptor race) {
        DataEntryPO entry = findRace(race);
        
        if(entry != null) {
            WebElement action = ActionsHelper.findRefreshAction(entry.getWebElement());
            
            action.click();
            
            // QUESTION [D049941]: What else can happen?
            ActionsHelper.acceptAlert(this.driver);
        }
    }
    
    public void linkRace(RaceDescriptor race, TrackedRaceDescriptor tracking) {
        DataEntryPO raceRow = findRace(race);
        DataEntryPO trackingRow = findTracking(tracking);
        if(raceRow == null || trackingRow == null) {
        }
        raceRow.select();
        trackingRow.select();
        waitForAjaxRequests(); // the selection will update elements of the cell table; wait until the callback has been received
    }
    
    private DataEntryPO findRace(RaceDescriptor race) {
        CellTablePO<DataEntryPO> racesTable = getRacesTable();
        for(DataEntryPO entry : racesTable.getEntries()) {
            RaceDescriptor descriptor = createRaceDescriptor(entry);
            if (descriptor.equals(race)) {
                return entry;
            }
        }
        return null;
    }
    
    private DataEntryPO findTracking(TrackedRaceDescriptor tracking) {
        CellTablePO<DataEntryPO> trackingTable = getTrackedRacesTable();
        
        for(DataEntryPO entry : trackingTable.getEntries()) {
            String regatta = entry.getColumnContent("Regatta");
            String boatClass = entry.getColumnContent("Boat Class");
            String raceName = entry.getColumnContent("Race");
            
            TrackedRaceDescriptor descriptor = new TrackedRaceDescriptor(regatta, boatClass, raceName);
            
            if(descriptor.equals(tracking))
                return entry;
        }
        
        return null;
    }
    
    private CellTablePO<DataEntryPO> getRacesTable() {
        return new GenericCellTablePO<>(this.driver, this.racesCellTable, DataEntryPO.class);
    }
    
    private CellTablePO<DataEntryPO> getTrackedRacesTable() {
        WebElement table = findElementBySeleniumId(this.trackedRacesListComposite, "TrackedRacesCellTable");
        
        return new GenericCellTablePO<>(this.driver, table, DataEntryPO.class);
    }
}
