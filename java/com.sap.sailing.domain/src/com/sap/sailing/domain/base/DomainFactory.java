package com.sap.sailing.domain.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.base.impl.DomainFactoryImpl;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
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
import com.sap.sse.common.TimePoint;
import com.sap.sse.replication.impl.ObjectInputStreamResolvingAgainstCache;

public interface DomainFactory extends SharedDomainFactory {
    /**
     * A default domain factory for test purposes only. In a server environment, ensure NOT to use this. Use
     * the <code>RacingEventService.getBaseDomainFactory()</code> instead which should be the single instance used
     * by all other services linked to the <code>RacingEventService</code>.
     */
    static DomainFactory INSTANCE = new DomainFactoryImpl();

    MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor);
    
    /**
     * When de-serializing objects of types whose instances that are managed and cached by this domain factory,
     * de-serialized instances need to be replaced by / resolved to the counterparts already known by this factory.
     * The stream returned by this method can be used 
     */
    ObjectInputStreamResolvingAgainstCache<DomainFactory> createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException;
    
    ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType);

    CompetitorDTO convertToCompetitorDTO(Competitor c);

    FleetDTO convertToFleetDTO(Fleet fleet);

    /**
     * @param trackedRace must not be <code>null</code>
     */
    RaceDTO createRaceDTO(TrackedRegattaRegistry trackedRegattaRegistry, boolean withGeoLocationData, RegattaAndRaceIdentifier raceIdentifier, TrackedRace trackedRace);

    PlacemarkDTO convertToPlacemarkDTO(Placemark placemark);

    List<CompetitorDTO> getCompetitorDTOList(List<Competitor> competitors);

    TrackedRaceDTO createTrackedRaceDTO(TrackedRace trackedRace);

    TrackedRaceStatisticsDTO createTrackedRaceStatisticsDTO(TrackedRace trackedRace, Leaderboard leaderboard, RaceColumn raceColumn,
            Fleet fleet, Collection<MediaTrack> mediatracks);

    /**
     * @param trackedRace must not be <code>null</code>
     */
    void updateRaceDTOWithTrackedRaceData(TrackedRace trackedRace, RaceDTO raceDTO);

}
