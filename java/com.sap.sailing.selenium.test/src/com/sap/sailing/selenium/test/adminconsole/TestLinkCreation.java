package com.sap.sailing.selenium.test.adminconsole;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegistrationLinkWithQRCodeDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

/**
 * There are various link creation logic in AdminConsole. This test is to cover them.
 */
public class TestLinkCreation extends AbstractSeleniumTest {

    private static final String BMW_CUP_EVENT = "BMW Cup";
    private static final String BMW_CUP_BOAT_CLASS = "J80";
    private static final String AUDI_CUP_BOAT_CLASS = "J70";
    private static final String BMW_CUP_REGATTA = "BMW Cup (J80)"; //$NON-NLS-1$
    private static final String AUDI_CUP_REGATTA = "Audi Business Cup (J70)"; //$NON-NLS-1$
    private static final String BMW_CUP_EVENTS_DESC = "BMW Cup Description";
    private static final String BMW_VENUE = "Somewhere";
    private static final Date BMW_START_EVENT_TIME = DatatypeConverter.parseDateTime("2012-04-08T10:09:00-05:00")
            .getTime();
    private static final Date BMW_STOP_EVENT_TIME = DatatypeConverter.parseDateTime("2017-04-08T10:50:00-05:00")
            .getTime();
    private static final String CUSTOM_COURSE_AREA = "Custom X";
    private static final String INVITATION_URL_BASE = "https://sailinsight30-app.sapsailing.com/publicInvite?regatta_name=Audi+Business+Cup+(J70)+(J70)&secret=<secret>&server=http%3A%2F%2Flocalhost%3A8888&event_id=<event-id>";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }
    
    /**
     * Test the creation of an invitation link.
     */
    @Test
    public void testRegattaOverviewInvitationLinkCreation() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, true, BMW_CUP_REGATTA, BMW_CUP_BOAT_CLASS,
                BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, false, CUSTOM_COURSE_AREA);
        RegattaStructureManagementPanelPO regattas = adminConsole.goToRegattaStructure();
        RegattaDescriptor regattaDescriptor = new RegattaDescriptor(AUDI_CUP_REGATTA, AUDI_CUP_BOAT_CLASS);
        regattas.createRegattaAndAddToEvent(regattaDescriptor, BMW_CUP_EVENT, new String[] { CUSTOM_COURSE_AREA });

        RegattaEditDialogPO editRegatta = regattas.getRegattaList().editRegatta(regattaDescriptor);
        String secret = editRegatta.getSecret();
        String selectedEventId = editRegatta.getSelectedEventId();
        editRegatta.clickOkButtonOrThrow();
        // regattas.getRegattaList().selectRegatta(regattaDescriptor);
        RegattaDetailsCompositePO regattaDetails = regattas.getRegattaDetails();
        RegistrationLinkWithQRCodeDialogPO registrationLinkWithQRCode = regattaDetails.configureRegistrationURL();
        String invitationUrl = INVITATION_URL_BASE.replace("<secret>", secret).replace("<event-id>", selectedEventId);
        Assert.assertEquals(invitationUrl, registrationLinkWithQRCode.getRegistrationLinkUrl());
        registrationLinkWithQRCode.clickOkButtonOrThrow();
    }
}
