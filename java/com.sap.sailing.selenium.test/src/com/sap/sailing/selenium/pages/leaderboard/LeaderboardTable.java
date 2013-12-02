package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.gwt.CellTable;
import com.sap.sailing.selenium.pages.gwt.DataEntry;

import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTable.LeaderboardEntry;

public class LeaderboardTable extends CellTable<LeaderboardEntry> {
    public class LeaderboardEntry extends DataEntry {
        private static final String TOTAL_RANK = "Total rank";
        private static final String COMPETITOR = "Competitor";
        private static final String NAME = "Name";
        
        protected LeaderboardEntry(WebElement element) {
            super(LeaderboardTable.this, element);
        }
        
        
        public String getDescriptor() {
            return getCompetitor();
        }
        
        public int getTotalRank() {
            return Integer.parseInt(getColumnContent(TOTAL_RANK));
        }
        
        public String getCompetitor() {
            return getColumnContent(COMPETITOR);
        }
        
        public String getName() {
            return getColumnContent(NAME);
        }
        
//        public int getTotalSailedDistance() {
//        }
//        public void getAverageSpeedOverGround() {
//        }
//        public void getMaximumSpeedOverGround() {
//        }
//        public int getTotalDownwindTime() {
//        }
//        public int getTotalUpwindTime() {
//        }
//        public int getTotalReachingLegsTime() {
//        }
//        public int getTotalSailingTime() {
//        }
//        public int getTotalPoints() {
//        }
    }
    
    public LeaderboardTable(WebDriver driver, WebElement element) {
        super(driver, element);
        
    }
    
    @Override
    protected LeaderboardEntry createDataEntry(WebElement element) {
        return new LeaderboardEntry(element);
    }
}
