package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.SailingServer;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

public class ReadEventsFromSailingServerCallable implements Callable<List<Pair<SailingServer, EventBase>>> {
    private SailingServer sailingServer;
    private URL remoteUrl;
    private MalformedURLException urlException;

    public ReadEventsFromSailingServerCallable(SailingServer sailingServer) {
        this.sailingServer = sailingServer;
        String getEventsUrl = sailingServer.getURL().toExternalForm();
        if (!getEventsUrl.endsWith("/")) {
            getEventsUrl += "/";
        }
        getEventsUrl += "sailingserver/api/v1/events";

        try {
            remoteUrl = new URL(getEventsUrl);
        } catch (MalformedURLException e) {
            // bad URL
            urlException = e;
        }
    }

    @Override
    public List<Pair<SailingServer, EventBase>> call() throws Exception {
        if (urlException != null) {
            throw urlException;
        }

        List<Pair<SailingServer, EventBase>> result = new ArrayList<Pair<SailingServer, EventBase>>();
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
            for (Object eventAsObject : eventsAsJsonArray) {
                JSONObject eventAsJson = (JSONObject) eventAsObject;
                EventBase event = deserializer.deserialize(eventAsJson);
                result.add(new Pair<SailingServer, EventBase>(sailingServer, event));
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        return result;
    }

}