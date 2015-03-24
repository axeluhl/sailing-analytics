package com.sap.sailing.dashboards.gwt.server.startanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisCompetitorDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisRankingTableEntryDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.WindAndAdvantagesInfoForStartLineDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.LineDetails;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class StartAnalysisDTOFactory extends AbstractStartAnalysisCreationValidator{
    
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    
    private static int MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS = 3;
    
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
        
        boolean isCompetitorOneOfFirstThree = false;
        for (int i = 0; i < MINIMUM_NUMBER_COMPETITORS_FOR_STARTANALYSIS; i++) {
            competitors.add(createStartAnalysisCompetitorDTO(trackedRace, i + 1,
                    markPassingsInOrder.get(i).getCompetitor()));
            if (markPassingsInOrder.get(i).getCompetitor().equals(competitor)) {
                isCompetitorOneOfFirstThree = true;
            }
        }
        if (!isCompetitorOneOfFirstThree) {
            int rankOfCompetitorWhilePassingSecondWaypoint = getRankOfCompetitorWhilePassingSecondWaypoint(competitor, trackedRace);
            competitors.add(createStartAnalysisCompetitorDTO(trackedRace, rankOfCompetitorWhilePassingSecondWaypoint, competitor));
        }
        startAnalysisDTO.competitor = baseDomainFactory.getCompetitorStore().convertToCompetitorDTO(competitor);
        startAnalysisDTO.startAnalysisCompetitorDTOs = competitors;
        startAnalysisDTO.timeOfStartInMilliSeconds = trackedRace.getStartOfRace().asMillis();
        startAnalysisDTO.regattaAndRaceIdentifier = trackedRace.getRaceIdentifier();
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
        startAnalysisDTO.startAnalysisWindLineInfoDTO = createStartAnalysisWindAndLineData(trackedRace);
        return startAnalysisDTO;
    }


    private StartAnalysisRankingTableEntryDTO createRankTableEntry(TrackedRace trackedRace, int rank,
            Competitor competitor) {
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
        StartAnalysisCompetitorDTO startAnalysisCompetitorDTOsForRace = new StartAnalysisCompetitorDTO();
        startAnalysisCompetitorDTOsForRace.competitorDTO = baseDomainFactory.getCompetitorStore().convertToCompetitorDTO(competitor);
        startAnalysisCompetitorDTOsForRace.rankingTableEntryDTO = createRankTableEntry(trackedRace, rank, competitor);
        return startAnalysisCompetitorDTOsForRace;
    }
}
