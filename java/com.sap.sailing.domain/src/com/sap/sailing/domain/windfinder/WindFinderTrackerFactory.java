package com.sap.sailing.domain.windfinder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.tracking.WindTrackerFactory;

public interface WindFinderTrackerFactory extends WindTrackerFactory {

    /**
     * Obtains the reviewed spot collections that this factory knows about. This set is constructed from the collections
     * provided explicitly using {@link #addReviewedSpotCollection(ReviewedSpotCollection)} and
     * {@link #removeReviewedSpotCollection(ReviewedSpotsCollection)}, and is extended by the collections obtained
     * through {@link #reviewedSpotsCollectionIdProvider} if a corresponding service can be resolved.
     * 
     * @param cached
     *            if {@code true}, only those spots collections are returned that this factory has previously obtained;
     *            this is useful for a very fast lookup but does not guarantee that all spots collections added recently
     *            will be considered
     * 
     * @return a non-live set of spots collections known by this factory at this point in time
     */
    Iterable<ReviewedSpotsCollection> getReviewedSpotsCollections(boolean cached) throws InterruptedException, ExecutionException;

    ReviewedSpotsCollection getReviewedSpotsCollectionById(String spotsCollectionId, boolean lookupInCache) throws InterruptedException, ExecutionException;

    /**
     * @param cached
     *            if {@code true}, only those spots collections are returned that this factory has previously obtained;
     *            this is useful for a very fast lookup but does not guarantee that all spots collections added recently
     *            will be considered
     */
    Spot getSpotById(String spotId, boolean cached) throws MalformedURLException, IOException, ParseException, InterruptedException, ExecutionException;

}
