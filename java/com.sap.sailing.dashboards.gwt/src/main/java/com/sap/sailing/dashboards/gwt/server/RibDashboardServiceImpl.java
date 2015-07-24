package com.sap.sailing.dashboards.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.dashboards.gwt.client.RibDashboardService;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.server.startanalysis.StartAnalysisCreationController;
import com.sap.sailing.dashboards.gwt.server.startlineadvantages.StartlineAdvantagesCalculator;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.ResponseMessage;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartlineAdvantagesWithMaxAndAverageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * The server side implementation of the RPC {@link RibDashboardService}.
 *
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardServiceImpl extends RemoteServiceServlet implements RibDashboardService, LiveTrackedRaceProvider {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private StartAnalysisCreationController startAnalysisCreationController;
    
    private StartlineAdvantagesCalculator startlineAdvantagesCalculator;

    /**
     * Variable contains last {@link TrackedRace} that is or was live
     * */
    private TrackedRace runningRace;

    /**
     * {@link MovingAverage} of last 400 start line advantage values during the last races. See initialization at
     * {@link #RibDashboardServiceImpl()}) Advantage by geometry is the delta in meters of the distance from race
     * committee boat to first mark and pin end mark to first mark. Wind has no influence on the value.
     * */
    private MovingAverage averageStartLineAdvantageByGeometry;

    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    private final BundleContext context;

    private static final Logger logger = Logger.getLogger(RibDashboardServiceImpl.class.getName());

    public RibDashboardServiceImpl() {
        this.context = Activator.getDefault();
        this.racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        this.baseDomainFactory = getRacingEventService().getBaseDomainFactory();
        this.startAnalysisCreationController = new StartAnalysisCreationController(getRacingEventService());
        this.startlineAdvantagesCalculator = new StartlineAdvantagesCalculator(getPolarService());
        addLiveTrackedRaceListener(startlineAdvantagesCalculator);
        this.averageStartLineAdvantageByGeometry = new MovingAverage(400);
    }
    
    protected RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService(); // grab the service
    }
    
    private PolarDataService getPolarService() {
        ServiceReference<PolarDataService> polarServiceReference = context.getServiceReference(PolarDataService.class);
        PolarDataService polarDataService = context.getService(polarServiceReference);
        return polarDataService;
    }

    protected ServiceTracker<RacingEventService, RacingEventService> createAndOpenRacingEventServiceTracker(
            BundleContext context) {
        ServiceTracker<RacingEventService, RacingEventService> result = new ServiceTracker<RacingEventService, RacingEventService>(
                context, RacingEventService.class.getName(), null);
        result.open();
        return result;
    }

    /**
     * <param>leaderboardName</param> is used to retrieve the live running race with the method
     * {@link #getLiveRaceFromLeaderboardName(String)}.
     * 
     * @throws NoWindException
     * */
    @Override
    public RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardName) throws NoWindException {
        RibDashboardRaceInfoDTO lRInfo = new RibDashboardRaceInfoDTO();
        if (leaderboardName != null) {
            TimePoint timePointOfRequest = MillisecondsTimePoint.now();
            if (checkIfRaceIsStillRunning(timePointOfRequest, leaderboardName)) {
                fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
                fillLiveRaceInfoDTOWithStartLineAdavantageByGeometryData(lRInfo, timePointOfRequest);
                lRInfo.responseMessage = ResponseMessage.RACE_LIVE;
            } else {
                lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;
            }
        } else {
            lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;
        }
        return lRInfo;
    }

    private void fillLiveRaceInfoDTOWithRaceData(RibDashboardRaceInfoDTO lRInfo, TimePoint now) {
        if (runningRace != null) {
            lRInfo.idOfLastTrackedRace = runningRace.getRaceIdentifier();
            List<Competitor> competitors = runningRace.getCompetitorsFromBestToWorst(now);
            List<String> competitorNames = new ArrayList<String>();
            for (Competitor competitor : competitors) {
                competitorNames.add(competitor.getName());
            }
            Collections.sort(competitorNames);
            lRInfo.competitorNamesFromLastTrackedRace = competitorNames;
        }
    }

    // returns true if race is still live
    private boolean checkIfRaceIsStillRunning(TimePoint now, String leaderboardName) {
        if (runningRace == null || !(runningRace.isLive(now))) {
            runningRace = getLiveRaceFromLeaderboardName(leaderboardName);
            if (runningRace == null) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private TrackedRace getLiveRaceFromLeaderboardName(String leaderboardName) {
        TrackedRace result = null;
        Leaderboard lb = getRacingEventService().getLeaderboardByName(leaderboardName);
        if (lb != null) {
            for (RaceColumn column : lb.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null) {
                        notifyLiveTrackedRaceListenerAboutLiveTrackedRaceChange(race);
                        TimePoint startOfRace = race.getStartOfRace();
                        // not relying on isLive() because the time window is too short
                        // to retrieve wind information we need to extend the time window
                        if (startOfRace != null && race.getEndOfRace() == null && 
                                MillisecondsTimePoint.now().after(startOfRace.minus(Duration.ONE_MINUTE.times(20)))) {
                            result = race;
                            // no break here as we want to have the last race that is deemed to be live
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<TrackedRace> getTrackedRacesFromLeaderboard(String leaderboardName) {
        List<TrackedRace> result = new ArrayList<TrackedRace>();
        Leaderboard lb = getRacingEventService().getLeaderboardByName(leaderboardName);
        if (lb != null) {
            for (RaceColumn column : lb.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null) {
                        result.add(race);
                    }
                }
            }
        }
        return result;
    }

    private void fillLiveRaceInfoDTOWithStartLineAdavantageByGeometryData(RibDashboardRaceInfoDTO lRInfo,
            TimePoint timePoint) throws NoWindException {
        if (runningRace != null && timePoint != null) {
            StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
            startLineAdvantageDTO.startLineAdvatageType = StartlineAdvantageType.GEOMETRIC;
            double startlineAdvantage = calculateStartlineAdvantageByGeometry();
            startLineAdvantageDTO.startLineAdvantage = startlineAdvantage;
            averageStartLineAdvantageByGeometry.add(startlineAdvantage);
            startLineAdvantageDTO.average = averageStartLineAdvantageByGeometry.getAverage();
            lRInfo.startLineAdvantageDTO = startLineAdvantageDTO;
        }
    }

    private double calculateStartlineAdvantageByGeometry() {
        double result = 0.0;
        Course course = runningRace.getRace().getCourse();
        if (course != null) {
            Waypoint startlineWayPoint = course.getFirstLeg().getFrom();
            Waypoint firstmarkWayPoint = course.getFirstLeg().getTo();
            if (startlineWayPoint != null && firstmarkWayPoint != null) {
                Pair<Position, Position> startlineMarkPositions = retrieveStartlineMarkPositionsFromStartLineWayPoint(startlineWayPoint);
                Position firstMarkPosition = retrieveFirstMarkPositionFromFirstMarkWayPoint(firstmarkWayPoint);
                if (startlineMarkPositions != null && firstMarkPosition != null) {
                    Distance rcToMark = firstMarkPosition.getDistance(startlineMarkPositions.getA());
                    Distance pinToMark = firstMarkPosition.getDistance(startlineMarkPositions.getB());
                    return pinToMark.getMeters() - rcToMark.getMeters();
                }
            }
        }
        return result;
    }

    private Pair<Position, Position> retrieveStartlineMarkPositionsFromStartLineWayPoint(Waypoint startLineWayPoint) {
        Pair<Position, Position> startLineMarkPositions = null;
        Iterator<Mark> markIterator = startLineWayPoint.getMarks().iterator();
        if (markIterator.hasNext()) {
            Mark startboat = (Mark) markIterator.next();
            if (markIterator.hasNext()) {
                Mark pinEnd = (Mark) markIterator.next();
                TimePoint now = MillisecondsTimePoint.now();
                Position startBoatPosition = getPositionFromMarkAtTimePoint(runningRace, startboat, now);
                Position pinEndPosition = getPositionFromMarkAtTimePoint(runningRace, pinEnd, now);
                return  new Pair<Position, Position>(startBoatPosition, pinEndPosition);
            }
        }
        return startLineMarkPositions;
    }

    private Position getPositionFromMarkAtTimePoint(TrackedRace trackedRace, Mark mark, TimePoint timePoint) {
        GPSFixTrack<Mark, GPSFix> fixTrack = trackedRace.getTrack(mark);
        return fixTrack.getEstimatedPosition(timePoint, true);
    }

    private Position retrieveFirstMarkPositionFromFirstMarkWayPoint(Waypoint firstMarkWayPoint) {
        Position firstMarkPosition = null;
        if (firstMarkWayPoint.getMarks().iterator().hasNext()) {
            Mark firstMark = firstMarkWayPoint.getMarks().iterator().next();
            TimePoint now = MillisecondsTimePoint.now();
            firstMarkPosition = getPositionFromMarkAtTimePoint(runningRace, firstMark, now);
            logger.log(Level.INFO, "Firstmark: " + firstMarkPosition);
        }
        return firstMarkPosition;
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public List<StartAnalysisDTO> getStartAnalysisListForCompetitorIDAndLeaderboardName(String competitorIdAsString,
            String leaderboardName) {
        List<StartAnalysisDTO> startAnalysisDTOs = new ArrayList<StartAnalysisDTO>();
        if (leaderboardName != null) {
            Competitor competitor = baseDomainFactory.getCompetitorStore().getExistingCompetitorByIdAsString(competitorIdAsString);
            List<TrackedRace> trackedRacesForLeaderBoardName = getTrackedRacesFromLeaderboard(leaderboardName);
            for (TrackedRace trackedRace : trackedRacesForLeaderBoardName) {
                StartAnalysisDTO startAnalysisDTO = startAnalysisCreationController
                        .checkStartAnalysisForCompetitorInTrackedRace(competitor, trackedRace);
                if (startAnalysisDTO != null) {
                    startAnalysisDTOs.add(startAnalysisDTO);
                }
            }
        }
        return startAnalysisDTOs;
    }

    @Override
    public List<CompetitorDTO> getCompetitorsInLeaderboard(String leaderboardName) {
        logger.log(Level.INFO, "getCompetitorsInLeaderboard(...) Request.");
        if (leaderboardName != null) {
            Leaderboard lb = getRacingEventService().getLeaderboardByName(leaderboardName);
            return baseDomainFactory.getCompetitorDTOList(lb.getCompetitorsFromBestToWorst(new MillisecondsTimePoint(
                    new Date())));
        } else {
            return null;
        }
    }

    @Override
    public StartlineAdvantagesWithMaxAndAverageDTO getAdvantagesOnStartline(String leaderboardName) {
        StartlineAdvantagesWithMaxAndAverageDTO result = startlineAdvantagesCalculator.getStartLineAdvantagesAccrossLineAtTimePoint(MillisecondsTimePoint.now());
        for(StartLineAdvantageDTO startLineAdvantageDTO : result.advantages) {
            logger.log(Level.INFO, "X:"+startLineAdvantageDTO.distanceToRCBoatInMeters+" Y: "+startLineAdvantageDTO.startLineAdvantage+" C: "+startLineAdvantageDTO.confidence);
        }
        return startlineAdvantagesCalculator.getStartLineAdvantagesAccrossLineAtTimePoint(MillisecondsTimePoint.now());
    }
    

    @Override
    public void notifyLiveTrackedRaceListenerAboutLiveTrackedRaceChange(TrackedRace trackedRace) {
        listener.forEach(listener -> listener.liveTrackedRaceDidChange(trackedRace));
    }
}