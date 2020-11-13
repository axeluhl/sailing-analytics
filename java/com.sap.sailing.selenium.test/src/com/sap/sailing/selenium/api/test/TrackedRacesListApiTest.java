package com.sap.sailing.selenium.api.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.TrackedEventsApi;
import com.sap.sailing.selenium.api.event.TrackedRacesListApi;
import com.sap.sailing.selenium.api.regatta.RaceColumn;
import com.sap.sailing.selenium.api.regatta.RegattaApi;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util.Triple;

public class TrackedRacesListApiTest extends AbstractSeleniumTest {
    private final EventApi eventApi = new EventApi();
    private final TrackedEventsApi trackedEventsApi = new TrackedEventsApi();
    private final TrackedRacesListApi trackedRacesListApi = new TrackedRacesListApi();
    private final RegattaApi regattaApi = new RegattaApi();
    
    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetRaces() {
        final String deviceId = UUID.randomUUID().toString();
        final Set<Triple<String, String, String>> trackedIds = new HashSet<>();
        final String competitorId = UUID.randomUUID().toString();
        final String boatId = UUID.randomUUID().toString();
        trackedIds.add(new Triple<>(competitorId, null, null));
        trackedIds.add(new Triple<>(null, boatId, null));
        
        ApiContext ctx = ApiContext.createAdminApiContext(getContextRoot(),
                ApiContext.SERVER_CONTEXT);
        Event event = eventApi.createEvent(ctx, "Test Event GH", "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED, "Karlsruhe");
        
        trackedEventsApi.updateOrCreateTrackedEvent(ctx, event.getId(), event.getName(), "/", deviceId, trackedIds, event.getSecret());
        RaceColumn[] result = regattaApi.addRaceColumn(ctx, event.getName(), "T", 5);
        
        trackedRacesListApi.getRaces(ctx, false, new ArrayList<String>(), "excl");
        System.out.println("done");
    }
}
