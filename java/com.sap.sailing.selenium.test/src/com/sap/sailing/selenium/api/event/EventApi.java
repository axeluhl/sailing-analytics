package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.selenium.api.core.ApiContext;
import com.sap.sailing.selenium.api.core.JsonWrapper;

public class EventApi {

    private static final String CREATE_EVENT_URL = "/api/v1/events/createEvent";
    private static final String LIST_EVENTS = "/api/v1/events";

    public Event createEvent(ApiContext ctx, String eventName, String boatclassname,
            CompetitorRegistrationType competitorRegistrationType, String venuename) {
        Map<String, String> formParams = new TreeMap<>();
        formParams.put("boatclassname", boatclassname);
        formParams.put("competitorRegistrationType", competitorRegistrationType.name());
        formParams.put("eventName", eventName);
        formParams.put("venuename", venuename);
        EventForCreate event = new EventForCreate(ctx.post(CREATE_EVENT_URL, null, formParams));
        return event;
    }

    public Event getEvent(ApiContext ctx, String eventId) {
        Event event = new Event(ctx.get(LIST_EVENTS + "/" + eventId));
        return event;
    }

    public class Event extends JsonWrapper {
        private Event(JSONObject json) {
            super(json);
        }

        public String getId() {
            return get("id");
        }

        public String getName() {
            return get("name");
        }

        public String getSecret() {
            return null;
        }

        public Long getStartDate() {
            return get("startDate");
        }

        public Long getEndDate() {
            return get("endDate");
        }
    }

    private class EventForCreate extends Event {

        private EventForCreate(JSONObject json) {
            super(json);
        }

        public String getId() {
            return get("eventid");
        }

        public String getName() {
            return get("eventname");
        }

        public String getSecret() {
            return get("registrationSecret");
        }

        public Long getStartDate() {
            return get("eventstartdate");
        }

        public Long getEndDate() {
            return (Long) get("eventenddate");
        }
    }
}
