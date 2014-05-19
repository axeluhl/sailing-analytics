package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.SailingServer;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

public class ReadEventsFromSailingServerCallable implements Callable<Map<SailingServer, Iterable<EventBase>>> {
    private final SailingServer sailingServer;
    private final URL remoteUrl;
    private final MalformedURLException urlException;

    public ReadEventsFromSailingServerCallable(SailingServer sailingServer) {
        this.sailingServer = sailingServer;
        String getEventsUrl = sailingServer.getURL().toExternalForm();
        if (!getEventsUrl.endsWith("/")) {
            getEventsUrl += "/";
        }
        getEventsUrl += "sailingserver/api/v1/events";
        URL url = null;
        MalformedURLException mue = null;
        try {
            url = new URL(getEventsUrl);
        } catch (MalformedURLException e) {
            // bad URL
            mue = e;
        }
        remoteUrl = url;
        urlException = mue;
    }

    public SailingServer getSailingServer() {
        return sailingServer;
    }

    @Override
    public Map<SailingServer, Iterable<EventBase>> call() throws Exception {
        if (urlException != null) {
            throw urlException;
        }
        Map<SailingServer, Iterable<EventBase>> result = new HashMap<>();
        BufferedReader bufferedReader = null;
        try {
            URLConnection urlConnection = remoteUrl.openConnection();
            urlConnection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            JSONParser parser = new JSONParser();
            Object eventsAsObject = parser.parse(bufferedReader);
            EventBaseJsonDeserializer deserializer = new EventBaseJsonDeserializer(new VenueJsonDeserializer(
                    new CourseAreaJsonDeserializer(DomainFactory.INSTANCE)));
            JSONArray eventsAsJsonArray = (JSONArray) eventsAsObject;
            final List<EventBase> events = new ArrayList<>();
            for (Object eventAsObject : eventsAsJsonArray) {
                JSONObject eventAsJson = (JSONObject) eventAsObject;
                EventBase event = deserializer.deserialize(eventAsJson);
                events.add(event);
            }
            result.put(sailingServer, events);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return result;
    }

}