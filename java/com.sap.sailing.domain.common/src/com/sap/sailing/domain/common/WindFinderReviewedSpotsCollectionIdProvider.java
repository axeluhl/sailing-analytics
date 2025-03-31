package com.sap.sailing.domain.common;

/**
 * Something that can provide IDs of WindFinder (https://www.windfinder.com) spot collections
 * that have been reviewed by WindFinder and have been released for use. Typically, an event
 * would specify zero or more such {@link String} IDs, the {@code RacingEventService} would
 * expose the result of collecting those over all events through this interface, and the
 * WindFinder adapter that is responsible for receiving wind data through the WindFinder API
 * would request them through this interface that it expects to discover through the OSGi
 * registry.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface WindFinderReviewedSpotsCollectionIdProvider {
    Iterable<String> getAllWindFinderReviewedSpotsCollectionIds();

    /**
     * If WindFinder spot collections are provided scoped to events, regattas, or races, this method
     * allows for a qualified search for WindFinder spot collections, using a regatta identifier as a
     * qualifier. For example, if events specify WindFinder spot collection IDs, the regatta identifier
     * can be traced to the events to which this regatta belongs, and then only those events' WindFinder
     * spot collection IDs will be considered.
     */
    Iterable<String> getWindFinderReviewedSpotsCollectionIdsByRegatta(RegattaIdentifier regattaIdentifier);
}
