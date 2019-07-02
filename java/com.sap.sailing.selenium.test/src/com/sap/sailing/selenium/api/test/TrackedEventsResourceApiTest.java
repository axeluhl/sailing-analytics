package com.sap.sailing.selenium.api.test;

import static com.sap.sailing.selenium.api.core.ApiContext.SERVER_CONTEXT;
import static com.sap.sailing.selenium.api.core.ApiContext.createAdminApiContext;
import static com.sap.sailing.selenium.api.core.ApiContext.createApiContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.event.EventApi;
import com.sap.sailing.selenium.api.event.EventApi.Event;
import com.sap.sailing.selenium.api.event.SecurityApi;
import com.sap.sailing.selenium.api.event.TrackedEventsApi;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedElement;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedEvent;
import com.sap.sailing.selenium.api.event.TrackedEventsApi.TrackedEvents;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;

public class TrackedEventsResourceApiTest extends AbstractSeleniumTest {

    private final TrackedEventsApi trackedEventsApi = new TrackedEventsApi();

    private final EventApi eventApi = new EventApi();
    private final SecurityApi securityApi = new SecurityApi();

    @Before
    public void setUp() {
        clearState(getContextRoot());
    }

    @Test
    public void testGetTrackedEventsEmpty() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);
        Assert.assertNotNull("Tracked Events element should not be null", trackedEvents);
        Assert.assertTrue("Expected empty list of tracked events", Util.isEmpty(trackedEvents.getEvents()));
    }

    @Test
    public void testCreateMultipleTrackingEventsAndEditOne() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String competitorId = UUID.randomUUID().toString();
        final String boatId = UUID.randomUUID().toString();
        final String markId = UUID.randomUUID().toString();

        final String eventBaseUrl = "testUrl";
        final String deviceId = UUID.randomUUID().toString();

        final Event evt = eventApi.createEvent(adminCtx, "TestEvent-" + UUID.randomUUID().toString(),
                "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId = evt.getId();
        final String leaderboardName = evt.getName();
        final String regattaSecret = evt.getSecret();

        final Event evt2 = eventApi.createEvent(adminCtx, "TestEvent-" + UUID.randomUUID().toString(),
                "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId2 = evt2.getId();
        final String leaderboardName2 = evt2.getName();
        final String regattaSecret2 = evt.getSecret();

        final Set<Triple<String, String, String>> trackedIds = new HashSet<>();
        trackedIds.add(new Triple<>(competitorId, null, null));
        trackedIds.add(new Triple<>(null, boatId, null));
        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId, leaderboardName, eventBaseUrl, deviceId,
                trackedIds, regattaSecret);
        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId2, leaderboardName2, eventBaseUrl, deviceId,
                new HashSet<>(Arrays.asList(new Triple<>(null, null, markId))), regattaSecret2);

        trackedEventsApi.setArchived(adminCtx, evt.getId(), leaderboardName, true);
        trackedEventsApi.setArchived(adminCtx, evt2.getId(), leaderboardName2, false);
        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, false);

        int cntEvents = 0;
        for (final TrackedEvent event : trackedEvents.getEvents()) {
            cntEvents++;
            Assert.assertEquals("Unexpected event base url", eventBaseUrl, event.getEventBaseUrl());
            if (eventId.equals(event.getEventId())) {
                Assert.assertEquals("Unexpected event ID", eventId, event.getEventId());
                Assert.assertEquals("Unexpected leaderboard name", leaderboardName, event.getLeaderboardName());
                Assert.assertEquals("Unexpected regatta secret", regattaSecret, event.getRegattaSecret());
                Assert.fail("Expected event 1 to not be shown anymore.");
            } else if (eventId2.equals(event.getEventId())) {
                Assert.assertEquals("Unexpected event ID", eventId2, event.getEventId());
                Assert.assertEquals("Unexpected leaderboard name", leaderboardName2, event.getLeaderboardName());
                Assert.assertEquals("Unexpected regatta secret", regattaSecret2, event.getRegattaSecret());
            } else {
                Assert.fail("Invalid event id.");
            }

            int cntElements = 0;
            for (final TrackedElement elem : event.getTrackedElements()) {
                cntElements++;
                final boolean correctBoatId = boatId.equals(elem.getBoatId());
                final boolean correctCompetitorId = competitorId.equals(elem.getCompetitorId());
                final boolean correctMarkId = markId.equals(elem.getMarkId());

                Assert.assertEquals("Unexpected device ID", deviceId, elem.getDeviceId());
                Assert.assertTrue("More than one or zero items tracked in this element",
                        correctBoatId ^ correctCompetitorId ^ correctMarkId);
            }
            Assert.assertEquals("Invalid numer of elements in this event", 1, cntElements);
        }

        Assert.assertEquals("Expected 1 events", 1, cntEvents);
    }

    @Test
    public void testCreateMultipleTrackingEvents() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String competitorId = UUID.randomUUID().toString();
        final String boatId = UUID.randomUUID().toString();
        final String markId = UUID.randomUUID().toString();

        final String eventBaseUrl = "testUrl";
        final String deviceId = UUID.randomUUID().toString();

        final Event evt = eventApi.createEvent(adminCtx, "TestEvent-" + UUID.randomUUID().toString(),
                "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId = evt.getId();
        final String leaderboardName = evt.getName();
        final String regattaSecret = evt.getSecret();

        final Event evt2 = eventApi.createEvent(adminCtx, "TestEvent-" + UUID.randomUUID().toString(),
                "75QMNATIONALEKREUZER", CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId2 = evt2.getId();
        final String leaderboardName2 = evt2.getName();
        final String regattaSecret2 = evt2.getSecret();

        final Set<Triple<String, String, String>> trackedIds = new HashSet<>();
        trackedIds.add(new Triple<>(competitorId, null, null));
        trackedIds.add(new Triple<>(null, boatId, null));
        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId, leaderboardName, eventBaseUrl, deviceId,
                trackedIds, regattaSecret);
        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId2, leaderboardName2, eventBaseUrl, deviceId,
                new HashSet<>(Arrays.asList(new Triple<>(null, null, markId))), regattaSecret2);

        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);

        int cntEvents = 0;
        for (final TrackedEvent event : trackedEvents.getEvents()) {
            cntEvents++;
            Assert.assertEquals("Unexpected event base url", eventBaseUrl, event.getEventBaseUrl());
            if (eventId.equals(event.getEventId())) {
                Assert.assertEquals("Unexpected event ID", eventId, event.getEventId());
                Assert.assertEquals("Unexpected leaderboard name", leaderboardName, event.getLeaderboardName());
                Assert.assertEquals("Unexpected regatta secret", regattaSecret, event.getRegattaSecret());
            } else if (eventId2.equals(event.getEventId())) {
                Assert.assertEquals("Unexpected event ID", eventId2, event.getEventId());
                Assert.assertEquals("Unexpected leaderboard name", leaderboardName2, event.getLeaderboardName());
                Assert.assertEquals("Unexpected regatta secret", regattaSecret2, event.getRegattaSecret());
            } else {
                Assert.fail("Invalid event id.");
            }

            int cntElements = 0;
            for (final TrackedElement elem : event.getTrackedElements()) {
                cntElements++;
                final boolean correctBoatId = boatId.equals(elem.getBoatId());
                final boolean correctCompetitorId = competitorId.equals(elem.getCompetitorId());
                final boolean correctMarkId = markId.equals(elem.getMarkId());

                Assert.assertEquals("Unexpected device ID", deviceId, elem.getDeviceId());
                Assert.assertTrue("More than one or zero items tracked in this element",
                        correctBoatId ^ correctCompetitorId ^ correctMarkId);
            }
            Assert.assertEquals("Invalid numer of elements in this event", eventId.equals(event.getEventId()) ? 2 : 1,
                    cntElements);
        }

        Assert.assertEquals("Expected 2 events", 2, cntEvents);
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
        final String leaderboardName = evt.getName();
        final String regattaSecret = evt.getSecret();

        final Set<Triple<String, String, String>> trackedIds = new HashSet<>();
        trackedIds.add(new Triple<>(competitorId, null, null));
        trackedIds.add(new Triple<>(null, boatId, null));
        trackedIds.add(new Triple<>(null, null, markId));
        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId, leaderboardName, eventBaseUrl, deviceId,
                trackedIds, regattaSecret);

        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);

        int cntEvents = 0;
        for (final TrackedEvent event : trackedEvents.getEvents()) {
            cntEvents++;
            Assert.assertEquals("Unexpected event base url", eventBaseUrl, event.getEventBaseUrl());
            Assert.assertEquals("Unexpected event ID", eventId, event.getEventId());
            Assert.assertEquals("Unexpected leaderboard name", leaderboardName, event.getLeaderboardName());
            Assert.assertEquals("Unexpected regatta secret", regattaSecret, event.getRegattaSecret());

            int cntElements = 0;
            for (final TrackedElement elem : event.getTrackedElements()) {
                cntElements++;
                final boolean correctBoatId = boatId.equals(elem.getBoatId());
                final boolean correctCompetitorId = competitorId.equals(elem.getCompetitorId());
                final boolean correctMarkId = markId.equals(elem.getMarkId());

                Assert.assertEquals("Unexpected device ID", deviceId, elem.getDeviceId());
                Assert.assertTrue("More than one or zero items tracked in this element",
                        correctBoatId ^ correctCompetitorId ^ correctMarkId);
            }
            Assert.assertEquals("Expected 3 event elements", 3, cntElements);
        }

        Assert.assertEquals("Expected exactly 1 event", 1, cntEvents);
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

    private String testCreateTrackedEventWithTracking(final String competitorId, final String boatId,
            final String markId, final ApiContext adminCtx, final String eventName) {
        final String eventBaseUrl = "testUrl";
        final String deviceId = UUID.randomUUID().toString();

        final Event evt = eventApi.createEvent(adminCtx, eventName, "75QMNATIONALEKREUZER",
                CompetitorRegistrationType.CLOSED, "Mannheim");
        final String eventId = evt.getId();
        final String leaderboardName = evt.getName();
        final String regattaSecret = evt.getSecret();

        trackedEventsApi.updateOrCreateTrackedEvent(adminCtx, eventId, leaderboardName, eventBaseUrl, deviceId,
                new HashSet<>(Arrays.asList(new Triple<>(competitorId, boatId, markId))), regattaSecret);

        // check if created event is still there
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);

        boolean hasEvents = false;
        for (final TrackedEvent event : trackedEvents.getEvents()) {
            hasEvents = true;
            Assert.assertEquals("Unexpected event base url", eventBaseUrl, event.getEventBaseUrl());
            Assert.assertEquals("Unexpected event ID", eventId, event.getEventId());
            Assert.assertEquals("Unexpected leaderboard name", leaderboardName, event.getLeaderboardName());
            Assert.assertEquals("Unexpected regatta secret", regattaSecret, event.getRegattaSecret());

            boolean hasElements = false;
            for (final TrackedElement elem : event.getTrackedElements()) {
                hasElements = true;
                Assert.assertEquals("Unexpected boat ID", boatId, elem.getBoatId());
                Assert.assertEquals("Unexpected competitor ID", competitorId, elem.getCompetitorId());
                Assert.assertEquals("Unexpected device ID", deviceId, elem.getDeviceId());
                Assert.assertEquals("Unexpected mark ID", markId, elem.getMarkId());
            }
            Assert.assertTrue("Expected at least one element", hasElements);
        }

        Assert.assertTrue("Expected at least one event", hasEvents);
        return eventId;
    }

    @Test
    public void testDeleteEvent() {
        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);
        final String leaderboardName = "TestEvent-" + UUID.randomUUID().toString();
        final String eventId = testCreateTrackedEventWithTracking(UUID.randomUUID().toString(), null, null, adminCtx,
                leaderboardName);

        trackedEventsApi.deleteEventTrackings(adminCtx, eventId, leaderboardName);

        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(adminCtx, true);
        Assert.assertNotNull("Tracked Events element should not be null", trackedEvents);
        Assert.assertTrue("Expected empty list of tracked events", Util.isEmpty(trackedEvents.getEvents()));
    }

    @Test
    public void testMultipleUsers() {

        // create test user
        final ApiContext adminSecurityCtx = createAdminApiContext(getContextRoot(), ApiContext.SECURITY_CONTEXT);
        securityApi.createUser(adminSecurityCtx, "tuser", "Test User", null, "testuser");
        final ApiContext userCtx = createApiContext(getContextRoot(), SERVER_CONTEXT, "tuser", "testuser");

        final ApiContext adminCtx = createAdminApiContext(getContextRoot(), SERVER_CONTEXT);

        // add an event in the user context of admin user
        testCreateTrackedEventWithTracking(null, UUID.randomUUID().toString(), null, adminCtx,
                "TestEvent-" + UUID.randomUUID().toString());

        // check if event is invisible to the other user
        final TrackedEvents trackedEvents = trackedEventsApi.getTrackedEvents(userCtx, true);
        Assert.assertNotNull("Tracked Events element should not be null", trackedEvents);
        Assert.assertTrue("Expected empty list of tracked events", Util.isEmpty(trackedEvents.getEvents()));
    }

}
