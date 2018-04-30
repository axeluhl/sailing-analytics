package com.sap.sailing.gwt.common.client;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.domain.windfinder.Spot;
import com.sap.sailing.domain.windfinder.WindFinderTrackerFactory;

public class EventWindFinderUtil {
    private static final Logger logger = Logger.getLogger(EventWindFinderUtil.class.getName());
    
    /**
     * Obtains wind finder spots for a given Event
     * 
     * @param useCachedSpotsForTrackedRaces
     *            whether or not to use the cached set of wind finder spots for tracked races
     */
    public Iterable<SpotDTO> getWindFinderSpotsToConsider(Event event, WindFinderTrackerFactory windFinderTrackerFactory, boolean useCachedSpotsForTrackedRaces) {
        final Set<SpotDTO> windFinderSpots = new LinkedHashSet<>();
        for (final String spotsCollectionId : event.getWindFinderReviewedSpotsCollectionIds()) {
            try {
                for (final Spot spot : windFinderTrackerFactory.getReviewedSpotsCollectionById(spotsCollectionId, /* lookupInCache */ true).
                        getSpots(/* cached */ true)) {
                    windFinderSpots.add(new SpotDTO(spot));
                }
            } catch (IOException | ParseException | InterruptedException | ExecutionException e) {
                logger.warning("Unable to determine WindFinder spots for reviewed spot collection with ID "+spotsCollectionId);
            }
        }
        for (final String spotIdFromTrackedRace : event.getAllFinderSpotIdsUsedByTrackedRacesInEvent()) {
            try {
                Spot spotById = windFinderTrackerFactory.getSpotById(spotIdFromTrackedRace, /* cached */ true);
                if (spotById != null) {
                    windFinderSpots.add(new SpotDTO(spotById));
                } else {
                    logger.warning("Couldn't find WindFinder spot with ID "+spotIdFromTrackedRace);
                }
            } catch (IOException | ParseException | InterruptedException | ExecutionException e) {
                logger.warning("Unable to determine WindFinder spot with ID "+spotIdFromTrackedRace);
            }
        }
        return windFinderSpots;
    }
}
