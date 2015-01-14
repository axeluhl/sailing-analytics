package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorStore;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Placemark;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.PlacemarkDTO;
import com.sap.sailing.domain.common.dto.PlacemarkOrderDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RaceStatusDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceStatisticsDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10Or8AndLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets1LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsEight;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsEightAndInterpolation;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsFive;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsSix;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.LowPointWinnerGetsZero;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sailing.geocoding.ReverseGeocoder;
import com.sap.sse.common.ObjectInputStreamResolvingAgainstCache;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class DomainFactoryImpl extends SharedDomainFactoryImpl implements DomainFactory {
    private static Logger logger = Logger.getLogger(DomainFactoryImpl.class.getName());
    
    /**
     * Uses a transient competitor store
     */
    public DomainFactoryImpl() {
        super(new TransientCompetitorStoreImpl());
    }
    
    public DomainFactoryImpl(CompetitorStore competitorStore) {
        super(competitorStore);
    }

    @Override
    public MarkPassing createMarkPassing(TimePoint timePoint, Waypoint waypoint, Competitor competitor) {
        return new MarkPassingImpl(timePoint, waypoint, competitor);
    }

    @Override
    public ObjectInputStreamResolvingAgainstCache<DomainFactory> createObjectInputStreamResolvingAgainstThisFactory(InputStream inputStream) throws IOException {
        return new ObjectInputStreamResolvingAgainstDomainFactoryImpl(inputStream, this);
    }

    @Override
    public ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType) {
        switch (scoringSchemeType) {
        case LOW_POINT:
            return new LowPoint();
        case HIGH_POINT:
            return new HighPoint();
        case HIGH_POINT_ESS_OVERALL:
            return new HighPointExtremeSailingSeriesOverall();
        case HIGH_POINT_LAST_BREAKS_TIE:
            return new HighPointLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_ONE:
            return new HighPointFirstGets1LastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TEN:
            return new HighPointFirstGets10LastBreaksTie();
        case LOW_POINT_WINNER_GETS_ZERO:
            return new LowPointWinnerGetsZero();
        case HIGH_POINT_WINNER_GETS_FIVE:
            return new HighPointWinnerGetsFive();
        case HIGH_POINT_WINNER_GETS_SIX:
            return new HighPointWinnerGetsSix();
        case HIGH_POINT_WINNER_GETS_EIGHT:
            return new HighPointWinnerGetsEight();
        case HIGH_POINT_WINNER_GETS_EIGHT_AND_INTERPOLATION:
            return new HighPointWinnerGetsEightAndInterpolation();
        case HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT:
            return new HighPointFirstGets10Or8AndLastBreaksTie();
        }
        throw new RuntimeException("Unknown scoring scheme type "+scoringSchemeType.name());
    }

    @Override
    public CompetitorDTO convertToCompetitorDTO(Competitor c) {
        return competitorStore.convertToCompetitorDTO(c);
    }

    @Override
    public FleetDTO convertToFleetDTO(Fleet fleet) {
        return new FleetDTO(fleet.getName(), fleet.getOrdering(), fleet.getColor());
    }

    @Override
    public RaceDTO createRaceDTO(TrackedRegattaRegistry trackedRegattaRegistry, boolean withGeoLocationData, RegattaAndRaceIdentifier raceIdentifier, TrackedRace trackedRace) {
        assert trackedRace != null;
        // Optional: Getting the places of the race
        PlacemarkOrderDTO racePlaces = withGeoLocationData ? getRacePlaces(trackedRace) : null;
        TrackedRaceDTO trackedRaceDTO = createTrackedRaceDTO(trackedRace); 
        RaceDTO raceDTO = new RaceDTO(raceIdentifier, trackedRaceDTO, trackedRegattaRegistry.isRaceBeingTracked(
                trackedRace.getTrackedRegatta().getRegatta(), trackedRace.getRace()));
        raceDTO.places = racePlaces;
        updateRaceDTOWithTrackedRaceData(trackedRace, raceDTO);
        return raceDTO;
    }

    @Override
    public void updateRaceDTOWithTrackedRaceData(TrackedRace trackedRace, RaceDTO raceDTO) {
        assert trackedRace != null;
        raceDTO.startOfRace = trackedRace.getStartOfRace() == null ? null : trackedRace.getStartOfRace().asDate();
        raceDTO.endOfRace = trackedRace.getEndOfRace() == null ? null : trackedRace.getEndOfRace().asDate();
        raceDTO.status = new RaceStatusDTO();
        raceDTO.status.status = trackedRace.getStatus() == null ? null : trackedRace.getStatus().getStatus();
        raceDTO.status.loadingProgress = trackedRace.getStatus() == null ? 0.0 : trackedRace.getStatus().getLoadingProgress();
    }

    @Override
    public TrackedRaceDTO createTrackedRaceDTO(TrackedRace trackedRace) {
        TrackedRaceDTO trackedRaceDTO = new TrackedRaceDTO();
        trackedRaceDTO.startOfTracking = trackedRace.getStartOfTracking() == null ? null : trackedRace.getStartOfTracking().asDate();
        trackedRaceDTO.endOfTracking = trackedRace.getEndOfTracking() == null ? null : trackedRace.getEndOfTracking().asDate();
        trackedRaceDTO.timePointOfNewestEvent = trackedRace.getTimePointOfNewestEvent() == null ? null : trackedRace.getTimePointOfNewestEvent().asDate();
        trackedRaceDTO.hasWindData = trackedRace.hasWindData();
        trackedRaceDTO.hasGPSData = trackedRace.hasGPSData();
        trackedRaceDTO.delayToLiveInMs = trackedRace.getDelayToLiveInMillis();
        return trackedRaceDTO;
    }

    @Override
    public TrackedRaceStatisticsDTO createTrackedRaceStatisticsDTO(TrackedRace trackedRace, Leaderboard leaderboard,
            RaceColumn raceColumn, Fleet fleet, Collection<MediaTrack> mediaTracks) {
        TrackedRaceStatisticsDTO statisticsDTO = new TrackedRaceStatisticsDTO();
        
        // GPS data
        statisticsDTO.hasGPSData = trackedRace.hasGPSData();

        Competitor leaderOrWinner = null;
        TimePoint now = MillisecondsTimePoint.now();
        try {
            if(trackedRace.isLive(now)) {
                leaderOrWinner = trackedRace.getOverallLeader(now);
            } else if (trackedRace.getEndOfRace() != null) {
                for(Competitor competitor: leaderboard.getCompetitorsFromBestToWorst(raceColumn, now)) {
                    Fleet fleetOfCompetitor = raceColumn.getFleetOfCompetitor(competitor);
                    if(fleetOfCompetitor != null && fleetOfCompetitor.equals(fleet)) {
                        leaderOrWinner = competitor;
                        break;
                    }
                }
            }                
            if(leaderOrWinner != null) {
                statisticsDTO.hasLeaderOrWinnerData = true;
                statisticsDTO.leaderOrWinner = convertToCompetitorDTO(leaderOrWinner);
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(leaderOrWinner);
                if(track != null) {
                    statisticsDTO.averageGPSDataSampleInterval = track.getAverageIntervalBetweenFixes();
                }
            }
        } catch (NoWindException e) {
        }
        
        // Measured wind sources data
        statisticsDTO.measuredWindSourcesCount = Util.size(trackedRace.getWindSources(WindSourceType.EXPEDITION));
        statisticsDTO.hasMeasuredWindData = statisticsDTO.measuredWindSourcesCount > 0; 

        // leg progress data
        RaceDefinition race = trackedRace.getRace();
        if(race.getCourse() != null) {
            statisticsDTO.hasLegProgressData = true;
            statisticsDTO.totalLegsCount = race.getCourse().getLegs().size();
            TrackedLeg currentLeg = trackedRace.getCurrentLeg(MillisecondsTimePoint.now());
            if(currentLeg != null) {
                statisticsDTO.currentLegNo = race.getCourse().getIndexOfWaypoint(currentLeg.getLeg().getFrom());
            } else {
                statisticsDTO.currentLegNo = 0;
            }
        }
        
        // media data
        if(mediaTracks != null) {
            for(MediaTrack track: mediaTracks) {
                switch(track.mimeType.mediaType) {
                case audio:
                    statisticsDTO.hasAudioData = true;
                    statisticsDTO.audioTracksCount = statisticsDTO.audioTracksCount == null ? 1 : statisticsDTO.audioTracksCount++;   
                    break;
                case video:
                    statisticsDTO.hasVideoData = true;
                    statisticsDTO.videoTracksCount = statisticsDTO.videoTracksCount == null ? 1 : statisticsDTO.videoTracksCount++;   
                    break;
                }
            }
        }

        return statisticsDTO;
    }

    private PlacemarkOrderDTO getRacePlaces(TrackedRace trackedRace) {
        Util.Pair<Placemark, Placemark> startAndFinish = getStartFinishPlacemarksForTrackedRace(trackedRace);
        PlacemarkOrderDTO racePlaces = new PlacemarkOrderDTO();
        if (startAndFinish.getA() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getA()));
        }
        if (startAndFinish.getB() != null) {
            racePlaces.getPlacemarks().add(convertToPlacemarkDTO(startAndFinish.getB()));
        }
        if (racePlaces.isEmpty()) {
            racePlaces = null;
        }
        return racePlaces;
    }

    private Util.Pair<Placemark, Placemark> getStartFinishPlacemarksForTrackedRace(TrackedRace race) {
        double radiusCalculationFactor = 10.0;
        Placemark startBest = null;
        Placemark finishBest = null;

        // Get start postition
        Iterator<Mark> startMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix startMarkFix = startMarks.hasNext() ? race.getOrCreateTrack(startMarks.next()).getLastRawFix() : null;
        Position startPosition = startMarkFix != null ? startMarkFix.getPosition() : null;
        if (startPosition != null) {
            try {
                // Get distance to nearest placemark and calculate the search radius
                Placemark startNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(startPosition);
                if (startNearest != null) {
                    Distance startNearestDistance = startNearest.distanceFrom(startPosition);
                    double startRadius = startNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best start place
                    startBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(startPosition, startRadius,
                            new Placemark.ByPopulationDistanceRatio(startPosition));
                }
            } catch (IOException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            } catch (org.json.simple.parser.ParseException e) {
                logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
            }
        }

        // Get finish position
        Iterator<Mark> finishMarks = race.getRace().getCourse().getFirstWaypoint().getMarks().iterator();
        GPSFix finishMarkFix = finishMarks.hasNext() ? race.getOrCreateTrack(finishMarks.next()).getLastRawFix() : null;
        Position finishPosition = finishMarkFix != null ? finishMarkFix.getPosition() : null;
        if (startPosition != null && finishPosition != null) {
            if (startPosition.getDistance(finishPosition).getKilometers() <= ReverseGeocoder.POSITION_CACHE_DISTANCE_LIMIT_IN_KM) {
                finishBest = startBest;
            } else {
                try {
                    // Get distance to nearest placemark and calculate the search radius
                    Placemark finishNearest = ReverseGeocoder.INSTANCE.getPlacemarkNearest(finishPosition);
                    Distance finishNearestDistance = finishNearest.distanceFrom(finishPosition);
                    double finishRadius = finishNearestDistance.getKilometers() * radiusCalculationFactor;

                    // Get the estimated best finish place
                    finishBest = ReverseGeocoder.INSTANCE.getPlacemarkLast(finishPosition, finishRadius,
                            new Placemark.ByPopulationDistanceRatio(finishPosition));
                } catch (IOException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                } catch (org.json.simple.parser.ParseException e) {
                    logger.throwing(TrackedRaceImpl.class.getName(), "getPlaceOrder()", e);
                }
            }
        }
        Util.Pair<Placemark, Placemark> placemarks = new Util.Pair<Placemark, Placemark>(startBest, finishBest);
        return placemarks;
    }

    @Override
    public PlacemarkDTO convertToPlacemarkDTO(Placemark placemark) {
        Position position = placemark.getPosition();
        return new PlacemarkDTO(placemark.getName(), placemark.getCountryCode(), new PositionDTO(position.getLatDeg(),
                position.getLngDeg()), placemark.getPopulation());
    }

    @Override
    public List<CompetitorDTO> getCompetitorDTOList(List<Competitor> competitors) {
        List<CompetitorDTO> result = new ArrayList<CompetitorDTO>();
        for (Competitor competitor : competitors) {
            result.add(convertToCompetitorDTO(competitor));
        }
        return result;
    }

}
