package com.sap.sailing.server.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.anniversary.DetailedRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleRaceInfo;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.statistics.Statistics;
import com.sap.sailing.server.gateway.deserialization.impl.CourseAreaJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.EventBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.LeaderboardGroupBaseJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.StatisticsByYearJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.StatisticsJsonDeserializer;
import com.sap.sailing.server.gateway.deserialization.impl.VenueJsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DetailedRaceInfoJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.SimpleRaceInfoJsonSerializer;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;
import com.sap.sse.util.HttpUrlConnectionHelper;

/**
 * A set of {@link RemoteSailingServerReference}s including a cache of their {@link EventBase events} that is
 * periodically updated.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RemoteSailingServerSet {
    private static final int POLLING_INTERVAL_IN_SECONDS = 60;
    private NamedReentrantReadWriteLock lock = new NamedReentrantReadWriteLock("lock for RemoteSailingServerSet", true);

    private static final Logger logger = Logger.getLogger(RemoteSailingServerSet.class.getName());

    /**
     * Holds the remote server references managed by this set. Keys are the
     * {@link RemoteSailingServerReference#getName() names} of the server references.
     */
    private final ConcurrentMap<String, RemoteSailingServerReference> remoteSailingServers;

    private final ConcurrentMap<RemoteSailingServerReference, Util.Pair<Iterable<EventBase>, Exception>> cachedEventsForRemoteSailingServers;

    private final ConcurrentMap<RemoteSailingServerReference, Util.Pair<Map<Integer, Statistics>, Exception>> cachedStatisticsByYearForRemoteSailingServers;
    private final StatisticsJsonDeserializer statisticsJsonDeserializer;
    
    /**
     * Tracked races content is not replicated, because only the master (e.g. archive) determines anniversaries. The
     * results of this determination are replicated. In a replica, {@link #cachedTrackedRacesForRemoteSailingServers}
     * remains empty, as no remote servers are queried for their tracked races lists.
     */
    private final ConcurrentMap<RemoteSailingServerReference, Util.Pair<Iterable<SimpleRaceInfo>, Exception>> cachedTrackedRacesForRemoteSailingServers;
    
    /**
     * {@link Set} of {@link Runnable callback}s, which are called when retrieving tracked races from a remote server
     */
    private final Set<Runnable> remoteRaceResultReceivedCallbacks = ConcurrentHashMap.newKeySet();
    
    /**
     * {@link AtomicBoolean Flag} to enable or disable retrieval of tracked races from remote servers. Usually
     * <code>false</code>/disable for replicas in order to save bandwidth and CPU.
     */
    private final AtomicBoolean retrieveRemoteRaceResult = new AtomicBoolean(true);

    /**
     * @param scheduler
     *            Used to schedule the periodic updates of the {@link #cachedEventsForRemoteSailingServers event cache}
     */
    public RemoteSailingServerSet(ScheduledExecutorService scheduler, SharedDomainFactory baseDomainFactory) {
        this.statisticsJsonDeserializer = StatisticsJsonDeserializer.create(baseDomainFactory);
        remoteSailingServers = new ConcurrentHashMap<>();
        cachedEventsForRemoteSailingServers = new ConcurrentHashMap<>();
        cachedStatisticsByYearForRemoteSailingServers = new ConcurrentHashMap<>();
        cachedTrackedRacesForRemoteSailingServers = new ConcurrentHashMap<>();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                updateRemoteSailingServerReferenceEventCaches();
            }
        }, /* initialDelay */ 0, POLLING_INTERVAL_IN_SECONDS, TimeUnit.SECONDS);
    }

    public void clear() {
        LockUtil.executeWithWriteLock(lock, () -> {
            remoteSailingServers.clear();
            cachedEventsForRemoteSailingServers.clear();
            cachedStatisticsByYearForRemoteSailingServers.clear();
            cachedTrackedRacesForRemoteSailingServers.clear();
        });
    }

    public void add(RemoteSailingServerReference remoteSailingServerReference) {
        LockUtil.executeWithWriteLock(lock, () -> {
            remoteSailingServers.put(remoteSailingServerReference.getName(), remoteSailingServerReference);
            triggerAsynchronousEventCacheUpdate(remoteSailingServerReference);
        });
    }

    private void updateRemoteSailingServerReferenceEventCaches() {
        LockUtil.executeWithReadLock(lock, () -> {
            for (RemoteSailingServerReference ref : remoteSailingServers.values()) {
                triggerAsynchronousEventCacheUpdate(ref);
            }
        });
    }

    /**
     * If this set has a {@link RemoteSailingServerReference} whose {@link RemoteSailingServerReference#getName() name}
     * equals <code>name</code>, it is returned. Otherwise, <code>null</code> is returned.
     */
    public RemoteSailingServerReference getServerReferenceByName(String name) {
        return remoteSailingServers.get(name);
    }

    private void triggerAsynchronousEventCacheUpdate(final RemoteSailingServerReference ref) {
        new Thread(() -> updateRemoteServerEventCacheSynchronously(ref), "Event Cache Updater for remote server " + ref).start();
        new Thread(() -> updateRemoteServerStatisticsCacheSynchronously(ref),
                "Statistics by year Cache Updater for remote server " + ref).start();
        if (retrieveRemoteRaceResult.get()) {
            new Thread(() -> updateRemoteServerTrackedRacesCacheSynchronously(ref),
                    "Anniversary Cache Updater for remote server " + ref).start();
        }
    }

    /**
     * Sets the {@link #retrieveRemoteRaceResult flag} to control whether or not this {@link RemoteSailingServerSet}
     * instance attempts to load tracked races lists from remote servers. It is expected to be set to <code>false</code>
     * for replicas, what will also clear the {@link #cachedTrackedRacesForRemoteSailingServers tracked races cache} of
     * this instance.
     * 
     * @param retrieveRemoteRaceResult
     *            <code>true</code> to enable tracked races retrieval from remote servers, <code>false</code> to disable
     */
    public void setRetrieveRemoteRaceResult(boolean retrieveRemoteRaceResult) {
        this.retrieveRemoteRaceResult.set(retrieveRemoteRaceResult);
        synchronized (cachedEventsForRemoteSailingServers) {
            if (!this.retrieveRemoteRaceResult.get()) {
                cachedTrackedRacesForRemoteSailingServers.clear();
            }
        }
    }

    /**
     * This method will load the list of all tracked races from the provided {@link RemoteSailingServerReference remote
     * server} instance (transitively), by calling REST API's <code>TrackedRaceListResource</code>
     * ("/v1/trackedRaces/getRaces").
     * 
     * @param ref
     *            the {@link RemoteSailingServerReference} to load tracked races from
     */
    private void updateRemoteServerTrackedRacesCacheSynchronously(RemoteSailingServerReference ref) {
        Util.Pair<Iterable<SimpleRaceInfo>, Exception> result;
        try {
            final URL raceListURL = getRaceListURL(ref.getURL());
            logger.fine("Updating racelist for remote server " + ref + " from URL " + raceListURL);
            final SimpleRaceInfoJsonSerializer deserializer = new SimpleRaceInfoJsonSerializer();
            final Set<SimpleRaceInfo> races = new HashSet<>();
            for (Object remoteWithRaces : getJsonFromRemoteServerSynchronously(ref, raceListURL)) {
                JSONObject remoteWithRacesAsJson = (JSONObject) remoteWithRaces;
                String remoteUrlAsString = (String) remoteWithRacesAsJson
                        .get(DetailedRaceInfoJsonSerializer.FIELD_REMOTEURL);
                URL remoteUrl;
                if (remoteUrlAsString != null && !remoteUrlAsString.isEmpty()) {
                    remoteUrl = new URL(remoteUrlAsString);
                } else {
                    // if the race was local to the remote server, indicated by a null URL; use the remote server's URL
                    remoteUrl = ref.getURL();
                }
                JSONArray raceListForOneRemote = (JSONArray) remoteWithRacesAsJson
                        .get(DetailedRaceInfoJsonSerializer.FIELD_RACES);
                for (Object remoteRace : raceListForOneRemote) {
                    JSONObject remoteRaceAsJson = (JSONObject) remoteRace;
                    SimpleRaceInfo event = deserializer.deserialize(remoteRaceAsJson, remoteUrl);
                    races.add(event);
                }
            }
            result = new Util.Pair<Iterable<SimpleRaceInfo>, Exception>(races, /* exception */ null);
        } catch (Exception e) {
            logger.log(Level.INFO,
                    "Exception trying to fetch AnniversaryRaceData from remote server " + ref + ": " + e.getMessage(),
                    e);
            result = new Util.Pair<Iterable<SimpleRaceInfo>, Exception>(/* events */ null, e);
        }
        synchronized (cachedTrackedRacesForRemoteSailingServers) {
            if (retrieveRemoteRaceResult.get()) {
                updateCache(ref, result, cachedTrackedRacesForRemoteSailingServers::put);
            }
        }
        notifyRemoteRaceResultReceivedCallbacks();
    }

    /**
     * Calls all {@link #addRemoteRaceResultReceivedCallback(Runnable) registered} callbacks, to notify them about the
     * availability of newer, but not necessarily different tracked races results.
     */
    private void notifyRemoteRaceResultReceivedCallbacks() {
        new HashSet<>(remoteRaceResultReceivedCallbacks).forEach(Runnable::run);
    }

    private URL getRaceListURL(URL remoteServerBaseURL) throws MalformedURLException {
        return getEndpointUrl(remoteServerBaseURL, "/trackedRaces/getRaces?transitive=true");
    }

    private URL getEndpointUrl(URL remoteServerBaseURL, final String endpoint) throws MalformedURLException {
        return getURL(remoteServerBaseURL, "sailingserver/api/v1"+endpoint);
    }

    private Util.Pair<Iterable<EventBase>, Exception> updateRemoteServerEventCacheSynchronously(
            RemoteSailingServerReference ref) {
        BufferedReader bufferedReader = null;
        Util.Pair<Iterable<EventBase>, Exception> result;
        try {
            try {
                final URL eventsURL = getEventsURL(ref.getURL());
                logger.fine("Updating events for remote server " + ref + " from URL " + eventsURL);
                URLConnection urlConnection = HttpUrlConnectionHelper.redirectConnection(eventsURL);
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                JSONParser parser = new JSONParser();
                Object eventsAsObject = parser.parse(bufferedReader);
                EventBaseJsonDeserializer deserializer = new EventBaseJsonDeserializer(
                        new VenueJsonDeserializer(new CourseAreaJsonDeserializer(DomainFactory.INSTANCE)),
                        new LeaderboardGroupBaseJsonDeserializer());
                JSONArray eventsAsJsonArray = (JSONArray) eventsAsObject;
                final Set<EventBase> events = new HashSet<>();
                for (Object eventAsObject : eventsAsJsonArray) {
                    JSONObject eventAsJson = (JSONObject) eventAsObject;
                    EventBase event = deserializer.deserialize(eventAsJson);
                    events.add(event);
                }
                result = new Util.Pair<Iterable<EventBase>, Exception>(events, /* exception */ null);
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.INFO, "Exception trying to fetch events from remote server " + ref + ": " + e.getMessage(),
                    e);
            result = new Util.Pair<Iterable<EventBase>, Exception>(/* events */ null, e);
        }
        final Pair<Iterable<EventBase>, Exception> finalResult = result;
        LockUtil.executeWithWriteLock(lock, () -> {
            // check that the server was not removed while no lock was held
            if (remoteSailingServers.containsValue(ref)) {
                cachedEventsForRemoteSailingServers.put(ref, finalResult);
            } else {
                logger.fine("Omitted update for " + ref + " as it was removed");
            }
        });
        return result;
    }

    private void updateRemoteServerStatisticsCacheSynchronously(RemoteSailingServerReference ref) {
        Util.Pair<Map<Integer, Statistics>, Exception> result;
        try {
            final URL statisticsByYearURL = getStatisticsByYearURL(ref.getURL());
            logger.fine("Updating by-year statistics for remote server " + ref + " from URL " + statisticsByYearURL);
            JSONArray statisticsByYear = getJsonFromRemoteServerSynchronously(ref, statisticsByYearURL);
            StatisticsByYearJsonDeserializer deserializer = new StatisticsByYearJsonDeserializer(statisticsJsonDeserializer);
            final Map<Integer, Statistics> statisticsByYearMap = new HashMap<>();
            for (Object entry : statisticsByYear) {
                JSONObject jsonEntry = (JSONObject) entry;
                Pair<Integer, Statistics> pair = deserializer.deserialize(jsonEntry);
                statisticsByYearMap.put(pair.getA(), pair.getB());
            }
            result = new Pair<Map<Integer, Statistics>, Exception>(statisticsByYearMap, /* exception */ null);
        } catch (IOException | ParseException e) {
            logger.log(Level.INFO,
                    "Exception trying to fetch by-year statistics from remote server " + ref + ": " + e.getMessage(),
                    e);
            result = new Util.Pair<Map<Integer, Statistics>, Exception>(/* statistics by year */ null, e);
        }
        updateCache(ref, result, cachedStatisticsByYearForRemoteSailingServers::put);
    }

    private JSONArray getJsonFromRemoteServerSynchronously(RemoteSailingServerReference ref,
            final URL url) throws IOException, ParseException {
        logger.fine("Updating data for remote server " + ref + " from URL " + url);
        URLConnection urlConnection = HttpUrlConnectionHelper.redirectConnection(url);
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
            JSONParser parser = new JSONParser();
            return (JSONArray) parser.parse(bufferedReader);
        }
    }

    private <T> void updateCache(final RemoteSailingServerReference ref, final Util.Pair<T, Exception> result,
            final BiConsumer<RemoteSailingServerReference, Util.Pair<T, Exception>> updateCacheCallback) {
        LockUtil.executeWithWriteLock(lock, () -> {
            // check that the server was not removed while no lock was held
            if (remoteSailingServers.containsValue(ref)) {
                updateCacheCallback.accept(ref, result);
            } else {
                logger.fine("Omitted update for " + ref + " as it was removed");
            }
        });
    }

    private URL getEventsURL(URL remoteServerBaseURL) throws MalformedURLException {
        return getEndpointUrl(remoteServerBaseURL, "/events");
    }
    
    private URL getStatisticsByYearURL(URL remoteServerBaseURL) throws MalformedURLException {
        return getEndpointUrl(remoteServerBaseURL, "/statistics/years");
    }

    private URL getURL(URL remoteServerBaseURL, String subURL) throws MalformedURLException {
        String baseURL = remoteServerBaseURL.toExternalForm();
        if (!baseURL.endsWith("/")) {
            baseURL += "/";
        }
        return new URL(baseURL + subURL);
    }

    public Map<RemoteSailingServerReference, Util.Pair<Iterable<EventBase>, Exception>> getCachedEventsForRemoteSailingServers() {
        LockUtil.lockForRead(lock);
        try {
            return Collections.unmodifiableMap(cachedEventsForRemoteSailingServers);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    public RemoteSailingServerReference remove(String name) {
        RemoteSailingServerReference ref = null;
        LockUtil.lockForWrite(lock);
        try{
            ref = remoteSailingServers.remove(name);
            if (ref != null) {
                cachedEventsForRemoteSailingServers.remove(ref);
                cachedStatisticsByYearForRemoteSailingServers.remove(ref);
                cachedTrackedRacesForRemoteSailingServers.remove(ref);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
        notifyRemoteRaceResultReceivedCallbacks();
        return ref;
    }

    /**
     * Synchronously fetches the latest events list for the remote server reference specified. The result is cached. If
     * <code>ref</code> was not yet part of this remote sailing server reference set, it is automatically added.
     */
    public Util.Pair<Iterable<EventBase>, Exception> getEventsOrException(RemoteSailingServerReference ref) {
        LockUtil.lockForWrite(lock);
        try {
            if (!remoteSailingServers.containsKey(ref.getName())) {
                remoteSailingServers.put(ref.getName(), ref);
            }
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
        return updateRemoteServerEventCacheSynchronously(ref);
    }

    public Iterable<RemoteSailingServerReference> getLiveRemoteServerReferences() {
        List<RemoteSailingServerReference> result = new ArrayList<>();
        LockUtil.lockForRead(lock);
        try {
            for (Map.Entry<RemoteSailingServerReference, Pair<Iterable<EventBase>, Exception>> e : cachedEventsForRemoteSailingServers.entrySet()) {
                if (e.getValue().getB() == null) {
                    // no exception; reference considered live
                    result.add(e.getKey());
                }
            }
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
        return result;
    }
    
    public Map<RemoteSailingServerReference, Util.Pair<Map<Integer, Statistics>, Exception>> getCachedStatisticsForRemoteSailingServers() {
        LockUtil.lockForRead(lock);
        try {
            return Collections.unmodifiableMap(cachedStatisticsByYearForRemoteSailingServers);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    /**
     * Provided the currently cached tracked races lists group by the {@link RemoteSailingServerReference remote server}
     * instances where they were originally retrieved from. This cache is determined by periodical updates.
     */
    public Map<RemoteSailingServerReference, Pair<Iterable<SimpleRaceInfo>, Exception>> getCachedRaceList() {
        LockUtil.lockForRead(lock);
        try {
            return Collections.unmodifiableMap(cachedTrackedRacesForRemoteSailingServers);
        } finally {
            LockUtil.unlockAfterRead(lock);
        }
    }

    /**
     * Retrieves {@link DetailedRaceInfo detailed information} for the provided {@link SimpleRaceInfo race} from the
     * {@link SimpleRaceInfo#getRemoteUrl() corresponding remote server URL} (blocking operation).
     * 
     * @param matching
     *            the {@link SimpleRaceInfo} to get the detailed information for
     * @return the retrieved {@link DetailedRaceInfo detailed information}
     */
    public DetailedRaceInfo getDetailedInfoBlocking(SimpleRaceInfo matching) {
        try {
            URL remoteRequestUrl = getDetailRaceInfoURL(matching.getRemoteUrl(), matching);
            logger.fine("Loading DetailedRace data for remote server from URL " + remoteRequestUrl);
            URLConnection urlConnection = HttpUrlConnectionHelper.redirectConnection(remoteRequestUrl);
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
                JSONParser parser = new JSONParser();
                JSONObject detailedInfoAsJson =  (JSONObject) parser.parse(bufferedReader);
                DetailedRaceInfoJsonSerializer detailedRaceListJsonSerializer = new DetailedRaceInfoJsonSerializer();
                DetailedRaceInfo detailedInfoAsJava = detailedRaceListJsonSerializer.deserialize(detailedInfoAsJson);
                if (detailedInfoAsJava.getRemoteUrl() == null) {
                    // not transitive, use direct url
                    detailedInfoAsJava = new DetailedRaceInfo(detailedInfoAsJava, matching.getRemoteUrl());
                }
                return detailedInfoAsJava;
            }
        } catch (ParseException | IOException e) {
            throw new IllegalStateException("RemoteUrl could not be called ", e);
        }
    }

    private URL getDetailRaceInfoURL(URL remoteServerBaseURL, SimpleRaceInfo matching)
            throws MalformedURLException, UnsupportedEncodingException {
        String raceName = URLEncoder.encode(matching.getIdentifier().getRaceName(), "UTF-8").replace("+", "%20");
        String regattaName = URLEncoder.encode(matching.getIdentifier().getRegattaName(), "UTF-8").replace("+", "%20");
        String params = "raceName=" + raceName + "&regattaName=" + regattaName;
        return getEndpointUrl(remoteServerBaseURL, "/trackedRaces/raceDetails?" + params);
    }

    /**
     * Registers a {@link Runnable callback} which is {@link Runnable#run() called}, after a retrieval of tracked races
     * from a {@link RemoteSailingServerReference remote server} instance.
     * 
     * @param callback
     *            {@link Runnable} to register
     */
    public void addRemoteRaceResultReceivedCallback(Runnable callback) {
        this.remoteRaceResultReceivedCallbacks.add(callback);
    }
    
    /**
     * Unregisters the given {@link Runnable callback} if it was {@link #add(RemoteSailingServerReference) registered}
     * before, otherwise this is operation has no effect.
     * 
     * @param callback
     *            {@link Runnable} to unregister
     */
    public void removeRemoteRaceResultReceivedCallback(Runnable callback){
        this.remoteRaceResultReceivedCallbacks.remove(callback);
    }
}
