package com.sap.sailing.domain.tracking;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sse.common.TypeBasedServiceFinder;

/**
 * Handler for mapping objects of classes that implement the {@link RaceTrackingConnectivityParameters} interface
 * to / from maps using {@link String}s as keys, such as {@link JSONObject} or {@link BasicDBObject}. This can be
 * used to save and restore the races currently being tracked by a server, based on the tracking connectivity
 * data provided when last tracked.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RaceTrackingConnectivityParametersHandler {
    /**
     * Produces a map that has the value for the {@link TypeBasedServiceFinder#TYPE} key set such that this handler
     * can be {@link TypeBasedServiceFinder#findService(String) found} based on that value again; furthermore, all
     * other key/value pairs in the map returned allow the {@link #mapTo(Map)} method of this handler to restore
     * a {@link RaceTrackingConnectivityParameters} object that is equivalent to {@code params}.<p>
     * 
     * The types of the value objects must be restricted to thos types that MongoDB can handle as field values,
     * in particular {@link String}, {@link Number} and {@link UUID}. 
     */
    Map<String, Object> mapFrom(RaceTrackingConnectivityParameters params) throws MalformedURLException;
    
    /**
     * Produces a {@link RaceTrackingConnectivityParameters} object from the contents of {@code map}, ignoring the
     * {@link TypeBasedServiceFinder#TYPE} key which has enabled {@link TypeBasedServiceFinder#findService(String)
     * finding} this service instance in the first place. The object returned is equivalent to one that has
     * produced {@code map} by passing it to this service's {@link #mapFrom(RaceTrackingConnectivityParameters)}
     * method.
     * 
     * @return {@code null} in case the object cannot be produced, e.g., because the leaderboard to which a
     * race log-tracked race belonged has been deleted in the meantime.
     */
    RaceTrackingConnectivityParameters mapTo(Map<String, Object> map) throws Exception;

    /**
     * @return a map that contains the {@link TypeBasedServiceFinder#TYPE} identifier as taken from
     *         {@link RaceTrackingConnectivityParameters#getTypeIdentifier()} as well as a non-empty set of further
     *         entries that uniquely identify the {@code params} object in the database, in particular for removing.
     */
    Map<String, Object> getKey(RaceTrackingConnectivityParameters params) throws MalformedURLException;
}
