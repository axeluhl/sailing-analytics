package com.sap.sailing.server.gateway.jaxrs.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;
import com.sap.sailing.server.gateway.jaxrs.AbstractSailingServerResource;
import com.sap.sailing.server.impl.preferences.model.SailingPreferences;
import com.sap.sailing.server.impl.preferences.model.TrackedElementWithDeviceId;
import com.sap.sailing.server.impl.preferences.model.TrackedEventPreference;
import com.sap.sailing.server.impl.preferences.model.TrackedEventPreferences;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.security.shared.OwnershipAnnotation;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.shared.media.ImageDescriptor;

@Path("/v1/trackedevents/")
public class TrackedEventsResource extends AbstractSailingServerResource {

    private static final String KEY_QUERY_INCLUDE_ARCHIVED = "includeArchived";

    private static final String KEY_LEADERBOARD_NAME = "leaderboardName";
    private static final String KEY_EVENT_ID = "eventId";
    private static final String KEY_EVENT_IS_ARCHIVED = "isArchived";
    private static final String KEY_EVENT_NAME = "name";
    private static final String KEY_EVENT_START = "start";
    private static final String KEY_EVENT_END = "end";
    private static final String KEY_EVENT_TRACKED_ELEMENTS = "trackedElements";
    private static final String KEY_EVENT_BASE_URL = "url";
    private static final String KEY_EVENT_IS_OWNER = "isOwner";
    private static final String KEY_EVENT_IMAGE_URL = "imageUrl";
    private static final String KEY_EVENT_REGATTA_SECRET = "regattaSecret";
    private static final String KEY_TRACKED_ELEMENT_DEVICE_ID = "deviceId";
    private static final String KEY_TRACKED_ELEMENT_COMPETITOR_ID = "competitorId";
    private static final String KEY_TRACKED_ELEMENT_BOAT_ID = "boatId";
    private static final String KEY_TRACKED_ELEMENT_MARK_ID = "markId";
    private static final String KEY_TRACKED_EVENTS = "trackedEvents";

    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getTrackedEvents(@QueryParam(KEY_QUERY_INCLUDE_ARCHIVED) String includeArchivedStr) {

        final User currentUser = getSecurityService().getCurrentUser();
        final ResponseBuilder builder;

        // check if user logged in
        if (currentUser != null) {

            // load tracked events from storage
            final TrackedEventPreferences prefs = getSecurityService().getPreferenceObject(currentUser.getName(),
                    SailingPreferences.TRACKED_EVENTS_PREFERENCES);

            final JSONArray result = new JSONArray();
            final boolean includeArchived = Boolean.parseBoolean(includeArchivedStr);

            if (prefs != null) {
                // iterate all stored tracked events
                for (final TrackedEventPreference pref : prefs.getTrackedEvents()) {

                    if (!includeArchived && pref.getIsArchived()) {
                        // skip, if event is archived and should be filtered out
                        continue;
                    }

                    final UUID eventId = pref.getEventId();
                    final Event event = getService().getEvent(eventId);
                    final JSONObject jsonEvent = new JSONObject();
                    if (event != null) {
                        // event actually exists on this server -> parse relevant data
                        if (getSecurityService().hasCurrentUserReadPermission(event)) {
                            // user has read permission on event -> add additional event-specific data
                            final OwnershipAnnotation ownership = getSecurityService()
                                    .getOwnership(event.getIdentifier());
                            final boolean isOwner = currentUser == null ? false
                                    : currentUser.equals(ownership.getAnnotation().getUserOwner());
                            jsonEvent.put(KEY_EVENT_IS_OWNER, isOwner);

                            jsonEvent.put(KEY_EVENT_NAME, event.getName());
                            jsonEvent.put(KEY_EVENT_START, event.getStartDate().toString());
                            jsonEvent.put(KEY_EVENT_END, event.getEndDate().toString());

                            final List<ImageDescriptor> imageWithLogoTag = event
                                    .findImagesWithTag(MediaTagConstants.LOGO.getName());
                            if (!imageWithLogoTag.isEmpty()) {
                                jsonEvent.put(KEY_EVENT_IMAGE_URL, imageWithLogoTag.get(0).getURL().toExternalForm());
                            } else {
                                // user does not have read permissions for this event on this server -> only send back
                                // basic data
                            }
                        }
                    } else {
                        // event does not exist on this server --> only send back basic data
                    }
                    jsonEvent.put(KEY_EVENT_ID, pref.getEventId().toString());
                    jsonEvent.put(KEY_EVENT_REGATTA_SECRET, pref.getRegattaSecret());
                    jsonEvent.put(KEY_EVENT_BASE_URL, pref.getBaseUrl());
                    jsonEvent.put(KEY_EVENT_IS_ARCHIVED, pref.getIsArchived());
                    jsonEvent.put(KEY_LEADERBOARD_NAME, pref.getLeaderboardName());
                    jsonEvent.put(KEY_EVENT_TRACKED_ELEMENTS, trackedElementsToJson(pref));
                    result.add(jsonEvent);
                }
            }

            // storage was empty -> respond with empty list
            final JSONObject resultEvents = new JSONObject();
            resultEvents.put(KEY_TRACKED_EVENTS, result);
            final String jsonString = resultEvents.toJSONString();
            builder = Response.ok(jsonString).header("Content-Type", MediaType.APPLICATION_JSON + ";charset=UTF-8");
        } else {
            builder = Response.status(Status.UNAUTHORIZED);
        }
        return builder.build();
    }

