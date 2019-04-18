package com.sap.sailing.selenium.api.test;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.GPSFixApi;
import com.sap.sailing.selenium.api.event.GPSFixApi.GpsFixResponse;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.event.RegattaApi;
import com.sap.sailing.selenium.api.event.RegattaApi.Competitor;
import com.sap.sailing.selenium.api.event.RegattaApi.RaceColumn;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class OpenRegattaTest extends AbstractSeleniumTest {

    private ApiContext adminSecurityCtx;
    private ApiContext ownerCtx;
    private ApiContext sailorCtx;

    private SecurityApi securityApi = new SecurityApi();
    private EventApi eventApi = new EventApi();
    private RegattaApi regattaApi = new RegattaApi();
    private LeaderboardApi leaderboardApi = new LeaderboardApi();
    private GPSFixApi gpsFixApi = new GPSFixApi();

    private static final String EVENT_NAME = "Duckburg 2019 Everybody's Regatta";
    private static final String BOAT_CLASS = "49er";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminSecurityCtx = ApiContext.createApiContext(getContextRoot(), ApiContext.SECURITY_CONTEXT, "admin", "admin");
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        ownerCtx = ApiContext.createApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT, "donald", "daisy0815");
        sailorCtx = ApiContext.createAnonymousApiContext(getContextRoot(), ApiContext.SERVER_CONTEXT);
        AdminConsolePage adminConsole = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLcalServerPanel().setSelfServiceServer(true);
    }

    @Test
    public void simpleTest() {
        UUID deviceUuidCompetitor1 = UUID.randomUUID();
        UUID deviceUuidCompetitor2 = UUID.randomUUID();
        Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS,
                CompetitorRegistrationType.OPEN_UNMODERATED, "Duckburg Harbour");
        String registrationLinkSecret = event.getSecret();
        RaceColumn race = regattaApi.addRaceColumn(ownerCtx, EVENT_NAME, null, 1)[0];

        Competitor competitor1 = regattaApi.createAndAddCompetitorWithSecret(ownerCtx, EVENT_NAME, BOAT_CLASS,
                /* email */ null, "Donald Duck", "USA", registrationLinkSecret, deviceUuidCompetitor1);
        Competitor competitor2 = regattaApi.createAndAddCompetitorWithSecret(sailorCtx, EVENT_NAME, BOAT_CLASS,
                /* email */ null, "Mickey Mouse", "USA", registrationLinkSecret, deviceUuidCompetitor2);

        leaderboardApi.deviceMappingsStart(ownerCtx, EVENT_NAME, competitor1.getId(), competitor1.getBoat().getId(),
                /* markId */ null, deviceUuidCompetitor1, registrationLinkSecret, System.currentTimeMillis());

        leaderboardApi.deviceMappingsStart(sailorCtx, EVENT_NAME, competitor2.getId(), competitor2.getBoat().getId(),
                /* markId */ null, deviceUuidCompetitor2, registrationLinkSecret, System.currentTimeMillis());

        leaderboardApi.setTrackingTimes(ownerCtx, EVENT_NAME, race.getRaceName(), "Default", System.currentTimeMillis(),
                null);
        leaderboardApi.startRaceLogTracking(ownerCtx, EVENT_NAME, race.getRaceName(), "Default");

        for (double i = 0.0; i < 100.0; i++) {
            GpsFixResponse fix;
            fix = gpsFixApi.postGpsFix(ownerCtx, deviceUuidCompetitor1, gpsFixApi.new GpsFix(49.12 + i / 1000.0,
                    8.599 + i / 1000.0, System.currentTimeMillis(), 10.0, 180.0));
            System.out.println(fix.getJson().toJSONString());
            fix = gpsFixApi.postGpsFix(sailorCtx, deviceUuidCompetitor2, gpsFixApi.new GpsFix(49.12 + i / 1000.0,
                    8.599 + i / 1000.0, System.currentTimeMillis(), 10.0, 180.0));
            System.out.println(fix.getJson().toJSONString());
        }

        leaderboardApi.deviceMappingsEnd(ownerCtx, EVENT_NAME, competitor1.getId(), competitor1.getBoat().getId(),
                /* markId */ null, deviceUuidCompetitor1, registrationLinkSecret, System.currentTimeMillis());

        leaderboardApi.deviceMappingsEnd(sailorCtx, EVENT_NAME, competitor2.getId(), competitor2.getBoat().getId(),
                /* markId */ null, deviceUuidCompetitor2, registrationLinkSecret, System.currentTimeMillis());
    }

}
