package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.domain.common.CompetitorRegistrationType.OPEN_UNMODERATED;
import static com.sap.sailing.selenium.api.core.ApiContext.SECURITY_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createAnonymousApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;
import static com.sap.sailing.selenium.api.core.GpsFixMoving.createFix;
import static com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage.goToPage;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.GpsFixApi;
import com.sap.sailing.selenium.api.event.LeaderboardApi;
import com.sap.sailing.selenium.api.event.Mark;
import com.sap.sailing.selenium.api.event.MarkApi;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.regatta.Competitor;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class OpenRegattaTest extends AbstractSeleniumTest {

    private ApiContext adminCtx;
    private ApiContext ownerCtx;
    private ApiContext sailorCtx;

    private final SecurityApi securityApi = new SecurityApi();
    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final LeaderboardApi leaderboardApi = new LeaderboardApi();
    private final GpsFixApi gpsFixApi = new GpsFixApi();
    private final MarkApi markApi = new MarkApi();

    private static final String EVENT_NAME = "Duckburg 2019 Everybody's Regatta";
    private static final String BOAT_CLASS = "49er";

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "donald", "Donald Duck", null, "daisy0815");
        adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        ownerCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "donald", "daisy0815");
        sailorCtx = createAnonymousApiContext(getContextRoot(), SERVER_CONTEXT);
        final AdminConsolePage adminConsole = goToPage(getWebDriver(), getContextRoot());
        adminConsole.goToLocalServerPanel().setSelfServiceServer(true);
    }

    @Test
    public void simpleTest() throws Exception {
        final UUID deviceUuidCompetitor1 = randomUUID();
        final UUID deviceUuidCompetitor2 = randomUUID();
        final Event event = eventApi.createEvent(ownerCtx, EVENT_NAME, BOAT_CLASS, OPEN_UNMODERATED,
                "Duckburg Harbour");
        final String registrationLinkSecret = event.getSecret();
        final RaceColumn race = regattaApi.addRaceColumn(ownerCtx, EVENT_NAME, null, 1)[0];

        final Competitor competitorOwner = regattaApi.createAndAddCompetitorWithSecret(ownerCtx, EVENT_NAME, BOAT_CLASS,
                /* email */ null, "Competitor Owner", "USA", registrationLinkSecret, deviceUuidCompetitor1);
        assertNotNull("Competitor for Owner should not be null!", competitorOwner);
        final Competitor competitorSailor = regattaApi.createAndAddCompetitorWithSecret(sailorCtx, EVENT_NAME,
                BOAT_CLASS, /* email */ null, "Competitor Sailor", "USA", registrationLinkSecret,
                deviceUuidCompetitor2);
        assertNotNull("Competitor for Sailor should not be null!", competitorSailor);

        assertEquals("Regatta should contain 2 competitors (seen by regatta owner)", 2,
                regattaApi.getCompetitors(ownerCtx, EVENT_NAME).length);
        assertEquals("Regatta should contain 1 competitor (seen by anonymous)", 1,
                regattaApi.getCompetitors(sailorCtx, EVENT_NAME).length);
        assertEquals("Regatta should contain 2 competitors (seen by admin)", 2,
                regattaApi.getCompetitors(adminCtx, EVENT_NAME).length);

        leaderboardApi.startRaceLogTracking(ownerCtx, EVENT_NAME, race.getRaceName(), "Default");
        leaderboardApi.setTrackingTimes(ownerCtx, EVENT_NAME, race.getRaceName(), "Default", currentTimeMillis(),
                currentTimeMillis() + 600_000L);

        for (double i = 0.0; i < 100.0; i++) {
            final Double longitude = 9.12 + i / 1000.0, latitude = .599 + i / 1000.0, speed = 10.0, course = 180.0;
            gpsFixApi.postGpsFix(ownerCtx, deviceUuidCompetitor1,
                    createFix(longitude, latitude, currentTimeMillis(), speed, course));
            gpsFixApi.postGpsFix(sailorCtx, deviceUuidCompetitor2,
                    createFix(longitude, latitude, currentTimeMillis(), speed, course));
        }

        final Mark mark1 = markApi.addMarkToRegatta(ownerCtx, EVENT_NAME, "FirstMark");

        final Double longitude = 9.12, latitude = .599;
        markApi.addMarkFix(ownerCtx, EVENT_NAME, race.getRaceName(), "Default", mark1.getMarkId(), null, null,
                longitude, latitude, currentTimeMillis());
    }

}
