package com.sap.sailing.selenium.test.pairinglist;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.leaderboard.LeaderboardConfigurationPanelPO.LeaderboardEntryPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.SeriesEditDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.PairinfListCreationDialogPO;
import com.sap.sailing.selenium.pages.leaderboard.PairingListCreationSetupDialogPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

import junit.framework.Assert;

public class PairinglistTest extends AbstractSeleniumTest {
    private static final String EVENT = "TestEvent";
    private static final String EVENT_DESC = "TestEvent";
    private static final String VENUE = "Walldorf";
    private static final String BOAT_CLASS_49ER = "49er";
    private static final String REGATTA_49ER = "KW 2015 Olympic - 49er"; //$NON-NLS-1$
    private static final String REGATTA_49ER_WITH_SUFFIX = REGATTA_49ER + " ("+BOAT_CLASS_49ER+")"; //$NON-NLS-1$
    private static final Date EVENT_START_TIME = DatatypeConverter.parseDateTime("2015-06-20T08:00:00-00:00")
            .getTime();
    private static final Date EVENT_END_TIME = DatatypeConverter.parseDateTime("2015-06-28T20:00:00-00:00")
            .getTime();
    private static final String SERIES_QUALIFICATION = "Quali";
    private static final String SERIES_MEDALS = "Medals";
    private static final String SERIES_DEFAULT = "Default";
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    @Test
    public void createEventAndTestPOForCalculation() throws InterruptedException {
        final RegattaDescriptor regattaDescriptor = new RegattaDescriptor(REGATTA_49ER, BOAT_CLASS_49ER);
        {
            AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
            EventConfigurationPanelPO events = adminConsolePage.goToEvents();
            events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(EVENT, EVENT_DESC, VENUE,
                    EVENT_START_TIME, EVENT_END_TIME, true, REGATTA_49ER_WITH_SUFFIX, BOAT_CLASS_49ER, EVENT_START_TIME,
                    EVENT_END_TIME, false);
            final RegattaStructureManagementPanelPO regattaStructurePanel = adminConsolePage.goToRegattaStructure();
            final RegattaDetailsCompositePO regattaDetails = regattaStructurePanel.getRegattaDetails(regattaDescriptor);
            regattaDetails.deleteSeries(SERIES_DEFAULT);
            RegattaEditDialogPO editRegatta = regattaStructurePanel.getRegattaList().editRegatta(regattaDescriptor);
            editRegatta.addSeries(SERIES_QUALIFICATION);
            editRegatta.addSeries(SERIES_MEDALS);
            editRegatta.pressOk();

            final SeriesEditDialogPO editSeriesQualification = regattaDetails.editSeries(SERIES_QUALIFICATION);
            editSeriesQualification.addRaces(1, 12, "Q");
            editSeriesQualification.pressOk();

            final SeriesEditDialogPO editSeriesMedals = regattaDetails.editSeries(SERIES_MEDALS);
            editSeriesMedals.setMedalSeries(true);
            editSeriesMedals.addSingleRace("M");
            editSeriesMedals.pressOk();
            LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = adminConsolePage
                    .goToLeaderboardConfiguration();
            LeaderboardEntryPO leaderboardEntryPO = leaderboardConfigurationPanelPO.getLeaderboardTable()
                    .getEntry(REGATTA_49ER_WITH_SUFFIX);
            PairingListCreationSetupDialogPO dialog = leaderboardEntryPO.getLeaderboardPairingListCreationSetupDialog();
            Assert.assertTrue(!dialog.isOkButtonEnabled());
            dialog.setCompetitorsCount("1");
            Assert.assertTrue(dialog.isOkButtonEnabled());
            dialog.setCompetitorsCount("-1");
            Assert.assertTrue(!dialog.isOkButtonEnabled());
            dialog.setCompetitorsCount("17");
            Assert.assertTrue(dialog.isOkButtonEnabled());
            dialog.setCompetitorsCount("18");
            Assert.assertTrue(dialog.isOkButtonEnabled());
            PairinfListCreationDialogPO dialog2 = dialog.pressOk();
            Assert.assertEquals("12", dialog2.getValueOfFlightsLabel());
            Assert.assertEquals("1", dialog2.getValueOfGroupsLabel());
            Assert.assertEquals("18", dialog2.getValueOfCompetitorsLabel());
            dialog2.pressClose();
        }
        {
            AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
            LeaderboardConfigurationPanelPO leaderboardConfigurationPanelPO = adminConsolePage
                    .goToLeaderboardConfiguration();
            LeaderboardEntryPO leaderboardEntryPO = leaderboardConfigurationPanelPO.getLeaderboardTable()
                    .getEntry(REGATTA_49ER_WITH_SUFFIX);
            PairingListCreationSetupDialogPO dialog = leaderboardEntryPO.getLeaderboardPairingListCreationSetupDialog();
            dialog.setCompetitorsCount("18");
            Assert.assertTrue(!dialog.isFlightMultiplierBoxEnabled());
            for (int i = 0; i < 1000; i++) {
                dialog.clickFlightMultiplierCheckBox();
                if (dialog.getValueOfFlightMultiplierCheckBox()) {
                    break;
                }
            }
            Assert.assertTrue(dialog.isFlightMultiplierBoxEnabled());
            Thread.sleep(5000);
            dialog.setFlightMultiplier("0");
            Assert.assertTrue(!dialog.isOkButtonEnabled());
            dialog.setFlightMultiplier("2");
            Assert.assertTrue(dialog.isOkButtonEnabled());
            PairinfListCreationDialogPO dialog2 = dialog.pressOk();
            Assert.assertEquals("12", dialog2.getValueOfFlightsLabel());
            Assert.assertEquals("1", dialog2.getValueOfGroupsLabel());
            Assert.assertEquals("18", dialog2.getValueOfCompetitorsLabel());
            Assert.assertEquals("2", dialog2.getValueOfMultiplerLabel());
            dialog2.pressClose();
        }
    }
}
