package com.sap.sailing.dashboards.gwt.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisCompetitorDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisRankingTableEntryDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.WindAndAdvantagesInfoForStartLineDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.server.RacingEventService;

/**
 * The task of StartAnalyisisRacesStore is to cache StartAnalysisDTOs for Competitors at one day. At initialization it
 * generates StartAnalysisDTOs for Competitors from races started before initialization time and generates
 * StartAnalysisDTOs for Competitors from races that are starting when the server is running. It achieves this basically
 * by registering as RaceChangeListener and receiving so markRoundings for competitors.
 */
public class StartAnalyisisRacesStore {

    private RacingEventService racingEventService;
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;

    /**
     * {@link StartAnalyisisRacesStoreListener} get notified at after initialization and after the change of
     * {@link #startAnalysisDTOListForCompetitorName}
     * */
    private List<StartAnalyisisRacesStoreListener> listeners;

    /**
     * Caches a list of chronologically sorted {@link StartAnalysisDTO} for names from Competitors. After any change of
     * this field {@link #notifyListernersAboutStartAnalysisDTOsChanges()} get called.
     * */
    private Map<String, List<StartAnalysisDTO>> startAnalysisDTOListForCompetitorName;

    /**
     * Contains {@link StartAnalysisCompetitorDTO}s for every competitor name in every race. This map gets created for a
     * tracked race when the first mark one rounding of a tracked race gets received. Caching
     * {@link StartAnalysisCompetitorDTO}s brings the advantage of faster code at creating finished
     * {@link #startAnalysisDTOListForCompetitorName}
     * */
    private Map<TrackedRace, Map<String, StartAnalysisCompetitorDTO>> startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace;

    /**
     * Contains a ranking list of competitor names at mark one for every race
     * */
    private Map<TrackedRace, List<String>> rankingListAtMarkOneForTrackedRace;

    /**
     * Like {@link startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace} gpsFixesForCompetitorNameForTracedRace
     * contains the list of gps fixes for a competitor name at from 6 seconds at the start. Caching
     * {@link gpsFixesForCompetitorNameForTracedRace}s brings the advantage of faster code at creating finished
     * {@link #startAnalysisDTOListForCompetitorName}.
     * */
    private Map<TrackedRace, Map<String, List<GPSFixDTO>>> gpsFixesForCompetitorNameForTracedRace;

    /**
     * Contains {@link StartAnalysisDTO}s that contain just static data for a start analysis.Caching
     * {@link startAnalysisDTOWithStaticData}s brings the advantage of faster code at creating finished
     * {@link #startAnalysisDTOListForCompetitorName}.
     * */
    private Map<TrackedRace, StartAnalysisDTO> startAnalysisDTOWithStaticData;

    /**
     * Calls {@link #generateStartAnalysisDTOsForCompetitorNamesFromRacesStartedBeforeClassInitialization()} to create
     * startanalysisDTO for races that started before class initialization.
     */
    public StartAnalyisisRacesStore(RacingEventService racingEventService) {

        this.racingEventService = racingEventService;
        baseDomainFactory = racingEventService.getBaseDomainFactory();
        this.listeners = new ArrayList<StartAnalyisisRacesStoreListener>();

        startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace = new HashMap<TrackedRace, Map<String, StartAnalysisCompetitorDTO>>();
        startAnalysisDTOListForCompetitorName = new HashMap<String, List<StartAnalysisDTO>>();
        rankingListAtMarkOneForTrackedRace = new HashMap<TrackedRace, List<String>>();
        gpsFixesForCompetitorNameForTracedRace = new HashMap<TrackedRace, Map<String, List<GPSFixDTO>>>();
        startAnalysisDTOWithStaticData = new HashMap<TrackedRace, StartAnalysisDTO>();

        generateStartAnalysisDTOsForCompetitorNamesFromRacesStartedBeforeClassInitialization();
    }

