package com.sap.sailing.domain.windfinderadapter.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.WindTracker;
import com.sap.sailing.domain.tracking.WindTrackerFactory;
import com.sap.sailing.domain.windfinderadapter.ReviewedSpotsCollection;

public class WindFinderTrackerFactory implements WindTrackerFactory {
    private final Map<RaceDefinition, WindTracker> windTrackerPerRace;
    private final Set<ReviewedSpotsCollection> reviewedSpotsCollections;
    
    public WindFinderTrackerFactory() {
        this.windTrackerPerRace = new HashMap<>();
        this.reviewedSpotsCollections = Collections.synchronizedSet(new HashSet<>());
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
     * Obtains the reviewed spot collections that this factory knows about. This set is
     * configurable using {@link #addReviewedSpotCollection(ReviewedSpotCollection)} and
     * {@link #removeReviewedSpotCollection(ReviewedSpotsCollection)}.
     * 
     * @return an unmodifiable set of spots collections known by this factory
     */
    public Iterable<ReviewedSpotsCollection> getReviewedSpotsCollections() {
        return Collections.unmodifiableSet(reviewedSpotsCollections);
    }
    
    public void addReviewedSpotCollection(ReviewedSpotsCollection collection) {
        reviewedSpotsCollections.add(collection);
    }

    public void removeReviewedSpotCollection(ReviewedSpotsCollection collection) {
        reviewedSpotsCollections.remove(collection);
    }
}
