package com.sap.sailing.selenium.test.adminconsole;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
import com.sap.sailing.selenium.pages.adminconsole.connectors.SmartphoneTrackingEventManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.event.EventConfigurationPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.racemanagementapp.DeviceConfigurationCreateDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.racemanagementapp.DeviceConfigurationDetailsAreaPO;
import com.sap.sailing.selenium.pages.adminconsole.racemanagementapp.DeviceConfigurationQRCodeDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.racemanagementapp.RaceManagementAppPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaDetailsCompositePO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaEditDialogPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaListCompositePO.RegattaDescriptor;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegattaStructureManagementPanelPO;
import com.sap.sailing.selenium.pages.adminconsole.regatta.RegistrationLinkWithQRCodeDialogPO;
import com.sap.sailing.selenium.pages.gwt.DataEntryPO;
import com.sap.sailing.selenium.pages.home.HomePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.AddCompetitorWithBoatDialogPO;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.AddDeviceMappingsDialogPO;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.MapDevicesDialogPO;
import com.sap.sailing.selenium.test.adminconsole.smartphonetracking.RegisterCompetitorsDialogPO;

/**
 * There are various link creation logic in AdminConsole. This test is to cover them.
 */
public class TestLinkCreation extends AbstractSeleniumTest {

    private static final String DEVICE_CONFIG_NAME = "Test";
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
    private static final String EXPECTED_PUPLIC_INVITE_QR_CODE_TITLE = "Welcome to the public regatta Audi Business Cup (J70) (J70)";    
//    private static final String EXPECTED_RACE_MANAGER_APP_QR_CODE_TITLE = "Welcome to the Race Manager App registration";
//    private static final String EXPECTED_DEVICE_REGISTRATION_QR_CODE_TITLE = "Welcome Competitor Test to the regatta Audi Business Cup (J70) (J70)";
    private static final String EXPECTED_QR_LINK_TEXT = "Please scan this QR Code with your mobile device to proceed with the registration";

    private static final String CHECK_RACE_APP_URL_REGEX = "^https:\\/\\/racemanager-app.sapsailing.com\\/invite\\"
            + "?server_url=http:\\/\\/localhost(:\\d{0,5})?" 
            + "&device_config_identifier=" + DEVICE_CONFIG_NAME
            + "&device_config_uuid=(\\w|\\d|-)*";

