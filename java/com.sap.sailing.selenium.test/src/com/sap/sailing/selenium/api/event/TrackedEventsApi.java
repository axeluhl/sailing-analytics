package com.sap.sailing.selenium.api.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class TrackedEventsApi {

    private static final String TRACKED_EVENTS_URL = "/api/v1/events/trackedevents/";
    private static final String KEY_TRACKED_EVENTS = "trackedEvents";

    private static final String KEY_REGATTA_ID = "regattaId";
    private static final String KEY_EVENT_ID = "eventId";
    private static final String KEY_EVENT_IS_ARCHIVED = "isArchived";
    private static final String KEY_EVENT_NAME = "name";
    private static final String KEY_EVENT_START = "start";
    private static final String KEY_EVENT_END = "end";
    private static final String KEY_EVENT_BASE_URL = "url";
    private static final String KEY_EVENT_IS_OWNER = "isOwner";

    private static final String KEY_EVENT_TRACKED_ELEMENTS = "trackedElements";

    private static final String KEY_TRACKED_ELEMENT_DEVICE_ID = "deviceId";
    private static final String KEY_TRACKED_ELEMENT_COMPETITOR_ID = "competitorId";
    private static final String KEY_TRACKED_ELEMENT_BOAT_ID = "boatId";
    private static final String KEY_TRACKED_ELEMENT_MARK_ID = "markId";

    public TrackedEvents getTrackedEvents(ApiContext ctx, boolean isArchived) {
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put(KEY_EVENT_IS_ARCHIVED, Boolean.toString(isArchived));
        return new TrackedEvents(ctx.get(TRACKED_EVENTS_URL, queryParams));
    }

    public void updateTrackedEvent(ApiContext ctx, String eventId, String regattaId, String eventBaseUrl,
            String deviceId, String competitorId, String boatId, String markId) {
        final JSONObject json = new JSONObject();
        json.put(KEY_EVENT_ID, eventId);
        json.put(KEY_REGATTA_ID, regattaId);
        json.put(KEY_EVENT_BASE_URL, eventBaseUrl);

        final JSONArray elements = new JSONArray();
        final JSONObject elem = new JSONObject();
        elem.put(KEY_TRACKED_ELEMENT_DEVICE_ID, deviceId);
        if (competitorId != null) {
            elem.put(KEY_TRACKED_ELEMENT_COMPETITOR_ID, competitorId);
        } else if (boatId != null) {
            elem.put(KEY_TRACKED_ELEMENT_BOAT_ID, boatId);
        } else if (markId != null) {
            elem.put(KEY_TRACKED_ELEMENT_MARK_ID, markId);
        }
        elements.add(elem);

        json.put(KEY_EVENT_TRACKED_ELEMENTS, elements);
        json.put(KEY_EVENT_IS_ARCHIVED, false);
        ctx.put(TRACKED_EVENTS_URL, new HashMap<>(), json);
    }

    public void setArchived(ApiContext ctx, String eventId, boolean archived) {
        final HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put(KEY_EVENT_ID, eventId);
        queryParams.put(KEY_EVENT_IS_ARCHIVED, Boolean.toString(archived));
        ctx.post(TRACKED_EVENTS_URL, queryParams);
    }

    public class TrackedElement extends JsonWrapper {
        public TrackedElement(JSONObject json) {
            super(json);
        }

        public String getCompetitorId() {
            return get(KEY_TRACKED_ELEMENT_COMPETITOR_ID);
        }

        public String getBoatId() {
            return get(KEY_TRACKED_ELEMENT_BOAT_ID);
        }

        public String getMarkId() {
            return get(KEY_TRACKED_ELEMENT_MARK_ID);
        }

        public String getDeviceId() {
            return get(KEY_TRACKED_ELEMENT_DEVICE_ID);
        }
    }

    public class TrackedEvents extends JsonWrapper {
        public TrackedEvents(JSONObject json) {
            super(json);
        }

        public Iterable<TrackedEvent> getEvents() {
            final Collection<TrackedEvent> events = new ArrayList<>();
            final JSONArray array = get(KEY_TRACKED_EVENTS);
            for (final Object json : array) {
                events.add(new TrackedEvent((JSONObject) json));
            }
            return events;
        }
    }

    public class TrackedEvent extends JsonWrapper {

        public TrackedEvent(JSONObject json) {
            super(json);
        }

        public String getEventId() {
            return get(KEY_EVENT_ID);
        }

        public String getEventName() {
            return get(KEY_EVENT_NAME);
        }

        public String getEventStart() {
            return get(KEY_EVENT_START);
        }

        public String getEventEnd() {
            return get(KEY_EVENT_END);
        }

        public String getEventBaseUrl() {
            return get(KEY_EVENT_BASE_URL);
        }

        public boolean isArchived() {
            return get(KEY_EVENT_IS_ARCHIVED);
        }

        public boolean isOwner() {
            return get(KEY_EVENT_IS_OWNER);
        }

        public Iterable<TrackedElement> getTrackedElements() {
            final JSONArray array = get(KEY_EVENT_TRACKED_ELEMENTS);
            final Collection<TrackedElement> col = new ArrayList<>();
            if (array != null) {
                for (final Object w : array) {
                    col.add(new TrackedElement((JSONObject) w));
                }
            }
            return col;
        }

        public String getRegattaId() {
            return get(KEY_REGATTA_ID);
        }
    }

}
