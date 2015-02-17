package com.sap.sailing.dashboards.gwt.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.dashboards.gwt.client.RibDashboardService;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAdvantageType;
import com.sap.sailing.dashboards.gwt.server.startanalysis.StartAnalysisCreationController;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * The server side implementation of the RPC {@link RibDashboardService}.
 *
 * @author Alexander Ries (D062114)
 *
 */
public class RibDashboardServiceImpl extends RemoteServiceServlet implements RibDashboardService {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private StartAnalysisCreationController startAnalysisCreationController;

    /**
     * Variable contains last {@link TrackedRace} that is or was live
     * */
    private TrackedRace runningRace;

    /**
     * {@link MovingAverage} of last 400 start line advantage values during the last races. See initialization at
     * {@link #RibDashboardServiceImpl()}) Advantage by wind is the advantage in meters that a boat, starting a the pin
     * end of the line, has, compared to a boat starting at the very right side of the line.
     * */
    private MovingAverage averageStartLineAdvantageByWind;

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
        context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        baseDomainFactory = getRacingEventService().getBaseDomainFactory();
        startAnalysisCreationController = new StartAnalysisCreationController(getRacingEventService());

        averageStartLineAdvantageByWind = new MovingAverage(400);
        averageStartLineAdvantageByGeometry = new MovingAverage(400);
    }

    protected RacingEventService getRacingEventService() {
        return racingEventServiceTracker.getService(); // grab the service
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
     * {@link #getLiveRaceFromLeaderboardName(String)}. <param>competitorName</param> If competitorName is null, the
     * response contains only a list of competitors in the live race. Otherwise the parameter is used to return the
     * right startanalysis for a specific competitor with in the returned {@link RibDashboardRaceInfoDTO}.
     * @throws NoWindException 
     * */
    @Override
    public RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardName) throws NoWindException {
        RibDashboardRaceInfoDTO lRInfo = new RibDashboardRaceInfoDTO();
/*        if (leaderboardName != null) {
            TimePoint timePointOfRequest = MillisecondsTimePoint.now();
            if (checkIfRaceIsStillRunning(timePointOfRequest, leaderboardName)) {
                if (competitorName == null) {
                    fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
                    logger.log(Level.INFO, "No Competitor selected");
                    lRInfo.responseMessage = ResponseMessage.NO_COMPETITOR_SELECTED;
                    return lRInfo;
                } else {
                    fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
                    fillLiveRaceInfoDTOWithStartLineAdavantageData(lRInfo, timePointOfRequest);
                    lRInfo.responseMessage = ResponseMessage.OK;
                }
            } else {
                lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;
            }
            return lRInfo;
        } else {
            lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;*/
            return lRInfo;
        //}
    }

    private void fillLiveRaceInfoDTOWithRaceData(RibDashboardRaceInfoDTO lRInfo, TimePoint now) {
        if (runningRace != null) {
            lRInfo.idOfLastTrackedRace = runningRace.getRaceIdentifier();
        }
    }

    // returns true if race is still live
    private boolean checkIfRaceIsStillRunning(TimePoint now, String leaderboardName) {
        if (runningRace == null || !(runningRace.isLive(now))) {
            runningRace = getLiveRaceFromLeaderboardName(leaderboardName);
            if (runningRace == null) {
                return false;
            } else {
                //startAnalysisRacesStore.addAsListener(runningRace);
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
                    if (race != null && race.isLive(new MillisecondsTimePoint(new Date()))) {
                        result = race;
                    }
                }
            }
        }
        return result;
    }
    
    private List<TrackedRace> getTrackedRacesFromLeaderboard(String leaderboardName){
        List<TrackedRace> result = new ArrayList<TrackedRace>();
        Leaderboard lb = getRacingEventService().getLeaderboardByName(leaderboardName);
        if (lb != null) {
            for (RaceColumn column : lb.getRaceColumns()) {
                for (Fleet fleet : column.getFleets()) {
                    TrackedRace race = column.getTrackedRace(fleet);
                    if (race != null && race.isLive(new MillisecondsTimePoint(new Date()))) {
                        result.add(race);
                    }
                }
            }
        }
        return result;
    }

    private void fillLiveRaceInfoDTOWithStartLineAdavantageData(RibDashboardRaceInfoDTO lRInfo, TimePoint timePoint) throws NoWindException {
        if (runningRace != null && timePoint != null) {
            StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
            StartlineAdvantageType startlineAdvantageType = getStartlineAdvantageType(runningRace, timePoint);
            if(startlineAdvantageType == null || startlineAdvantageType == StartlineAdvantageType.WIND){
                startLineAdvantageDTO.startLineAdvatageType = StartlineAdvantageType.WIND;
                double startlineAdvantage = runningRace.getStartLine(timePoint).getAdvantage().getMeters();
                startLineAdvantageDTO.startLineAdvantage = startlineAdvantage;
                averageStartLineAdvantageByWind.add(startlineAdvantage);
                startLineAdvantageDTO.startlineAdvantageAverage = averageStartLineAdvantageByWind.getAverage();
            }else if(startlineAdvantageType == StartlineAdvantageType.GEOMETRIC){
                startLineAdvantageDTO.startLineAdvatageType = StartlineAdvantageType.GEOMETRIC;
                double startlineAdvantage = runningRace.getStartLine(timePoint).getAdvantage().getMeters();
                startLineAdvantageDTO.startLineAdvantage = startlineAdvantage;
                averageStartLineAdvantageByGeometry.add(startlineAdvantage);
                startLineAdvantageDTO.startlineAdvantageAverage = averageStartLineAdvantageByGeometry.getAverage();
            }
            lRInfo.startLineAdvantageDTO = startLineAdvantageDTO;
        }
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

    private LegType getFirstLegTypeOfTrackedRaceAtTimePoint(TrackedRace trackedRace, TimePoint timePoint) throws NoWindException{
        Iterable<TrackedLeg> trackedLegs = trackedRace.getTrackedLegs();
        if(trackedLegs != null && trackedLegs.iterator().hasNext()){
            TrackedLeg firstLegInTrackedRace = trackedLegs.iterator().next();
            return firstLegInTrackedRace.getLegType(timePoint);
        }else{
            return null;
        }
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public List<StartAnalysisDTO> getStartAnalysisListForCompetitorIDAndLeaderboardName(Serializable competitorId,  String leaderboardName) {
        Competitor competitor = baseDomainFactory.getCompetitorStore().getExistingCompetitorById(competitorId);
        List<TrackedRace> trackedRacesForLeaderBoardName = getTrackedRacesFromLeaderboard(leaderboardName);
        for(TrackedRace trackedRace : trackedRacesForLeaderBoardName){
            startAnalysisCreationController.checkForNewStartAnalysisForCompetitorInTrackedRace(competitor, trackedRace);
        }
        return startAnalysisCreationController.getStartAnalysisCache().getStartAnalysisDTOsForCompetitor(competitor);
    }

    @Override
    public List<CompetitorDTO> getCompetitorsInRaceWithStateLive() {
        if(runningRace != null){
            try {
                return baseDomainFactory.getCompetitorDTOList(runningRace.getCompetitorsFromBestToWorst(new MillisecondsTimePoint(new Date())));
            } catch (NoWindException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}