package com.sap.sailing.selenium.test.integration.state;

import static com.sap.sailing.selenium.pages.common.DateHelper.getFutureDate;
import static com.sap.sailing.selenium.pages.common.DateHelper.getPastDate;

import java.util.Date;
import java.util.function.Predicate;

import junit.framework.Assert;

import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.common.LabelTypePO;
import com.sap.sailing.selenium.pages.home.event.EventPage;
import com.sap.sailing.selenium.pages.home.regatta.RegattaListItemPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestStateVisualizationRegatta extends AbstractSeleniumTest {

    @Override
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testStateFlagOfUpcomingRegatta() {
        testStateFlagOfRegatta("TestRegatta", getFutureDate(3), getFutureDate(7), LabelTypePO::isUpcoming);
    }

    // @Test
    public void testStateFlagOfInProgressRegatta() {
        // TODO
    }

    // @Test
    public void testStateFlagOfLiveRegatta() {
        // TODO
    }

    @Test
    public void testStateFlagOfFinishedRegatta() {
        testStateFlagOfRegatta("TestRegatta", getPastDate(7), getPastDate(3), LabelTypePO::isFinished);
    }
    
    private void testStateFlagOfRegatta(String regattaName, Date regattaStartDate, Date regattaEndDate,
            Predicate<LabelTypePO> expectedLabelType) {
        AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO eventConfigurationPanel = adminConsolePage.goToEvents();
        final String eventName = "RegattaStateTestEvent", venue = "Anywhere", boatClass = "J-22";
        eventConfigurationPanel.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(eventName, null,
                venue, getPastDate(3), getFutureDate(3), true, regattaName, boatClass, regattaStartDate, regattaEndDate);
        String eventUrl = eventConfigurationPanel.getEventEntry(eventName).getEventURL();
        EventPage eventPage = EventPage.goToEventUrl(getWebDriver(), eventUrl);
        RegattaListItemPO regattaListItem = eventPage.getRegattaListItem(regattaName);
        Assert.assertTrue(expectedLabelType.test(regattaListItem.getRegattaHeader().getRegattaStateLabel()));
    }
    
}
