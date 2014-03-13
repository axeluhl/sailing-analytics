package com.sap.sailing.selenium.pages.leaderboard;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.gwt.CellTablePO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;

import com.sap.sailing.selenium.pages.leaderboard.LeaderboardTablePO.LeaderboardEntry;

public class LeaderboardTablePO extends CellTablePO<LeaderboardEntry> {
    public class LeaderboardEntry extends DataEntryPO {
        private static final String TOTAL_RANK = "Total rank";
        private static final String COMPETITOR = "Competitor";
        private static final String NAME = "Name";
        
        protected LeaderboardEntry(WebElement element) {
            super(LeaderboardTablePO.this, element);
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
    
    public LeaderboardTablePO(WebDriver driver, WebElement element) {
        super(driver, element);
        
    }
    
    @Override
    protected LeaderboardEntry createDataEntry(WebElement element) {
        return new LeaderboardEntry(element);
    }
}