    /**
     * Searches for cached TrackedRaces and generates StartAnalysisDTO for each Competitor in each TrackedRace. Saves
     * the StartAnalysisDTOs in {@link #startAnalysisDTOListForCompetitorName} and notifies
     * {@link StartAnalyisisRacesStoreListener}
     * */
    private void generateStartAnalysisDTOsForCompetitorNamesFromRacesStartedBeforeClassInitialization() {
    }

    /**
     * Returns the Index of the Mark in the Race. Is used in
     * {@link #markPassingReceived(Competitor, Map, Iterable, TrackedRace)} to identify just markpassings at mark one,
     * because the position of a boat at mark one defines the performance/startingrank of the race
     */
    private int getIndexOfMarkPassingMark(Iterable<MarkPassing> markPassings, TrackedRace trackedRace) {
        List<MarkPassing> markPassingsList = listFromIterator(markPassings.iterator());
        return trackedRace.getRace().getCourse()
                .getIndexOfWaypoint(markPassingsList.get(markPassingsList.size() - 1).getWaypoint());
    }

    private List<MarkPassing> listFromIterator(Iterator<MarkPassing> iterator) {

        List<MarkPassing> result = new ArrayList<MarkPassing>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    private StartAnalysisDTO createStartAnalysisDTOWithOnlyStaticData(TrackedRace trackedRace) {
        StartAnalysisDTO startAnalysisDTO = new StartAnalysisDTO();
        startAnalysisDTO.raceName = trackedRace.getRace().getName();
        startAnalysisDTO.startLineMarkPositions = getStartLineBoyPositions(trackedRace);
        startAnalysisDTO.startAnalysisWindLineInfoDTO = createStartAnalysisWindAndLineData(trackedRace);
        startAnalysisDTO.startLineMarks = getStartLineMarks(trackedRace);
        return startAnalysisDTO;
    }

    private Map<String, Date> createPerCompetitorIdAsString(TrackedRace currentRace, Date fiveSecondsBeforeStart) {
        Map<String, Date> fromPerCompetitorIdAsString = new HashMap<String, Date>();
        try {
            for (Competitor competitor : currentRace.getCompetitorsFromBestToWorst(new MillisecondsTimePoint(new Date()
                    .getTime()))) {
                fromPerCompetitorIdAsString.put(competitor.getId().toString(), fiveSecondsBeforeStart);
            }
        } catch (NoWindException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return fromPerCompetitorIdAsString;
    }

    private StartAnalysisRankingTableEntryDTO createRankTableEntry(TrackedRace trackedRace, int rank, Competitor competitor) {
        StartAnalysisRankingTableEntryDTO startAnalysisRankTableEntryDTO = createStartAnalysisRankTableEntryDTOWithRankAndStartTimepoint(
                competitor, rank, trackedRace);
        return startAnalysisRankTableEntryDTO;
    }

    private StartAnalysisRankingTableEntryDTO createStartAnalysisRankTableEntryDTOWithRankAndStartTimepoint(
            Competitor competitor, int rank, TrackedRace trackedRace) {

        StartAnalysisRankingTableEntryDTO tableentry = new StartAnalysisRankingTableEntryDTO();

        tableentry.rankAtFirstMark = rank;
        tableentry.teamName = competitor.getName();
        tableentry.speedAtStartTime = trackedRace.getSpeed(competitor, 1).getKnots();
        tableentry.distanceToLineAtStartTime = trackedRace.getDistanceToStartLine(competitor, 1).getMeters();
        tableentry.tailColor = competitor.getColor().getAsHtml();
        return tableentry;
    }

    private List<PositionDTO> getStartLineBoyPositions(TrackedRace trackedRace) {

        return getMarkPositionDTOs(trackedRace.getStartTimeReceived(), trackedRace, trackedRace.getRace().getCourse()
                .getFirstWaypoint());
    }

    private List<PositionDTO> getMarkPositionDTOs(TimePoint timePoint, TrackedRace trackedRace, Waypoint waypoint) {

        List<PositionDTO> markPositionDTOs = new ArrayList<PositionDTO>();
        for (Mark startMark : waypoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark).getEstimatedPosition(
                    timePoint, /* extrapolate */false);
            if (estimatedMarkPosition != null) {
                markPositionDTOs.add(new PositionDTO(estimatedMarkPosition.getLatDeg(), estimatedMarkPosition
                        .getLngDeg()));
            }
        }
        return markPositionDTOs;
    }

    private List<MarkDTO> getStartLineMarks(TrackedRace trackedRace) {

        Waypoint startWayPoint = trackedRace.getStartLine(trackedRace.getStartOfRace()).getWaypoint();
        List<MarkDTO> startLineMarks = new ArrayList<MarkDTO>();
        for (Mark startMark : startWayPoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark).getEstimatedPosition(
                    trackedRace.getStartOfRace(), /* extrapolate */false);
            if (estimatedMarkPosition != null) {
                startLineMarks.add(convertToMarkDTO(startMark, estimatedMarkPosition));
            }
        }
        return startLineMarks;
    }

