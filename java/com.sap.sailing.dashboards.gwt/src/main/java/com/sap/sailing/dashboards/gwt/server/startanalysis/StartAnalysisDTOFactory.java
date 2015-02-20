package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
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
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartAnalysisDTOFactory extends AbstractStartAnalysisCreationValidator{
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private static int MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS = 3;
    private final int TAILLENGHT_IN_MILLISECONDS = 200000;
    
    private static final Logger logger = Logger.getLogger(StartAnalysisDTOFactory.class.getName());
    
    public StartAnalysisDTOFactory(RacingEventService racingEventService){
        baseDomainFactory = racingEventService.getBaseDomainFactory();
    }
    
    public StartAnalysisDTO createStartAnalysisForCompetitorAndTrackedRace(Competitor competitor, TrackedRace trackedRace) {
        StartAnalysisDTO startAnalysisDTO = new StartAnalysisDTO();
        addStaticDataToStartAnalysisDTOFrom(startAnalysisDTO, trackedRace);
        List<StartAnalysisCompetitorDTO> competitors = new ArrayList<StartAnalysisCompetitorDTO>();
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        List<MarkPassing> markPassingsInOrder = convertMarkpPassingsIteratorToList(trackedRace.getMarkPassingsInOrder(secondWaypoint).iterator());
        
        boolean isCompetitorBetweenFirstThree = false;
        for (int i = 0; i < MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS; i++) {
            competitors.add(createStartAnalysisCompetitorDTO(trackedRace, i + 1,
                    markPassingsInOrder.get(i).getCompetitor()));
            if (markPassingsInOrder.get(i).getCompetitor().equals(competitor)) {
                isCompetitorBetweenFirstThree = true;
            }
        }
        if (!isCompetitorBetweenFirstThree) {
            int rankOfCompetitorWhilePassingSecondWaypoint = getRankOfCompetitorWhilePassingSecondWaypoint(competitor, trackedRace);
            competitors.add(createStartAnalysisCompetitorDTO(trackedRace, rankOfCompetitorWhilePassingSecondWaypoint, competitor));
        }
        startAnalysisDTO.competitor = baseDomainFactory.getCompetitorStore().convertToCompetitorDTO(competitor);
        startAnalysisDTO.startAnalysisCompetitorDTOs = competitors;
        logger.log(Level.INFO, "Created StartAnalysis For Competitor "+competitor.getName()+" and "+trackedRace.getRace().getName());
        return startAnalysisDTO;
    }
    
    private List<MarkPassing> convertMarkpPassingsIteratorToList(Iterator<MarkPassing> iterator){
        List<MarkPassing> list = new ArrayList<MarkPassing>();
        while(iterator.hasNext()){
            list.add(iterator.next());
        }
        return list;
    }
    
    private int getRankOfCompetitorWhilePassingSecondWaypoint(Competitor competitor, TrackedRace trackedRace){
        Waypoint secondWaypoint = trackedRace.getRace().getCourse().getFirstLeg().getTo();
        Iterator<MarkPassing> markPassings = trackedRace.getMarkPassingsInOrder(secondWaypoint).iterator();
        int counter = 0;
        while(markPassings.hasNext()){
            counter ++;
            if(markPassings.next().getCompetitor().getId().equals(competitor.getId()))
                return counter;
        }
        return 0;
    }
    
    private StartAnalysisDTO addStaticDataToStartAnalysisDTOFrom(StartAnalysisDTO startAnalysisDTO,TrackedRace trackedRace) {
        startAnalysisDTO.raceName = trackedRace.getRace().getName();
        startAnalysisDTO.startLineMarkPositions = getStartLineBoyPositions(trackedRace);
        startAnalysisDTO.firstMark = getMarksOfWayPoint(trackedRace, getFirstMarkWayPoint(trackedRace)).get(0);
        startAnalysisDTO.startAnalysisWindLineInfoDTO = createStartAnalysisWindAndLineData(trackedRace);
        startAnalysisDTO.startLineMarks = getMarksOfWayPoint(trackedRace,
                trackedRace.getStartLine(trackedRace.getStartTimeReceived()).getWaypoint());
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
            e.printStackTrace();
        }
        return fromPerCompetitorIdAsString;
    }

    private StartAnalysisRankingTableEntryDTO createRankTableEntry(TrackedRace trackedRace, int rank,
            Competitor competitor) {
        StartAnalysisRankingTableEntryDTO startAnalysisRankTableEntryDTO = createStartAnalysisRankTableEntryDTOWithRankAndStartTimepoint(
                competitor, rank, trackedRace);
        return startAnalysisRankTableEntryDTO;
    }

    private StartAnalysisRankingTableEntryDTO createStartAnalysisRankTableEntryDTOWithRankAndStartTimepoint(
            Competitor competitor, int rank, TrackedRace trackedRace) {
        logger.log(Level.INFO, "createStartAnalysisRankTableEntryDTOWithRankAndStartTimepoint");
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

    private Waypoint getFirstMarkWayPoint(TrackedRace trackedRace) {
        Waypoint firstMarkWayPoint = null;
        Iterator<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints().iterator();
        int wayPointCounter = 0;
        while (waypoints.hasNext()) {
            Waypoint wayPoint = waypoints.next();
            if (wayPointCounter == 1) {
                return wayPoint;
            }
            wayPointCounter++;
        }
        return firstMarkWayPoint;
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

    private List<MarkDTO> getMarksOfWayPoint(TrackedRace trackedRace, Waypoint wayPoint) {
        List<MarkDTO> startLineMarks = new ArrayList<MarkDTO>();
        for (Mark startMark : wayPoint.getMarks()) {
            final Position estimatedMarkPosition = trackedRace.getOrCreateTrack(startMark).getEstimatedPosition(
                    trackedRace.getStartTimeReceived(), /* extrapolate */false);
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

    private WindAndAdvantagesInfoForStartLineDTO createStartAnalysisWindAndLineData(TrackedRace trackedRace) {
        WindAndAdvantagesInfoForStartLineDTO startAnalysisWindLineInfoDTO = new WindAndAdvantagesInfoForStartLineDTO();
        LineDetails startline = trackedRace.getStartLine(trackedRace.getStartTimeReceived());
        StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
        startLineAdvantageDTO.startLineAdvatageType = getStartlineAdvantageType(trackedRace, new MillisecondsTimePoint(new Date()));
        startLineAdvantageDTO.startLineAdvantage = startline.getAdvantage().getMeters();
        startAnalysisWindLineInfoDTO.startLineAdvantage = startLineAdvantageDTO;
        Position portMarkPosition = trackedRace.getOrCreateTrack(
                trackedRace.getStartLine(trackedRace.getStartTimeReceived()).getStarboardMarkWhileApproachingLine())
                .getEstimatedPosition(trackedRace.getStartTimeReceived(), /* extrapolate */
                false);
        Wind windAtStart = trackedRace.getWind(portMarkPosition, trackedRace.getStartTimeReceived());
        startAnalysisWindLineInfoDTO.windDirectionInDegrees = windAtStart.getBearing().getDegrees();
        startAnalysisWindLineInfoDTO.windSpeedInKnots = windAtStart.getKnots();
        return startAnalysisWindLineInfoDTO;
    }

    private StartlineAdvantageType getStartlineAdvantageType(TrackedRace trackedRace, TimePoint timePoint) {
        try {
            LegType typeOfFirstLeg;
            typeOfFirstLeg = getFirstLegTypeOfTrackedRaceAtTimePoint(trackedRace, timePoint);
            switch (typeOfFirstLeg) {
            case UPWIND:
                return StartlineAdvantageType.WIND;
            case REACHING:
                return StartlineAdvantageType.GEOMETRIC;
            default:
                return StartlineAdvantageType.WIND;
            }
        } catch (NoWindException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, List<GPSFixDTO>> getGPSFixDTOListForCompetitorName(TrackedRace trackedRace,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException {
        Map<String, List<GPSFixDTO>> result = new HashMap<String, List<GPSFixDTO>>();
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
                    // copy the fixes into a list while holding the monitor;
                    // then release the monitor to avoid deadlocks
                    // during wind estimations required for tack determination
                    List<GPSFixMoving> fixes = new ArrayList<GPSFixMoving>();
                    track.lockForRead();
                    try {
                        Iterator<GPSFixMoving> fixIter = track.getFixesIterator(fromTimePoint, /* inclusive */
                                true);
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

    private LegType getFirstLegTypeOfTrackedRaceAtTimePoint(TrackedRace trackedRace, TimePoint timePoint)
            throws NoWindException {
        Iterable<TrackedLeg> trackedLegs = trackedRace.getTrackedLegs();
        if (trackedLegs != null && trackedLegs.iterator().hasNext()) {
            TrackedLeg firstLegInTrackedRace = trackedLegs.iterator().next();
            return firstLegInTrackedRace.getLegType(timePoint);
        } else {
            return null;
        }
    }

    private StartAnalysisCompetitorDTO createStartAnalysisCompetitorDTO(TrackedRace trackedRace, int rank,
            Competitor competitor) {
        logger.log(Level.INFO, "createStartAnalysisCompetitorDTO => "+competitor.getName());
        StartAnalysisCompetitorDTO startAnalysisCompetitorDTOsForRace = new StartAnalysisCompetitorDTO();
        startAnalysisCompetitorDTOsForRace.competitorDTO = baseDomainFactory.getCompetitorStore().convertToCompetitorDTO(competitor);
        startAnalysisCompetitorDTOsForRace.rankingTableEntryDTO = createRankTableEntry(trackedRace, rank, competitor);
        List<GPSFixDTO> boatPositions = getOrCreateGPSFixDTOsForCompetitorForTrackedRace(trackedRace).get(
                competitor.getName());
        startAnalysisCompetitorDTOsForRace.gpsFixDTOs = boatPositions;
        return startAnalysisCompetitorDTOsForRace;
    }

    private Map<String, List<GPSFixDTO>> getOrCreateGPSFixDTOsForCompetitorForTrackedRace(TrackedRace trackedRace) {
        try {
            return getGPSFixDTOListForCompetitorName(trackedRace, createPerCompetitorIdAsString(trackedRace, new Date(trackedRace.getStartTimeReceived()
                                            .asMillis() - TAILLENGHT_IN_MILLISECONDS)), createPerCompetitorIdAsString(trackedRace, new Date(trackedRace.getStartTimeReceived()
                                                  .asMillis())), true);
        } catch (NoWindException e) {
            return null;
        }
    }
}
