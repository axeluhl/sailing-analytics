package com.sap.sailing.domain.windfinderadapter.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.simple.parser.ParseException;
import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.WindFinderReviewedSpotsCollectionIdProvider;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.windfinder.ReviewedSpotsCollection;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;
import com.sap.sse.common.Util;
import com.sap.sse.util.ServiceTrackerFactory;
import com.sap.sse.util.ThreadPoolUtil;

public class WindFinderTrackerFactoryImpl implements WindFinderTrackerFactory {
    private final Map<RaceDefinition, WindTracker> windTrackerPerRace;
    
    /**
     * The initial filling of this cache is either accomplished immediately if no
     * {@link #reviewedSpotsCollectionIdProvider} is available, or the provider is waited for in a background thread and
     * then requested to deliver its reviewed spot collection IDs, and once those are received, the future is fulfilled.
     */
    private final Future<ConcurrentMap<String, ReviewedSpotsCollection>> reviewedSpotsCollectionsByIdCache;
    
    /**
     * Can hold reviewed spots collections. Starts out valid but empty. Useful if we're running outside of an OSGi
     * environment such as a test set-up. The collections provided here will be added to the set returned by
     * {@link #getReviewedSpotsCollections(boolean)}.
     */
    private Set<ReviewedSpotsCollection> reviewedSpotsCollections;
    private final ServiceTracker<WindFinderReviewedSpotsCollectionIdProvider, WindFinderReviewedSpotsCollectionIdProvider> reviewedSpotsCollectionIdProvider;
    
    private final ScheduledExecutorService executor;

    public WindFinderTrackerFactoryImpl() {
        this.windTrackerPerRace = new HashMap<>();
        this.executor = ThreadPoolUtil.INSTANCE.getDefaultBackgroundTaskThreadPoolExecutor();
        this.reviewedSpotsCollections = Collections.synchronizedSet(new HashSet<>());
        if (Activator.getContext() != null) {
            reviewedSpotsCollectionIdProvider = ServiceTrackerFactory.createAndOpen(Activator.getContext(), WindFinderReviewedSpotsCollectionIdProvider.class);
            reviewedSpotsCollectionsByIdCache = executor.schedule(
                    ()->{
                        final ConcurrentMap<String, ReviewedSpotsCollection> result = new ConcurrentHashMap<>();
                        reviewedSpotsCollectionIdProvider.getService();
                        for (final ReviewedSpotsCollection c : loadSpotCollectionsFromProvider(/* lookupInCache */ false)) {
                            result.put(c.getId(), c);
                        }
                        return result;
                    }, /*delay*/ 0, TimeUnit.MILLISECONDS);
        } else {
            reviewedSpotsCollectionIdProvider = null;
            final ConcurrentMap<String, ReviewedSpotsCollection> cache = new ConcurrentHashMap<>();
            this.reviewedSpotsCollectionsByIdCache = new Future<ConcurrentMap<String,ReviewedSpotsCollection>>() {
                @Override public boolean cancel(boolean mayInterruptIfRunning) { return true; }
                @Override public boolean isCancelled() {  return false; }
                @Override public boolean isDone() { return true; }
                @Override public ConcurrentMap<String, ReviewedSpotsCollection> get() { return cache; }
                @Override public ConcurrentMap<String, ReviewedSpotsCollection> get(long timeout, TimeUnit unit) { return cache; }
            };
        }
    }

    @Override
    public WindTracker createWindTracker(DynamicTrackedRegatta trackedRegatta, RaceDefinition race,
            boolean correctByDeclination) throws Exception {
        final WindTracker result;
        synchronized (windTrackerPerRace) {
            final WindTracker existingWindTrackerForRace = getExistingWindTracker(race);
            if (existingWindTrackerForRace == null) {
                final DynamicTrackedRace trackedRace = trackedRegatta.getTrackedRace(race);
                result = new WindFinderWindTracker(trackedRace, this);
                windTrackerPerRace.put(race, result);
            } else {
                result = existingWindTrackerForRace;
            }
        }
        return result;
    }

    @Override
    public WindTracker getExistingWindTracker(RaceDefinition race) {
        synchronized (windTrackerPerRace) {
            return windTrackerPerRace.get(race);
        }
    }
    
    void trackerStopped(RaceDefinition race) {
        synchronized (windTrackerPerRace) {
            windTrackerPerRace.remove(race);
        }
    }

    @Override
    public Iterable<ReviewedSpotsCollection> getReviewedSpotsCollections(boolean cached) throws InterruptedException, ExecutionException {
        final Set<ReviewedSpotsCollection> result = new HashSet<>();
        if (cached) {
            result.addAll(reviewedSpotsCollectionsByIdCache.get().values());
        } else {
            result.addAll(reviewedSpotsCollections);
            Util.addAll(loadSpotCollectionsFromProvider(/* lookupInCache */ true), result);
            for (final ReviewedSpotsCollection c : result) {
                reviewedSpotsCollectionsByIdCache.get().putIfAbsent(c.getId(), c);
            }
        }
        return result;
    }

    private Iterable<ReviewedSpotsCollection> loadSpotCollectionsFromProvider(boolean lookupInCache) throws InterruptedException, ExecutionException {
        final Set<ReviewedSpotsCollection> result = new HashSet<>();
        final WindFinderReviewedSpotsCollectionIdProvider provider;
        if (reviewedSpotsCollectionIdProvider != null && (provider = reviewedSpotsCollectionIdProvider.getService()) != null) {
            for (String id : provider.getWindFinderReviewedSpotsCollectionIds()) {
                result.add(getReviewedSpotsCollectionById(id, lookupInCache));
            }
        }
        return result;
    }

    @Override
    public ReviewedSpotsCollection getReviewedSpotsCollectionById(String spotsCollectionId, boolean lookupInCache) throws InterruptedException, ExecutionException {
        ReviewedSpotsCollection result;
        if (!lookupInCache || (result = reviewedSpotsCollectionsByIdCache.get().get(spotsCollectionId)) == null) {
            final ReviewedSpotsCollectionImpl finalResult = new ReviewedSpotsCollectionImpl(spotsCollectionId);
            result = finalResult;
            executor.schedule(()->reviewedSpotsCollectionsByIdCache.get().put(spotsCollectionId, finalResult), /* delay */ 0, TimeUnit.MILLISECONDS);
        }
        return result;
    }

    @Override
    public Spot getSpotById(String spotId, boolean cached) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException {
        Spot result = null;
        for (final ReviewedSpotsCollection coll : getReviewedSpotsCollections(/* cached */ cached)) {
            for (final Spot spot : coll.getSpots(/* cached */ cached)) {
                if (Util.equalsWithNull(spot.getId(), spotId)) {
                    result = spot;
                    break;
                }
            }
        }
        return result;
    }
    
    public void addReviewedSpotCollection(ReviewedSpotsCollection collection) throws InterruptedException, ExecutionException {
        reviewedSpotsCollections.add(collection);
        reviewedSpotsCollectionsByIdCache.get().put(collection.getId(), collection);
    }

    public void removeReviewedSpotCollection(ReviewedSpotsCollection collection) throws InterruptedException, ExecutionException {
        reviewedSpotsCollections.remove(collection);
        reviewedSpotsCollectionsByIdCache.get().remove(collection.getId());
    }
}
