package com.sap.sailing.dashboards.gwt.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.dashboards.gwt.client.RibDashboardService;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.ResponseMessage;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
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
@SuppressWarnings("serial")
public class RibDashboardServiceImpl extends RemoteServiceServlet implements RibDashboardService,
        StartAnalysisRacesStoreListener {

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

    /**
     * The map values contain a Pair, whose first value is a {@link MovingAverage} for the true wind speed and the
     * second one contains an average for the true wind direction for a wind bot.
     * */

    private Map<String, List<StartAnalysisDTO>> startAnalysisDTOsForCompetitor;
    private StartAnalysisRacesStore startAnalysisRacesStore;
    
    private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
    private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
    private final BundleContext context;
    
    private static final Logger logger = Logger.getLogger(RibDashboardServiceImpl.class.getName());


    public RibDashboardServiceImpl() {
        context = Activator.getDefault();
        racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
        baseDomainFactory = getRacingEventService().getBaseDomainFactory();

        startAnalysisRacesStore = new StartAnalysisRacesStore(getRacingEventService());
        startAnalysisRacesStore.addStartAnalyisisRacesStoreListener(this);

        startAnalysisDTOsForCompetitor = new HashMap<String, List<StartAnalysisDTO>>();
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
     * <param>leaderboardGroupName</param> is used to retrieve the live running race with the method
     * {@link #getLiveRaceFromLeaderboardName(String)}. <param>competitorName</param> If competitorName is null, the
     * response contains only a list of competitors in the live race. Otherwise the parameter is used to return the
     * right startanalysis for a specific competitor with in the returned {@link RibDashboardRaceInfoDTO}.
     * @throws NoWindException 
     * */
    @Override
    public RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardGroupName, String competitorName) throws NoWindException {
        RibDashboardRaceInfoDTO lRInfo = new RibDashboardRaceInfoDTO();
        if (leaderboardGroupName != null) {
            TimePoint timePointOfRequest = MillisecondsTimePoint.now();
            if (checkIfRaceIsStillRunning(timePointOfRequest, leaderboardGroupName)) {
                if (competitorName == null) {
                    fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
                    logger.log(Level.INFO, "No Competitor selected");
                    lRInfo.responseMessage = ResponseMessage.NO_COMPETITOR_SELECTED;
                    return lRInfo;
                } else {
                    fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
                    fillLiveRaceInfoDTOWithStartLineAdavantageData(lRInfo, timePointOfRequest);
                    fillLiveRaceInfoDTOWithStartAnalysisData(lRInfo, competitorName);
                    lRInfo.responseMessage = ResponseMessage.OK;
                }
            } else {
                lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;
            }
            return lRInfo;
        } else {
            lRInfo.responseMessage = ResponseMessage.NO_RACE_LIVE;
            return lRInfo;
        }
    }

    private void fillLiveRaceInfoDTOWithRaceData(RibDashboardRaceInfoDTO lRInfo, TimePoint now) {
        if (runningRace != null) {
            lRInfo.idOfLastTrackedRace = runningRace.getRaceIdentifier();
            try {
                List<Competitor> competitors = runningRace.getCompetitorsFromBestToWorst(now);
                List<String> competitorNames = new ArrayList<String>();
                for (Competitor competitor : competitors) {
                    competitorNames.add(competitor.getName());
                }
                Collections.sort(competitorNames);
                lRInfo.competitorNamesFromLastTrackedRace = competitorNames;
            } catch (NoWindException e) {
                e.printStackTrace();
            }
        }
    }

    private void fillLiveRaceInfoDTOWithStartAnalysisData(RibDashboardRaceInfoDTO lRInfo, String competiorName) {
        lRInfo.startAnalysisDTOList = startAnalysisDTOsForCompetitor.get(competiorName);
    }

    // returns true if race is still live
    private boolean checkIfRaceIsStillRunning(TimePoint now, String leaderboardGroupName) {
        if (runningRace == null || !(runningRace.isLive(now))) {
            runningRace = getLiveRaceFromLeaderboardName(leaderboardGroupName);
            if (runningRace == null) {
                return false;
            } else {
                startAnalysisRacesStore.addAsListener(runningRace);
                return true;
            }
        } else {
            return true;
        }
    }

    private TrackedRace getLiveRaceFromLeaderboardName(String leaderboardGroupName) {
        TrackedRace result = null;
        Leaderboard lb = getRacingEventService().getLeaderboardByName(leaderboardGroupName);
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

    private void fillLiveRaceInfoDTOWithStartLineAdavantageData(RibDashboardRaceInfoDTO lRInfo, TimePoint timePoint) throws NoWindException {
        if (runningRace != null && timePoint != null) {
            StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
            startLineAdvantageDTO.legTypeOfFirstLegInTrackedRace = getFirstLegTypeOfTrackedRaceAtTimePoint(runningRace, timePoint);
            startLineAdvantageDTO.liveWindStartLineAdvantage = getWindStartLineAdvantageAtTimePoint(runningRace,
                    timePoint);
            startLineAdvantageDTO.liveGeometricStartLineAdvantage = getWindStartLineAdvantageAtTimePoint(
                    runningRace, timePoint);
            startLineAdvantageDTO.averageWindStartLineAdvantage = averageStartLineAdvantageByWind.getAverage();
            startLineAdvantageDTO.averageGeometricStartLineAdvantage = averageStartLineAdvantageByGeometry.getAverage();
            lRInfo.startLineAdvantageDTO = startLineAdvantageDTO;
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

    private double getWindStartLineAdvantageAtTimePoint(TrackedRace trackedRace, TimePoint timePoint) {
        double result = trackedRace.getStartLine(timePoint).getAdvantage().getMeters();
        averageStartLineAdvantageByWind.add(result);
        return result;
    }

    protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    public TrackedRace getExistingTrackedRace(RegattaAndRaceIdentifier regattaNameAndRaceName) {
        return null;
    }

    @Override
    public void startAnalyisisRacesChanged(Map<String, List<StartAnalysisDTO>> startAnalysisDTOCompetitorMap) {
        startAnalysisDTOsForCompetitor = startAnalysisDTOCompetitorMap;
    }
}