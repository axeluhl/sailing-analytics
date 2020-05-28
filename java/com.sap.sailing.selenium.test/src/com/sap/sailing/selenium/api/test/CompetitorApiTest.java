package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.regatta.Competitor;
import com.sap.sailing.selenium.api.regatta.CompetitorsApi;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class CompetitorApiTest extends AbstractSeleniumTest {

    private static final String EVENT_NAME = "Competitor Test Regatta";
    private static final String BOAT_CLASS = "GC 32";
    private static final String COMPETITOR_NAME = "Donald Duck";

    private final EventApi eventApi = new EventApi();
    private final RegattaApi regattaApi = new RegattaApi();
    private final CompetitorsApi competitorApi = new CompetitorsApi();

    private ApiContext adminCtx;

    @Before
    public void setUp() {
        clearState(getContextRoot());
        super.setUp();
        adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        eventApi.createEvent(adminCtx, EVENT_NAME, BOAT_CLASS, CompetitorRegistrationType.CLOSED, "Some special place");
    }

    @Test
    public void testGetCompetitorWithOldSerializer() {
        final Competitor competitor = regattaApi.createAndAddCompetitor(adminCtx, EVENT_NAME, BOAT_CLASS, "test@test",
                COMPETITOR_NAME, "USA");
        final Competitor competitorReloaded = competitorApi.getCompetitor(adminCtx, competitor.getId());
        assertEquals(competitor.getId(), competitorReloaded.getId());
        assertEquals(competitor.getName(), competitorReloaded.getName());
        assertEquals(competitor.getNationality(), competitorReloaded.getNationality());
        // assertEquals(competitor.getNationalityISO2(), competitorReloaded.getNationalityISO2());
        // assertEquals(competitor.getNationalityISO3(), competitorReloaded.getNationalityISO3());
        assertEquals(competitor.getColor(), competitorReloaded.getColor());
        assertEquals(competitor.getFlagImageUri(), competitorReloaded.getFlagImageUri());
        assertEquals(competitor.getTeam().getTeamImageUri(), competitorReloaded.getTeam().getTeamImageUri());
        assertEquals(competitor.getShortName(), competitorReloaded.getShortName());
        assertEquals(competitor.getTimeOnTimeFactor(), competitorReloaded.getTimeOnTimeFactor());
        assertEquals(competitor.getTimeOnDistanceAllowanceInSecondsPerNauticalMile(),
                competitorReloaded.getTimeOnDistanceAllowanceInSecondsPerNauticalMile());
        assertEquals(competitor.getNationalityISO2(), competitorReloaded.get("countryCode"));
        assertEquals(competitor.getColor(), competitorReloaded.get("color"));
        assertEquals(competitor.getFlagImageUri(), competitorReloaded.get("flagImage"));
        assertEquals(competitor.getTeam().getTeamImageUri(), competitorReloaded.get("teamImageUri"));
    }

    @Test
    public void testUpdateCompetitor() {
        final Competitor competitor = regattaApi.createAndAddCompetitor(adminCtx, EVENT_NAME, BOAT_CLASS, "test@test",
                COMPETITOR_NAME, "USA");

        Competitor competitorU1 = competitorApi.updateCompetitor(adminCtx, competitor.getId(),
                mapOf("displayColor", "#ABCDEF"));
        assertEquals(competitor.getId(), competitorU1.getId());
        assertEquals("#ABCDEF", competitorU1.getColor());
        assertEquals(competitor.getName(), competitorU1.getName());

        Competitor competitorU2 = competitorApi.updateCompetitor(adminCtx, competitor.getId(),
                mapOf("name", "Dagobert"));
        assertEquals("#ABCDEF", competitorU2.getColor());
        assertEquals("Dagobert", competitorU2.getName());
        
        Competitor competitorU3 = competitorApi.updateCompetitor(adminCtx, competitor.getId(),
                mapOf("nationality", "GER", "flagImageUri", "http://flagimage.url", "teamImageUri", "http://teamimage.url"));
        assertEquals("#ABCDEF", competitorU3.getColor());
        assertEquals("Dagobert", competitorU3.getName());
        assertEquals("GER", competitorU3.getNationality());
        assertEquals("http://flagimage.url", competitorU3.getFlagImageUri());
        assertEquals("http://teamimage.url", competitorU3.getTeam().getTeamImageUri());
        
        Competitor competitorU4 = competitorApi.updateCompetitor(adminCtx, competitor.getId(),
                mapOf("timeOnTimeFactor", 2.4, "timeOnDistanceAllowanceInSecondsPerNauticalMile", 10000));
        assertEquals("#ABCDEF", competitorU4.getColor());
        assertEquals("Dagobert", competitorU4.getName());
        assertEquals("GER", competitorU4.getNationality());
        assertEquals("http://flagimage.url", competitorU4.getFlagImageUri());
        assertEquals("http://teamimage.url", competitorU4.getTeam().getTeamImageUri());
        assertEquals(2.4, competitorU4.getTimeOnTimeFactor());
        assertEquals(10000.0, competitorU4.getTimeOnDistanceAllowanceInSecondsPerNauticalMile());
    }

    private Map<String, Object> mapOf(Object... args) {
        assertTrue(args.length % 2 == 0);
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put((String) args[i], args[i + 1]);
        }
        return map;
    }
}