    private static final String DEVICE_REGISTRATION_URL_REGEX = ".*https:\\/\\/sailinsight30-app.sapsailing.com\\/invite\\?checkinUrl=.*";

    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
    }

    /**
     * Test the creation of an invitation link.
     * <p>
     * Please notice, that the test checks the created invitation link by calling it and checking over the redirects
     * from branch.io over production environment (my.sapsailing.com) back to localhost. The link back to localhost need
     * an additional confirmation on production server.
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
        HomePage.goToHomeUrl(getWebDriver(), createdInvitationUrl);
        Wait<WebDriver> wait = new WebDriverWait(getWebDriver(), 30);
        // confirm dialog on my.sapsailing.com to redirect back to localhost
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[contains(text(), 'Yes')]"), 1)).get(0)
                .click();
        // here we are back on localhost (Home.html#QRCodePlace)
        wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//div[contains(text(), '" + EXPECTED_PUPLIC_INVITE_QR_CODE_TITLE + "')]")));
        WebElement qrCodeLink = wait.until(ExpectedConditions
                .presenceOfElementLocated(By.xpath("//a[contains(text(), '" + EXPECTED_QR_LINK_TEXT + "')]")));
        Assert.assertTrue(qrCodeLink.getAttribute("href").startsWith(INVITATION_QR_CODE_BASE));
        Assert.assertTrue(qrCodeLink.getAttribute("href").contains("secret=" + secret));
        Assert.assertTrue(qrCodeLink.getAttribute("href").contains("server=http%3A%2F%2Flocalhost%3A"));
    }

    /**
     * Testing the generation of an invitation QR code for the Race Manager App.
     */
    @Test
    public void testRaceManagerAppInvitationLink() {
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        RaceManagementAppPanelPO raceManagerApp = adminConsole.goToRaceManagerApp();
        DeviceConfigurationCreateDialogPO createDeviceConfiguration = raceManagerApp.createDeviceConfiguration();
        createDeviceConfiguration.setDeviceName(DEVICE_CONFIG_NAME);
        createDeviceConfiguration.clickOkButtonOrThrow();
        DeviceConfigurationDetailsAreaPO deviceConfigurationDetails = raceManagerApp.getDeviceConfigurationDetails();
        DeviceConfigurationQRCodeDialogPO qrCodeDialog = deviceConfigurationDetails.openQRCodeDialog();
        Matcher<String> matcher = Matchers.matchesRegex(CHECK_RACE_APP_URL_REGEX);
        String createdInvitationUrl = qrCodeDialog.getUrl();
        MatcherAssert.assertThat("Check URL",  matcher.matches(createdInvitationUrl));
        HomePage.goToHomeUrl(getWebDriver(), createdInvitationUrl);
        /* Activate after setting up branch.io correctly */
        // TODO: reactivate after branch.io is handling Race Manager App links the new way
//        Wait<WebDriver> wait = new WebDriverWait(getWebDriver(), 30);
//        // confirm dialog on my.sapsailing.com to redirect back to localhost
//        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[contains(text(), 'Yes')]"), 1)).get(0)
//                .click();
//        // here we are back on localhost (Home.html#QRCodePlace)
//        wait.until(ExpectedConditions
//                .presenceOfElementLocated(By.xpath("//div[contains(text(), '" + EXPECTED_RACE_MANAGER_APP_QR_CODE_TITLE + "')]")));
//        WebElement qrCodeLink = wait.until(ExpectedConditions
//                .presenceOfElementLocated(By.xpath("//a[contains(text(), '" + EXPECTED_QR_LINK_TEXT + "')]")));
//        MatcherAssert.assertThat("Check URL",  matcher.matches(qrCodeLink));
        
    }
    
    /**
     * @throws InterruptedException 
     * 
     */
    @Test
    public void testDeviceRegistation() throws InterruptedException {
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
        SmartphoneTrackingEventManagementPanelPO smartphoneTrackingPanel = adminConsole.goToSmartphoneTrackingPanel();
        DataEntryPO aLeaderboard = smartphoneTrackingPanel.getLeaderboardTable().getEntries().get(0);
        // Add a competitor with boat as precondition for adding devices
        RegisterCompetitorsDialogPO registerCompetitorsDialogPO = smartphoneTrackingPanel.pushCompetitorRegistrationsActionButton(aLeaderboard);
        AddCompetitorWithBoatDialogPO addCompetitorWithBoatDialogPO = registerCompetitorsDialogPO.openAddCompetitorWithBoatDialog();
        addCompetitorWithBoatDialogPO.addCompetitorWithBoat();
        Thread.sleep(200L);
        registerCompetitorsDialogPO.clickOkButtonOrThrow();
        // Add device
        aLeaderboard = smartphoneTrackingPanel.getLeaderboardTable().getEntries().get(0);
        MapDevicesDialogPO mapDevicesDialog = smartphoneTrackingPanel.pushMapDevicesActionButton(aLeaderboard);
        AddDeviceMappingsDialogPO addDeviceMappingsDialog = mapDevicesDialog.addMapping();
        // check URL based on boat selection
        addDeviceMappingsDialog.getBoatsTable().getEntries().get(0).select();
        String boatUrlPattern = DEVICE_REGISTRATION_URL_REGEX + "boat_id.*";
        Matcher<String> boatMatcher = Matchers.matchesRegex(boatUrlPattern);
        MatcherAssert.assertThat("Check URL",  boatMatcher.matches(addDeviceMappingsDialog.getQrCodeUrl(boatUrlPattern)));
        // check URL based on competitor selection
        addDeviceMappingsDialog.getCompetitorTable().getEntries().get(0).select();
        String compatitorUrlPattern = DEVICE_REGISTRATION_URL_REGEX + "competitor_id.*";
        Matcher<String> competitorMater = Matchers.matchesRegex(compatitorUrlPattern);
        String qrCodeUrl = addDeviceMappingsDialog.getQrCodeUrl(compatitorUrlPattern);
        MatcherAssert.assertThat("Check URL",  competitorMater.matches(qrCodeUrl));
        // check redirect
        HomePage.goToHomeUrl(getWebDriver(), qrCodeUrl);
        Wait<WebDriver> wait = new WebDriverWait(getWebDriver(), 30);
        // confirm dialog on my.sapsailing.com to redirect back to localhost
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//button[contains(text(), 'Yes')]"), 1)).get(0)
                .click();
        // here we are back on localhost (Home.html#QRCodePlace)
        // TODO: reactivate after fixing the problem that the link called by Selenium test is resulting in a different server behavior than manual request
//        wait.until(ExpectedConditions
//                .presenceOfElementLocated(By.xpath("//div[contains(text(), '" + EXPECTED_DEVICE_REGISTRATION_QR_CODE_TITLE + "')]")));
//        WebElement qrCodeLink = wait.until(ExpectedConditions
//                .presenceOfElementLocated(By.xpath("//a[contains(text(), '" + EXPECTED_QR_LINK_TEXT + "')]")));
//        Matcher<String> matcher = Matchers.matchesRegex(compatitorUrlPattern);
//        MatcherAssert.assertThat("Check URL",  matcher.matches(qrCodeLink));
    }
}
