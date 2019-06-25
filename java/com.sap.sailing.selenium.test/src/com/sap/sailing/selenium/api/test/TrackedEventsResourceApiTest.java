package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.TrackedEventsApi;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedElement;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedEvent;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedEvents;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;

public class TrackedEventsResourceApiTest extends AbstractSeleniumTest {

    private final TrackedEventsApi trackedEventsApi = new TrackedEventsApi();

    private final EventApi eventApi = new EventApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetTrackedEventsEmpty() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);
        Assert.assertNotNull(trackedEvents);
        Assert.assertTrue(Util.isEmpty(trackedEvents.getEvents()));
    }

    @Test
    public void testCreateMultipleTrackingsForSameEvent() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String eventName = "TestEvent-" + UUID.randomUUID().toString();
        final String competitorId = UUID.randomUUID().toString();
        final String boatId = UUID.randomUUID().toString();
        final String markId = UUID.randomUUID().toString();

        final String eventBaseUrl = "testUrl";
        final String deviceId = UUID.randomUUID().toString();

        final Event evt = eventApi.createEvent(adminCtx, eventName, "75QMNATIONALEKREUZER",
                CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId = evt.getId();
        final String regattaId = evt.getName();

        trackedEventsApi.updateTrackedEvent(adminCtx, eventId, regattaId, eventBaseUrl, deviceId, competitorId, null,
                null);
        trackedEventsApi.updateTrackedEvent(adminCtx, eventId, regattaId, eventBaseUrl, deviceId, null, boatId, null);
        trackedEventsApi.updateTrackedEvent(adminCtx, eventId, regattaId, eventBaseUrl, deviceId, null, null, markId);

        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);

        int cntEvents = 0;
        for (final TrackedEvent event : trackedEvents.getEvents()) {
            cntEvents++;
            Assert.assertEquals(eventBaseUrl, event.getEventBaseUrl());
            Assert.assertEquals(eventId, event.getEventId());
            Assert.assertEquals(regattaId, event.getRegattaId());

            int cntElements = 0;
            for (final TrackedElement elem : event.getTrackedElements()) {
                cntElements++;
                final boolean correctBoatId = boatId.equals(elem.getBoatId());
                final boolean correctCompetitorId = competitorId.equals(elem.getCompetitorId());
                final boolean correctMarkId = markId.equals(elem.getMarkId());

                Assert.assertEquals(deviceId, elem.getDeviceId());
                Assert.assertTrue(correctBoatId ^ correctCompetitorId ^ correctMarkId);
            }
            Assert.assertEquals(3, cntElements);
        }

        Assert.assertEquals(1, cntEvents);
    }

    @Test
    public void testCreateTrackedEventWithCompetitorTracking() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        testCreateTrackedEventWithTracking(UUID.randomUUID().toString(), null, null, adminCtx,
                "TestEvent-" + UUID.randomUUID().toString());
    }

    @Test
    public void testCreateTrackedEventWithBoatTracking() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        testCreateTrackedEventWithTracking(null, UUID.randomUUID().toString(), null, adminCtx,
                "TestEvent-" + UUID.randomUUID().toString());
    }

    @Test
    public void testCreateTrackedEventWithMarkTracking() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        testCreateTrackedEventWithTracking(null, null, UUID.randomUUID().toString(), adminCtx,
                "TestEvent-" + UUID.randomUUID().toString());
    }

    private void testCreateTrackedEventWithTracking(final String competitorId, final String boatId, final String markId,
            final ApiContext adminCtx, final String eventName) {
        final String eventBaseUrl = "testUrl";
        final String deviceId = UUID.randomUUID().toString();

        Event evt = eventApi.createEvent(adminCtx, eventName, "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED,
                "Mannheim");
        final String eventId = evt.getId();
        final String regattaId = evt.getName();

        trackedEventsApi.updateTrackedEvent(adminCtx, eventId, regattaId, eventBaseUrl, deviceId, competitorId, boatId,
                markId);

        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);

        boolean hasEvents = false;
        for (TrackedEvent event : trackedEvents.getEvents()) {
            hasEvents = true;
            Assert.assertEquals(eventBaseUrl, event.getEventBaseUrl());
            Assert.assertEquals(eventId, event.getEventId());
            Assert.assertEquals(regattaId, event.getRegattaId());

            boolean hasElements = false;
            for (TrackedElement elem : event.getTrackedElements()) {
                hasElements = true;
                Assert.assertEquals(boatId, elem.getBoatId());
                Assert.assertEquals(competitorId, elem.getCompetitorId());
                Assert.assertEquals(deviceId, elem.getDeviceId());
                Assert.assertEquals(markId, elem.getMarkId());
            }
            Assert.assertTrue(hasElements);
        }

        Assert.assertTrue(hasEvents);
    }

}
