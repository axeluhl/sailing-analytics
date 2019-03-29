package com.sap.sailing.selenium.api.event;

import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.ApiContext;

public class EventApi {

    private static final String CREATE_EVENT_URL = "/api/v1/events/createEvent";
    private static final String LIST_EVENTS = "/api/v1/events";

    public JSONObject createEvent(ApiContext ctx, String eventName, String boatclassname,
            String competitorRegistrationType, String venuename) {
        Map<String, String> formParams = new TreeMap<>();
        formParams.put("boatclassname", boatclassname);
        formParams.put("competitorRegistrationType", competitorRegistrationType);
        formParams.put("eventName", eventName);
        formParams.put("venuename", venuename);

        return ctx.post(CREATE_EVENT_URL, null, formParams);
    }

    public JSONObject getEvent(ApiContext ctx, String eventId) {
        return ctx.get(LIST_EVENTS + "/" + eventId);
    }

}
