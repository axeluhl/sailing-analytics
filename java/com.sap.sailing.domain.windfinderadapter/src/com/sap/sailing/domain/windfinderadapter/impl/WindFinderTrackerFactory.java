package com.sap.sailing.domain.windfinderadapter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.util.tracker.ServiceTracker;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.WindFinderReviewedSpotsCollectionIdProvider;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.windfinderadapter.ReviewedSpotsCollection;
import com.sap.sse.common.Util;
import com.sap.sse.util.ServiceTrackerFactory;

public class WindFinderTrackerFactory implements WindTrackerFactory {
    private final Map<RaceDefinition, WindTracker> windTrackerPerRace;
    
    /**
     * Can hold reviewed spots collections. Starts out as {@code null}. If this is the case, an attempt
     * will be made to retrieve spot collection IDs from {@link #reviewedSpotsCollectionIdProvider} if
     * that is not {@code null} either, e.g., because we're running outside of an OSGi environment such
     * as a test set-up.
     */
    private Set<ReviewedSpotsCollection> reviewedSpotsCollections;
    private final ServiceTracker<WindFinderReviewedSpotsCollectionIdProvider, WindFinderReviewedSpotsCollectionIdProvider> reviewedSpotsCollectionIdProvider;

    public WindFinderTrackerFactory() {
        this.windTrackerPerRace = new HashMap<>();
        this.reviewedSpotsCollections = Collections.synchronizedSet(new HashSet<>());
        if (Activator.getContext() != null) {
            reviewedSpotsCollectionIdProvider = ServiceTrackerFactory.createAndOpen(Activator.getContext(), WindFinderReviewedSpotsCollectionIdProvider.class);
        } else {
            reviewedSpotsCollectionIdProvider = null;
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

    /**
     * Obtains the reviewed spot collections that this factory knows about. This set is constructed from the collections
     * provided explicitly using {@link #addReviewedSpotCollection(ReviewedSpotCollection)} and
     * {@link #removeReviewedSpotCollection(ReviewedSpotsCollection)}, and is extended by the collections obtained
     * through {@link #reviewedSpotsCollectionIdProvider} if a corresponding service can be resolved.
     * 
     * @return a non-live set of spots collections known by this factory at this point in time
     */
    Iterable<ReviewedSpotsCollection> getReviewedSpotsCollections() {
        final List<ReviewedSpotsCollection> result = new ArrayList<>();
        result.addAll(reviewedSpotsCollections);
        final WindFinderReviewedSpotsCollectionIdProvider provider;
        if (reviewedSpotsCollectionIdProvider != null && (provider = reviewedSpotsCollectionIdProvider.getService()) != null) {
            Util.addAll(Util.map(provider.getWindFinderReviewedSpotsCollectionIds(), id->new ReviewedSpotsCollectionImpl(id)), result);
        }
        return result;
    }
    
    public void addReviewedSpotCollection(ReviewedSpotsCollection collection) {
        reviewedSpotsCollections.add(collection);
    }

    public void removeReviewedSpotCollection(ReviewedSpotsCollection collection) {
        reviewedSpotsCollections.remove(collection);
    }
}
