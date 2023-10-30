package com.sap.sailing.selenium.test.adminconsole;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegistrationLinkWithQRCodeDialogPO;
import com.sap.sailing.selenium.pages.home.HomePage;
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
    private static final String INVITATION_URL_BASE = "https://sailinsight30-app.sapsailing.com/publicInvite?regatta_name=Audi+Business+Cup+(J70)+(J70)";
    private static final String INVITATION_QR_CODE_BASE = "https://sailinsight30-app.sapsailing.com/publicInvite?regatta_name=Audi%20Business%20Cup%20(J70)%20(J70)";
    private static final String EXPECTED_QR_CODE_TITLE = "Welcome to the public regatta Audi Business Cup (J70) (J70)!";
    private static final String EXPECTED_QR_CODE_LINK_TEXT = "Please scan this QR Code with your mobile device to proceed with the registration";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    /**
     * Test the creation of an invitation link.
     * <p>
     * Please notice, that the test checks the created invitation link by calling it and checking over the 
     * redirects from branch.io over production environment (my.sapsailing.com) back to localhost. The link back
     * to localhost need an additional confirmation on production server.
     */
    @Test
    public void testRegattaOverviewInvitationLinkCreation() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        // create an event and regatta
        EventConfigurationPanelPO events = adminConsole.goToEvents();
        events.createEventWithDefaultLeaderboardGroupRegattaAndDefaultLeaderboard(BMW_CUP_EVENT, BMW_CUP_EVENTS_DESC,
                BMW_VENUE, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, /* isPublic */ true, BMW_CUP_REGATTA,
                BMW_CUP_BOAT_CLASS, BMW_START_EVENT_TIME, BMW_STOP_EVENT_TIME, /* useOverallLeaderboard */ false,
                CUSTOM_COURSE_AREA);
        RegattaStructureManagementPanelPO regattas = adminConsole.goToRegattaStructure();
        RegattaDescriptor regattaDescriptor = new RegattaDescriptor(AUDI_CUP_REGATTA, AUDI_CUP_BOAT_CLASS);
        regattas.createRegattaAndAddToEvent(regattaDescriptor, BMW_CUP_EVENT, new String[] { CUSTOM_COURSE_AREA });
        // extract secret and event ID
        RegattaEditDialogPO editRegatta = regattas.getRegattaList().editRegatta(regattaDescriptor);
        String secret = editRegatta.getSecret();
        String selectedEventId = editRegatta.getSelectedEventId();
        editRegatta.clickOkButtonOrThrow();
        // create expected link URL based on the secret and event ID
        RegattaDetailsCompositePO regattaDetails = regattas.getRegattaDetails();
        RegistrationLinkWithQRCodeDialogPO registrationLinkWithQRCode = regattaDetails.configureRegistrationURL();
        // check URL
        String createdInvitationUrl = registrationLinkWithQRCode.getRegistrationLinkUrl();
        Assert.assertTrue(createdInvitationUrl.startsWith(INVITATION_URL_BASE));
        Assert.assertTrue(createdInvitationUrl.contains("secret=" + secret));
        Assert.assertTrue(createdInvitationUrl.contains("event_id=" + selectedEventId));
        Assert.assertTrue(createdInvitationUrl.contains("server=http%3A%2F%2Flocalhost%3A"));
        registrationLinkWithQRCode.clickOkButtonOrThrow();
        HomePage.goToPage(getWebDriver(), createdInvitationUrl);
        Wait<WebDriver> wait = new WebDriverWait(getWebDriver(), 20);
        // confirm dialog on my.sapsailing.com to redirect back to localhost
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[contains(text(), 'Yes')]"), 1)).get(0)
                .click();
        // here we are back on localhost (Home.html#QRCodePlace)
        wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//div[contains(text(), '" + EXPECTED_QR_CODE_TITLE + "')]")));
        WebElement qrCodeLink = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//a[contains(text(), '" + EXPECTED_QR_CODE_LINK_TEXT + "')]")));
        Assert.assertTrue(qrCodeLink.getAttribute("href").startsWith(INVITATION_QR_CODE_BASE));
        Assert.assertTrue(qrCodeLink.getAttribute("href").contains("secret=" + secret));
        Assert.assertTrue(qrCodeLink.getAttribute("href").contains("server=http%3A%2F%2Flocalhost%3A"));
    }
}
