package com.sap.sailing.selenium.test.integration.state;

import static com.sap.sailing.selenium.pages.common.DateHelper.getFutureDate;
import static com.sap.sailing.selenium.pages.common.DateHelper.getPastDate;

import java.util.Date;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.common.LabelTypePO;
import com.sap.sailing.selenium.pages.home.event.EventPage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestStateVisualisationEvent extends AbstractSeleniumTest {

    @Override
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    @Test
    public void testStateFlagOfUpcomingEvent() {
        testStateFlagOfEvent("Test1", "Somewhere", getFutureDate(3), getFutureDate(7), LabelTypePO::isUpcoming);
    }
    
    @Test
    public void testStateFlagOfLiveEvent() {
        testStateFlagOfEvent("Test2", "Somewhere else", getPastDate(3), getFutureDate(3), LabelTypePO::isLive);
    }
    
    @Test
    public void testStateFlagOfFinishedEvent() {
        testStateFlagOfEvent("Test3", "Anywhere", getPastDate(7), getPastDate(3), LabelTypePO::isFinished);
    }

    private void testStateFlagOfEvent(String name, String venue, Date startDate, Date endDate,
            Predicate<LabelTypePO> expectedLabelType) {
        AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO eventConfigurationPanel = adminConsolePage.goToEvents();
        eventConfigurationPanel.createEmptyEvent(name, null, venue, startDate, endDate, true);
        String eventUrl = eventConfigurationPanel.getEventEntry(name).getEventURL();
        EventPage eventPage = EventPage.goToEventUrl(getWebDriver(), eventUrl);
        Assert.assertTrue(expectedLabelType.test(eventPage.getEventHeader().getEventStateLabel()));
    }

}
