package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.PlacemarkDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceStatisticsDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.ObjectInputStreamResolvingAgainstCache;

public interface DomainFactory extends SharedDomainFactory {
    /**
     * A default domain factory for test purposes only. In a server environment, ensure NOT to use this. Use
     * the <code>RacingEventService.getBaseDomainFactory()</code> instead which should be the single instance used
     * by all other services linked to the <code>RacingEventService</code>.
     */
    static DomainFactory INSTANCE = new DomainFactoryImpl((srlid)->null);

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);

    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory. The
     * stream returned by this method can be used to have objects implementing the {@link IsManagedByCache} interface
     * {@link IsManagedByCache#resolve(Object) resolve} against this domain factory as their cache.
     * <p>
     * 
     * <b>Note:</b> In order to allow the deserialization process to find all the classes required, it is a good idea to
     * {@link Thread#setContextClassLoader(ClassLoader) set the context class loader} on the
     * {@link Thread#currentThread() current thread} to one that can see and resolve all the classes that have instances
     * in the stream. Example:<pre>
     *          ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
     *          Thread.currentThread().setContextClassLoader(TopLevelMasterData.class.getClassLoader());
     *          Object o = domainFactory.createObjectInputStreamResolvingAgainstThisFactory(inputStream).readObject();
     *          Thread.currentThread().setContextClassLoader(oldContextClassLoader);
     * </pre>
     */
    ObjectInputStreamResolvingAgainstCache<DomainFactory> createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;
    
    ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType);

    CompetitorDTO convertToCompetitorDTO(CompetitorWithBoat c);

    /** Temporary functions -> REMOVE later on */
    CompetitorDTO convertToCompetitorDTO(Competitor c);

    CompetitorDTO convertToCompetitorDTO(Competitor c, Boat b);

    Map<CompetitorDTO, BoatDTO> convertToCompetitorAndBoatDTOs(Map<Competitor, Boat> competitorsAndBoats);

    BoatDTO convertToBoatDTO(Boat boat);

    FleetDTO convertToFleetDTO(Fleet fleet);

    /**
     * @param trackedRace must not be <code>null</code>
     */
    RaceDTO createRaceDTO(TrackedRegattaRegistry trackedRegattaRegistry, boolean withGeoLocationData, RegattaAndRaceIdentifier raceIdentifier, TrackedRace trackedRace);

    PlacemarkDTO convertToPlacemarkDTO(Placemark placemark);

    List<CompetitorDTO> getCompetitorDTOList(Map<Competitor, Boat> competitors);

    List<CompetitorDTO> getCompetitorDTOList(Iterable<Competitor> competitors);

    List<CompetitorDTO> getCompetitorDTOList(List<Pair<Competitor, Boat>> competitors);
    
    TrackedRaceDTO createTrackedRaceDTO(TrackedRace trackedRace);

    TrackedRaceStatisticsDTO createTrackedRaceStatisticsDTO(TrackedRace trackedRace, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, Iterable<MediaTrack> mediatracks);

    /**
     * @param trackedRace must not be <code>null</code>
     */
    void updateRaceDTOWithTrackedRaceData(TrackedRace trackedRace, RaceDTO raceDTO);

}
