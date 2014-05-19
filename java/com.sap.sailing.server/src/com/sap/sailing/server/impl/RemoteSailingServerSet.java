package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;

/**
 * A set of {@link RemoteSailingServerReference}s including a cache of their {@link Event}s that is
 * periodically updated.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RemoteSailingServerSet {
    private static final Logger logger = Logger.getLogger(RemoteSailingServerSet.class.getName());
    
    /**
     * Holds the {@link Event} objects for the events of all registered sailing server instances.
     */
    private final ConcurrentHashMap<String, RemoteSailingServerReference> remoteSailingServers;
    
    private final ConcurrentHashMap<RemoteSailingServerReference, Pair<Iterable<EventBase>, Exception>> cachedEventsForRemoteSailingServers;

    /**
     * @param scheduler
     *            Used to schedule the periodic updates of the {@link #cachedEventsForRemoteSailingServers event cache}
     */
    public RemoteSailingServerSet(ScheduledExecutorService scheduler) {
        remoteSailingServers = new ConcurrentHashMap<>();
        cachedEventsForRemoteSailingServers = new ConcurrentHashMap<>();
        scheduler.scheduleAtFixedRate(new Runnable() { @Override public void run() { updateRemoteSailingServerReferenceEventCaches(); } },
                /* initialDelay */ 0, /* period */ 60, TimeUnit.SECONDS);
    }

    public void clear() {
        remoteSailingServers.clear();
        cachedEventsForRemoteSailingServers.clear();
    }

    public void add(RemoteSailingServerReference remoteSailingServerReference) {
        remoteSailingServers.put(remoteSailingServerReference.getName(), remoteSailingServerReference);
        triggerAsynchronousEventCacheUpdate(remoteSailingServerReference);
    }
    
    private void updateRemoteSailingServerReferenceEventCaches() {
        for (RemoteSailingServerReference ref : remoteSailingServers.values()) {
            triggerAsynchronousEventCacheUpdate(ref);
        }
    }

    private void triggerAsynchronousEventCacheUpdate(final RemoteSailingServerReference ref) {
        new Thread("Event Cache Updater for remote server "+ref) {
            @Override public void run() { updateRemoteServerEventCacheSynchronously(ref); }
        }.start();
    }

    private Pair<Iterable<EventBase>, Exception> updateRemoteServerEventCacheSynchronously(RemoteSailingServerReference ref) {
        BufferedReader bufferedReader = null;
        Pair<Iterable<EventBase>, Exception> result;
        try {
            try {
                final URL eventsURL = getEventsURL(ref.getURL());
                logger.fine("Updating events for remote server "+ref+" from URL "+eventsURL);
                URLConnection urlConnection = eventsURL.openConnection();
                urlConnection.connect();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                JSONParser parser = new JSONParser();
                Object eventsAsObject = parser.parse(bufferedReader);
                EventBaseJsonDeserializer deserializer = new EventBaseJsonDeserializer(new VenueJsonDeserializer(
                        new CourseAreaJsonDeserializer(DomainFactory.INSTANCE)));
                JSONArray eventsAsJsonArray = (JSONArray) eventsAsObject;
                final Set<EventBase> events = new HashSet<>();
                for (Object eventAsObject : eventsAsJsonArray) {
                    JSONObject eventAsJson = (JSONObject) eventAsObject;
                    EventBase event = deserializer.deserialize(eventAsJson);
                    events.add(event);
                }
                result = new Pair<Iterable<EventBase>, Exception>(events, /* exception */ null);
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.INFO, "Exception trying to fetch events from remote server "+ref+": "+e.getMessage(), e);
            result = new Pair<Iterable<EventBase>, Exception>(/* events */ null, e);
        }
        cachedEventsForRemoteSailingServers.put(ref, result);
        return result;
    }
    
    private URL getEventsURL(URL remoteServerBaseURL) throws MalformedURLException {
        String getEventsUrl = remoteServerBaseURL.toExternalForm();
        if (!getEventsUrl.endsWith("/")) {
            getEventsUrl += "/";
        }
        getEventsUrl += "sailingserver/api/v1/events";
        return new URL(getEventsUrl);
    }

    public Map<RemoteSailingServerReference, Pair<Iterable<EventBase>, Exception>> getCachedEventsForRemoteSailingServers() {
        return Collections.unmodifiableMap(cachedEventsForRemoteSailingServers);
    }

    public RemoteSailingServerReference remove(String name) {
        RemoteSailingServerReference ref = remoteSailingServers.remove(name);
        if (ref != null) {
            cachedEventsForRemoteSailingServers.remove(ref);
        }
        return ref;
    }

    public Pair<Iterable<EventBase>, Exception> getCachedEventsOrException(RemoteSailingServerReference ref) {
        return cachedEventsForRemoteSailingServers.get(ref);
    }
}