    private JSONArray trackedElementsToJson(final TrackedEventPreference pref) {
        final JSONArray deviceIdsWithTrackedElementJson = new JSONArray();
        for (final TrackedElementWithDeviceId trackedElement : pref.getTrackedElements()) {
            final JSONObject trackedElementJson = new JSONObject();
            trackedElementJson.put(KEY_TRACKED_ELEMENT_DEVICE_ID, trackedElement.getDeviceId());
            if (trackedElement.getTrackedCompetitorId() != null) {
                trackedElementJson.put(KEY_TRACKED_ELEMENT_COMPETITOR_ID,
                        trackedElement.getTrackedCompetitorId().toString());
            } else if (trackedElement.getTrackedBoatId() != null) {
                trackedElementJson.put(KEY_TRACKED_ELEMENT_BOAT_ID, trackedElement.getTrackedBoatId().toString());
            } else if (trackedElement.getTrackedMarkId() != null) {
                trackedElementJson.put(KEY_TRACKED_ELEMENT_MARK_ID, trackedElement.getTrackedMarkId().toString());
            }
            deviceIdsWithTrackedElementJson.add(trackedElementJson);
        }
        return deviceIdsWithTrackedElementJson;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{eventId}/{leaderboardName}")
    public Response updateOrCreateTrackedEvent(@PathParam(KEY_EVENT_ID) String eventId,
            @PathParam(KEY_LEADERBOARD_NAME) String leaderboardName, String jsonBody) {
        ResponseBuilder responseBuilder = null;
        final User currentUser = getSecurityService().getCurrentUser();

        if (currentUser != null) {

            try {
                final Object requestBody = JSONValue.parseWithException(jsonBody);
                final JSONObject requestObject = Helpers.toJSONObjectSafe(requestBody);
                final Boolean archived = (Boolean) requestObject.get(KEY_EVENT_IS_ARCHIVED);
                final String baseUrl = (String) requestObject.get(KEY_EVENT_BASE_URL);
                final String regattaSecret = (String) requestObject.get(KEY_EVENT_REGATTA_SECRET);
                try {
                    final UUID uuidEvent = UUID.fromString(eventId);
                    final boolean isArchived = archived;

                    TrackedEventPreferences prefs = getSecurityService().getPreferenceObject(currentUser.getName(),
                            SailingPreferences.TRACKED_EVENTS_PREFERENCES);

                    if (prefs == null) {
                        prefs = new TrackedEventPreferences();
                    }

                    final JSONArray trackedElementsJson = (JSONArray) requestObject.get(KEY_EVENT_TRACKED_ELEMENTS);

                    // check that exactly one trackedElement is in JSON
                    if (trackedElementsJson == null || trackedElementsJson.size() == 0) {
                        // too few children
                        responseBuilder = Response.status(Status.BAD_REQUEST)
                                .entity("Invalid JSON body in request: Tracked element is missing.");
                    } else {

                        // Copy tracked event preference objects to new list while removing the changed
                        // event/leaderboard node
                        final Collection<TrackedEventPreference> prefsNew = //
                                StreamSupport.stream(prefs.getTrackedEvents().spliterator(), false)
                                        .filter(pref -> !(pref.getEventId().equals(uuidEvent)
                                                && pref.getLeaderboardName().equals(leaderboardName)))
                                        .collect(Collectors.toList());

                        // Create new tracked elements for this event/leaderboard from JSON body
                        final Collection<TrackedElementWithDeviceId> trackedElements = new ArrayList<>();

                        for (int i = 0; i < trackedElementsJson.size(); i++) {
                            final JSONObject jsonTrackedElement = (JSONObject) trackedElementsJson.get(i);

                            // parse IDs
                            final String deviceId = (String) jsonTrackedElement.get(KEY_TRACKED_ELEMENT_DEVICE_ID);
                            final String competitorIdStr = (String) jsonTrackedElement
                                    .get(KEY_TRACKED_ELEMENT_COMPETITOR_ID);
                            final String boatIdStr = (String) jsonTrackedElement.get(KEY_TRACKED_ELEMENT_BOAT_ID);
                            final String markIdStr = (String) jsonTrackedElement.get(KEY_TRACKED_ELEMENT_MARK_ID);

                            // parse UUIDs of tracked element
                            final UUID competitorId = parseUUID(competitorIdStr);
                            final UUID boatId = parseUUID(boatIdStr);
                            final UUID markId = parseUUID(markIdStr);

                            if (boatId != null ^ markId != null ^ competitorId != null) {

                                // create TrackedElementWithID holder
                                final TrackedElementWithDeviceId newPrefElem = new TrackedElementWithDeviceId(deviceId,
                                        boatId, competitorId, markId);
                                trackedElements.add(newPrefElem);

                            } else {
                                // no boatId, competitorId or markId were specified
                                responseBuilder = Response.status(Status.BAD_REQUEST)
                                        .entity("Invalid JSON body in request.");
                            }

                        }

                        final TrackedEventPreference newPreference = new TrackedEventPreference(uuidEvent,
                                leaderboardName, trackedElements, baseUrl, isArchived, regattaSecret);
                        prefsNew.add(newPreference);

                        prefs.setTrackedEvents(prefsNew);

                        getSecurityService().setPreferenceObject(currentUser.getName(),
                                SailingPreferences.TRACKED_EVENTS_PREFERENCES, prefs);
                        responseBuilder = Response.status(Status.ACCEPTED);
                    }

                } catch (IllegalArgumentException | ClassCastException e) {
                    responseBuilder = Response.status(Status.BAD_REQUEST)
                            .entity("Invalid or missing attributes in JSON body.");
                }
            } catch (ParseException | JsonDeserializationException e) {
                responseBuilder = Response.status(Status.BAD_REQUEST).entity("Invalid JSON body in request.");
            }
        } else {
            responseBuilder = Response.status(Status.UNAUTHORIZED);
        }
        return responseBuilder.build();
    }

    private UUID parseUUID(String potentialUUID) {
        UUID uuid;
        if (potentialUUID != null && !potentialUUID.isEmpty()) {

            try {
                uuid = UUID.fromString(potentialUUID);
            } catch (IllegalArgumentException e) {
                uuid = null;
            }
        } else {
            uuid = null;
        }
        return uuid;
    }

    @POST
    @Path("{eventId}/{leaderboardName}")
    public Response updateTrackedEventsArchivedStatus(@PathParam(KEY_EVENT_ID) String eventId,
            @PathParam(KEY_LEADERBOARD_NAME) String leaderboardName,
            @FormParam(KEY_QUERY_INCLUDE_ARCHIVED) String archived) {
        final boolean isArchived = Boolean.parseBoolean(archived);
        return applyOnEventByIdAndLeadboardName(eventId, leaderboardName,
                pref -> new TrackedEventPreference(pref, isArchived));
    }

    @DELETE
    @Path("{eventId}/{leaderboardName}")
    public Response deleteTrackedEvents(@PathParam(KEY_EVENT_ID) String eventId,
            @PathParam(KEY_LEADERBOARD_NAME) String leaderboardName) {
        return applyOnEventByIdAndLeadboardName(eventId, leaderboardName, pref -> null);
    }

    /** Finds the tracked event with the corresponding eventId and applies the function to it. */
    private Response applyOnEventByIdAndLeadboardName(String eventId, String leaderboardName,
            Function<TrackedEventPreference, TrackedEventPreference> function) {
        ResponseBuilder responseBuilder;
        final User currentUser = getSecurityService().getCurrentUser();

        if (currentUser != null) {

            final TrackedEventPreferences prefs = getSecurityService().getPreferenceObject(currentUser.getName(),
                    SailingPreferences.TRACKED_EVENTS_PREFERENCES);
            try {
                // parse tracked event UUID
                final UUID eventUuid = UUID.fromString(eventId);

                if (prefs == null) {
                    // storage is empty
                    responseBuilder = Response.status(Status.NOT_FOUND)
                            .entity("No tracked events with this eventId found.");
                } else {

                    // iterate stored preferences until tracked event with corresponding eventId is found
                    boolean found = false;
                    final Collection<TrackedEventPreference> newPrefs = new HashSet<>();
                    for (final TrackedEventPreference pref : prefs.getTrackedEvents()) {
                        if (pref.getEventId().equals(eventUuid) && pref.getLeaderboardName().equals(leaderboardName)) {
                            // tracked event found, apply function on it and remove it if result of function execution
                            // is null
                            found = true;
                            final TrackedEventPreference updatedPreference = function.apply(pref);
                            if (updatedPreference != null) {
                                newPrefs.add(updatedPreference);
                            }
                        } else {
                            newPrefs.add(pref);
                        }
                    }
                    if (!found) {
                        // tracked event was not found -> respond with error
                        responseBuilder = Response.status(Status.NOT_FOUND)
                                .entity("No tracked events with this eventId found.");
                    } else {
                        // update and store preferences
                        prefs.setTrackedEvents(newPrefs);
                        getSecurityService().setPreferenceObject(currentUser.getName(),
                                SailingPreferences.TRACKED_EVENTS_PREFERENCES, prefs);
                        responseBuilder = Response.status(Status.ACCEPTED);
                    }

                }
            } catch (IllegalArgumentException e) {
                // eventId parameter could not be parsed
                responseBuilder = Response.status(Status.BAD_REQUEST).entity("Invalid eventId.");
            }
        } else {
            responseBuilder = Response.status(Status.UNAUTHORIZED);
        }
        return responseBuilder.build();
    }
}
