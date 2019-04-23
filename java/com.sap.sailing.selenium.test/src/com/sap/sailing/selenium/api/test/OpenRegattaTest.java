package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.domain.common.CompetitorRegistrationType.OPEN_UNMODERATED;
import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAnonymousApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.api.core.GpsFixMoving.createFix;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.GpsFixApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi.DeviceMappingRequest;
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Competitor;
import com.sap.sailing.selenium.api.event.RegattaApi.RaceColumn;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class OpenRegattaTest extends AbstractSeleniumTest {

    private ApiContext ownerCtx;
    private ApiContext sailorCtx;

    private final SecurityApi securityApi = new SecurityApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi leaderboardApi = new LeaderboardApi();
    private final GpsFixApi gpsFixApi = new GpsFixApi();

    private static final String EVENT_NAME = "Duckburg 2019 Everybody's Regatta";
    private static final String BOAT_CLASS = "49er";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        final ApiContext adminSecurityCtx = createApiContext(getContextRoot(), SECURITY_CONTEXT, "admin", "admin");
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        sailorCtx = createAnonymousApiContext(getContextRoot(), SERVER_CONTEXT);
        final AdminConsolePage adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
    }

    @Test
    public void simpleTest() {
        final UUID deviceUuidCompetitor1 = randomUUID();
        final UUID deviceUuidCompetitor2 = randomUUID();
        final Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, OPEN_UNMODERATED,
                "Duckburg Harbour");
        final String registrationLinkSecret = event.getSecret();
        final RaceColumn race = regattaApi.addRaceColumn(ownerCtx, EVENT_NAME, null, 1)[0];

        final Competitor competitor1 = regattaApi.createAndAddCompetitorWithSecret(ownerCtx, EVENT_NAME, BOAT_CLASS,
                /* email */ null, "Donald Duck", "USA", registrationLinkSecret, deviceUuidCompetitor1);
        final Competitor competitor2 = regattaApi.createAndAddCompetitorWithSecret(sailorCtx, EVENT_NAME, BOAT_CLASS,
                /* email */ null, "Mickey Mouse", "USA", registrationLinkSecret, deviceUuidCompetitor2);

        final DeviceMappingRequest devideMappingRequestOwner = leaderboardApi
                .createDeviceMappingRequest(ownerCtx, EVENT_NAME).forCompetitor(competitor1.getId())
                .withDeviceUuid(deviceUuidCompetitor1);
        final DeviceMappingRequest deviceMappingRequestSailor = leaderboardApi
                .createDeviceMappingRequest(sailorCtx, EVENT_NAME).forCompetitor(competitor2.getId())
                .withDeviceUuid(deviceUuidCompetitor2).withSecret(registrationLinkSecret);

        devideMappingRequestOwner.startDeviceMapping(currentTimeMillis());
        deviceMappingRequestSailor.startDeviceMapping(currentTimeMillis());

        leaderboardApi.setTrackingTimes(ownerCtx, EVENT_NAME, race.getRaceName(), "Default", currentTimeMillis(), null);
        leaderboardApi.startRaceLogTracking(ownerCtx, EVENT_NAME, race.getRaceName(), "Default");

        for (double i = 0.0; i < 100.0; i++) {
            final Double longitude = 9.12 + i / 1000.0, latitude = .599 + i / 1000.0, speed = 10.0, course = 180.0;
            gpsFixApi.postGpsFix(ownerCtx, deviceUuidCompetitor1,
                    createFix(longitude, latitude, currentTimeMillis(), speed, course));
            gpsFixApi.postGpsFix(sailorCtx, deviceUuidCompetitor2,
                    createFix(longitude, latitude, currentTimeMillis(), speed, course));
        }

        devideMappingRequestOwner.startDeviceMapping(currentTimeMillis());
        deviceMappingRequestSailor.startDeviceMapping(currentTimeMillis());
    }

}
