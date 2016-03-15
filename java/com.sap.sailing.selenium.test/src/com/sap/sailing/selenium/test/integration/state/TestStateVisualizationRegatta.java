package com.sap.sailing.selenium.test.integration.state;

import static com.sap.sailing.selenium.pages.common.DateHelper.getFutureDate;
import static com.sap.sailing.selenium.pages.common.DateHelper.getPastDate;

import java.util.Date;
import java.util.function.Predicate;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardDetailsPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.SetStartTimeDialogPO;
import com.sap.sailing.selenium.pages.common.LabelTypePO;
import com.sap.sailing.selenium.pages.home.event.RegattaListItemPO;
import com.sap.sailing.selenium.pages.home.event.regatta.RegattaEventPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestStateVisualizationRegatta extends AbstractSeleniumTest {

    @Override
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testStateFlagOfUpcomingRegatta() {
        TestCase testCase = new TestCase();
        testCase.createTestEventWithRegatta("TestRegatta", getFutureDate(3), getFutureDate(7));
        testCase.assertStateFlagOfRegatta("TestRegatta", LabelTypePO::isUpcoming);
    }

    // @Test
    public void testStateFlagOfInProgressRegatta() {
        // TODO
    }

    @Test
    public void testStateFlagOfLiveRegatta() {
        TestCase testCase = new TestCase();
        testCase.createTestEventWithRegatta("TestLiveRegatta", getPastDate(3), getFutureDate(3));
        testCase.setLeaderboardsRaceStartTime("TestLiveRegatta", "R1", getPastDate(1));
        testCase.assertStateFlagOfRegatta("TestLiveRegatta", LabelTypePO::isLive);
    }

    @Test
    public void testStateFlagOfFinishedRegatta() {
        TestCase testCase = new TestCase();
        testCase.createTestEventWithRegatta("TestRegatta", getPastDate(3), getPastDate(7));
        testCase.assertStateFlagOfRegatta("TestRegatta", LabelTypePO::isFinished);
    }
    
    private class TestCase {
        private final AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        private String eventUrl; 
        
        private void createTestEventWithRegatta(String regattaName, Date regattaStartDate, Date regattaEndDate) {
            EventConfigurationPanelPO eventConfigPanel = adminConsolePage.goToEvents();
            final String eventName = "RegattaStateTestEvent", venue = "Anywhere", boatClass = "J-22";
            eventConfigPanel.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(eventName, null, venue,
                    getPastDate(3), getFutureDate(3), true, regattaName, boatClass, regattaStartDate, regattaEndDate);
            this.eventUrl = eventConfigPanel.getEventEntry(eventName).getEventURL();
        }
        
        private void setLeaderboardsRaceStartTime(String leaderboard, String race, Date startTime) {
            LeaderboardConfigurationPanelPO leaderboardConfiguration = adminConsolePage.goToLeaderboardConfiguration();
            LeaderboardDetailsPanelPO leaderboardDetails = leaderboardConfiguration.getLeaderboardDetails(leaderboard);
            SetStartTimeDialogPO startTimeDialog = leaderboardDetails.getRacesTable().getEntry(race).clickSetStartTime();
            startTimeDialog.setStartTimeValue(startTime);
            startTimeDialog.pressSetStartTime();
        }
        
        private void assertStateFlagOfRegatta(String regattaName, Predicate<LabelTypePO> expectedLabelType) {
            RegattaEventPage regattaEventPage = RegattaEventPage.goToRegattaEventUrl(getWebDriver(), eventUrl);
            RegattaListItemPO regattaListItem = regattaEventPage.getRegattaListItem(regattaName);
            Assert.assertTrue(expectedLabelType.test(regattaListItem.getRegattaHeader().getRegattaStateLabel()));
        }
    }
}