    private MarkDTO convertToMarkDTO(Mark mark, Position position) {
        MarkDTO markDTO;
        if (position != null) {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName(), position.getLatDeg(), position.getLngDeg());
        } else {
            markDTO = new MarkDTO(mark.getId().toString(), mark.getName());
        }
        markDTO.color = mark.getColor();
        markDTO.shape = mark.getShape();
        markDTO.pattern = mark.getPattern();
        markDTO.type = mark.getType();

        return markDTO;
    }

    public void addStartAnalyisisRacesStoreListener(StartAnalyisisRacesStoreListener startAnalyisisRacesStoreListener) {
        listeners.add(startAnalyisisRacesStoreListener);
    }

    private WindAndAdvantagesInfoForStartLineDTO createStartAnalysisWindAndLineData(TrackedRace trackedRace) {

        WindAndAdvantagesInfoForStartLineDTO startAnalysisWindLineInfoDTO = new WindAndAdvantagesInfoForStartLineDTO();

        LineDetails startline = trackedRace.getStartLine(trackedRace.getStartOfRace());
        startAnalysisWindLineInfoDTO.startLineAdvantageByGeometry = startline.getGeometricAdvantage().getMeters();
        startAnalysisWindLineInfoDTO.startLineAdvantageAtPinEnd = startline.getAdvantage().getMeters();
        Position portMarkPosition = trackedRace.getOrCreateTrack(
                trackedRace.getStartLine(trackedRace.getStartOfRace()).getStarboardMarkWhileApproachingLine())
                .getEstimatedPosition(trackedRace.getStartOfRace(), /* extrapolate */false);
        Wind windAtStart = trackedRace.getWind(portMarkPosition, trackedRace.getStartOfRace());
        startAnalysisWindLineInfoDTO.windDirectionInDegrees = windAtStart.getBearing().getDegrees();
        startAnalysisWindLineInfoDTO.windSpeedInKnots = windAtStart.getKnots();

        return startAnalysisWindLineInfoDTO;
    }

    public void notifyListernersAboutStartAnalysisDTOsChanges() {
        for (StartAnalyisisRacesStoreListener startAnalyisisRacesStoreListener : listeners) {
            startAnalyisisRacesStoreListener.startAnalyisisRacesChanged(startAnalysisDTOListForCompetitorName);
            // displayStarts(startAnalysisDTOListForCompetitorName);
        }
    }

    public TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        return racingEventService.getExistingTrackedRace(regattaNameAndRaceName);
    }

    /**
     * returns a List of GPSFixDTOs for every competitor name in tracked race
     * */
    private Map<String, List<GPSFixDTO>> getGPSFixDTOListForCompetitorName(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        Map<String, List<GPSFixDTO>> result = new HashMap<String, List<GPSFixDTO>>();
        TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
        if (trackedRace != null) {
            for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
                if (fromPerCompetitorIdAsString.containsKey(competitor.getId().toString())) {
                    CompetitorDTO competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
                    List<GPSFixDTO> fixesForCompetitor = new ArrayList<GPSFixDTO>();
                    result.put(competitorDTO.getName(), fixesForCompetitor);
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    TimePoint fromTimePoint = new MillisecondsTimePoint(fromPerCompetitorIdAsString.get(competitorDTO
                            .getIdAsString()));
                    TimePoint toTimePoint = new MillisecondsTimePoint(toPerCompetitorIdAsString.get(competitorDTO
                            .getIdAsString()));
                    // copy the fixes into a list while holding the monitor; then release the monitor to avoid deadlocks
                    // during wind estimations required for tack determination
                    List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
                    track.lockForRead();
                    try {
                        Iterator<GPSFixMoving> fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */true);
                        while (fixIter.hasNext()) {
                            GPSFixMoving fix = fixIter.next();
                            if (fix.getTimePoint().after(fromTimePoint) && fix.getTimePoint().before(toTimePoint)) {
                                fixes.add(fix);
                            }
                        }
                    } finally {
                        track.unlockAfterRead();
                    }

                    Iterator<GPSFixMoving> fixIter = fixes.iterator();
                    while (fixIter.hasNext()) {
                        GPSFixMoving fix = fixIter.next();
                        Wind wind = trackedRace.getWind(fix.getPosition(), toTimePoint);
                        final SpeedWithBearing estimatedSpeed = track.getEstimatedSpeed(fix.getTimePoint());
                        Tack tack = wind == null ? null : trackedRace.getTack(estimatedSpeed, wind, fix.getTimePoint());
                        LegType legType = null;
                        WindDTO windDTO = wind == null ? null : createWindDTOFromAlreadyAveraged(wind, toTimePoint);
                        GPSFixDTO fixDTO = createGPSFixDTO(fix, estimatedSpeed, windDTO, tack, legType, /* extrapolate */
                                false);
                        fixesForCompetitor.add(fixDTO);
                    }
                }
            }
        }
        return result;
    }

    protected WindDTO createWindDTOFromAlreadyAveraged(Wind wind, TimePoint requestTimepoint) {
        WindDTO windDTO = new WindDTO();
        windDTO.requestTimepoint = requestTimepoint.asMillis();
        windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.trueWindSpeedInKnots = wind.getKnots();
        windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        windDTO.dampenedTrueWindBearingDeg = wind.getBearing().getDegrees();
        windDTO.dampenedTrueWindFromDeg = wind.getBearing().reverse().getDegrees();
        windDTO.dampenedTrueWindSpeedInKnots = wind.getKnots();
        windDTO.dampenedTrueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
        if (wind.getPosition() != null) {
            windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(), wind.getPosition().getLngDeg());
        }
        if (wind.getTimePoint() != null) {
            windDTO.measureTimepoint = wind.getTimePoint().asMillis();
        }
        return windDTO;
    }

    private SpeedWithBearingDTO createSpeedWithBearingDTO(SpeedWithBearing speedWithBearing) {
        return new SpeedWithBearingDTO(speedWithBearing.getKnots(), speedWithBearing.getBearing().getDegrees());
    }

    private GPSFixDTO createGPSFixDTO(GPSFix fix, SpeedWithBearing speedWithBearing, WindDTO windDTO, Tack tack,
            LegType legType, boolean extrapolated) {
        return new GPSFixDTO(fix.getTimePoint().asDate(), fix.getPosition() == null ? null : new PositionDTO(fix
                .getPosition().getLatDeg(), fix.getPosition().getLngDeg()), speedWithBearing == null ? null
                : createSpeedWithBearingDTO(speedWithBearing), windDTO, tack, legType, extrapolated);
    }

    private StartAnalysisCompetitorDTO createStartAnalysisCompetitorDTO(TrackedRace trackedRace, int rank,
            Competitor competitor) {
        StartAnalysisCompetitorDTO startAnalysisCompetitorDTOsForRace = new StartAnalysisCompetitorDTO();
        startAnalysisCompetitorDTOsForRace.competitorDTO = baseDomainFactory.convertToCompetitorDTO(competitor);
        startAnalysisCompetitorDTOsForRace.rankingTableEntryDTO = createRankTableEntry(trackedRace, rank, competitor);
        List<GPSFixDTO> boatPositions = getOrCreateGPSFixDTOsForCompetitorForTrackedRace(trackedRace).get(
                competitor.getName());
        startAnalysisCompetitorDTOsForRace.gpsFixDTOs = boatPositions;
        return startAnalysisCompetitorDTOsForRace;
    }

    private StartAnalysisDTO createStartAnalysisDTOForCompetitorName(StartAnalysisDTO startAnalysisDTO,
            String competitorName, TrackedRace trackedRace) {
        List<StartAnalysisCompetitorDTO> competitors = new ArrayList<StartAnalysisCompetitorDTO>();
        List<String> rankingList = getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace);
        Map<String, StartAnalysisCompetitorDTO> startAnalysisCompetitorDTOsMap = getOrCreateStartAnalysisCompetitorDTOsMapForTrackedRace(trackedRace);
        boolean isCompetitorBetweenFirstThree = false;
        for (int i = 0; i < 3; i++) {
            competitors.add(startAnalysisCompetitorDTOsMap.get(rankingList.get(i)));
            if (rankingList.get(i).equals(competitorName)) {
                isCompetitorBetweenFirstThree = true;
            }
        }
        if (!isCompetitorBetweenFirstThree) {
            competitors.add(startAnalysisCompetitorDTOsMap.get(competitorName));
        }
        startAnalysisDTO.startAnalysisCompetitorDTOs = competitors;
        return startAnalysisDTO;
    }

    private List<String> getOrCreateRankingListAtMarkOneForTrackedRace(TrackedRace trackedRace) {
        if (!rankingListAtMarkOneForTrackedRace.containsKey(trackedRace)) {
            List<String> rankingListAtMarkOneForRace = new ArrayList<String>();
            rankingListAtMarkOneForTrackedRace.put(trackedRace, rankingListAtMarkOneForRace);
        }
        return rankingListAtMarkOneForTrackedRace.get(trackedRace);
    }

    private Map<String, StartAnalysisCompetitorDTO> getOrCreateStartAnalysisCompetitorDTOsMapForTrackedRace(
            TrackedRace trackedRace) {
        Map<String, StartAnalysisCompetitorDTO> startAnalysisCompetitorDTOsMap;
        if (!startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace.containsKey(trackedRace)) {
            startAnalysisCompetitorDTOsMap = new HashMap<String, StartAnalysisCompetitorDTO>();
            startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace.put(trackedRace, startAnalysisCompetitorDTOsMap);
        } else {
            startAnalysisCompetitorDTOsMap = startAnalysisCompetitorDTOsForCompetitorNameForTrackedRace
                    .get(trackedRace);
        }
        return startAnalysisCompetitorDTOsMap;
    }

    private Map<String, List<GPSFixDTO>> getOrCreateGPSFixDTOsForCompetitorForTrackedRace(TrackedRace trackedRace) {
        try {
            if (!gpsFixesForCompetitorNameForTracedRace.containsKey(trackedRace)) {
                gpsFixesForCompetitorNameForTracedRace.put(
                        trackedRace,
                        getGPSFixDTOListForCompetitorName(
                                trackedRace.getRaceIdentifier(),
                                createPerCompetitorIdAsString(trackedRace, new Date(trackedRace.getStartTimeReceived()
                                        .asMillis() - 60000)),
                                createPerCompetitorIdAsString(trackedRace, new Date(trackedRace.getStartTimeReceived()
                                        .asMillis())), true));
            }
        } catch (NoWindException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return gpsFixesForCompetitorNameForTracedRace.get(trackedRace);
    }

    private StartAnalysisDTO getOrCreateStaticStartAnalysisDTOForTrackedRace(TrackedRace trackedRace) {
        StartAnalysisDTO startAnalysisDTOWithOnlyStaticData = createStartAnalysisDTOWithOnlyStaticData(trackedRace);
        if (!startAnalysisDTOWithStaticData.containsKey(trackedRace)) {
            startAnalysisDTOWithOnlyStaticData = createStartAnalysisDTOWithOnlyStaticData(trackedRace);
            startAnalysisDTOWithStaticData.put(trackedRace, startAnalysisDTOWithOnlyStaticData);
        } else {
            startAnalysisDTOWithOnlyStaticData = startAnalysisDTOWithStaticData.get(trackedRace);
        }
        return startAnalysisDTOWithOnlyStaticData;
    }

    public static List<StartAnalysisDTO> cloneList(List<StartAnalysisDTO> list) {
        List<StartAnalysisDTO> clone = new ArrayList<StartAnalysisDTO>(list.size());
        for (StartAnalysisDTO item : list)
            clone.add(item);
        return clone;
    }
    
    public void addAsListener(TrackedRace trackedRace) {
        trackedRace.addListener(new Listener(trackedRace));
    }

    private class Listener extends AbstractRaceChangeListener {
        private final TrackedRace trackedRace;
        
        protected Listener(TrackedRace trackedRace) {
            this.trackedRace = trackedRace;
        }

        @Override
        public void markPassingReceived(Competitor competitor, Map<Waypoint, MarkPassing> oldMarkPassings,
                Iterable<MarkPassing> markPassings) {
            if (markPassings.iterator().hasNext()) {
                if (getIndexOfMarkPassingMark(markPassings, trackedRace) == 1) {
                    // Add Competitor name to raning list at mark one for TrackedRace
                    getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace).add(competitor.getName());
                    // Create StartAnalysisCompetitorDTO
                    StartAnalysisCompetitorDTO startAnalysisCompetitorDTO = createStartAnalysisCompetitorDTO(
                            trackedRace, getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace).size(), competitor);
                    // Add created StartAnalysisiCompetitorDTO to StartAnalyisisCompetitorDTOsMap for TrackedRace
                    getOrCreateStartAnalysisCompetitorDTOsMapForTrackedRace(trackedRace).put(
                            startAnalysisCompetitorDTO.rankingTableEntryDTO.teamName, startAnalysisCompetitorDTO);
                    if (getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace).size() == 3) {
                        StartAnalysisDTO staticStartAnalysisDTO = getOrCreateStaticStartAnalysisDTOForTrackedRace(trackedRace);
                        for (String competitorName : getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace)) {
                            List<StartAnalysisDTO> startAnalysisListForCompetitor = startAnalysisDTOListForCompetitorName
                                    .get(competitorName);
                            startAnalysisListForCompetitor.add(createStartAnalysisDTOForCompetitorName(
                                    staticStartAnalysisDTO, competitorName, trackedRace));
                            startAnalysisDTOListForCompetitorName.put(competitorName, startAnalysisListForCompetitor);
                        }
                        notifyListernersAboutStartAnalysisDTOsChanges();
                    } else if (getOrCreateRankingListAtMarkOneForTrackedRace(trackedRace).size() > 3) {
                        StartAnalysisDTO staticStartAnalysisDTO = getOrCreateStaticStartAnalysisDTOForTrackedRace(trackedRace);
                        List<StartAnalysisDTO> startAnalysisListForCompetitor = startAnalysisDTOListForCompetitorName
                                .get(competitor.getName());
                        startAnalysisListForCompetitor.add(createStartAnalysisDTOForCompetitorName(
                                staticStartAnalysisDTO, competitor.getName(), trackedRace));
                        startAnalysisDTOListForCompetitorName.put(competitor.getName(), startAnalysisListForCompetitor);
                        notifyListernersAboutStartAnalysisDTOsChanges();
                        trackedRace.removeListener(this);
                    }
                }
            }
        }
    }
}
