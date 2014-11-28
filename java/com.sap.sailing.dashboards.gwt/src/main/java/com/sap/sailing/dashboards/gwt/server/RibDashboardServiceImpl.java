package com.sap.sailing.dashboards.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sailing.dashboards.gwt.client.RibDashboardService;
import com.sap.sailing.dashboards.gwt.server.utils.MovingAverage;
import com.sap.sailing.dashboards.gwt.shared.ResponseMessage;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.WindBotComponentDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * The server side implementation of the RPC {@link RibDashboardService}.
 *
 * @author Alexander Ries (D062114)
 *
 */
@SuppressWarnings("serial")
public class RibDashboardServiceImpl extends RemoteServiceServlet implements
		RibDashboardService, StartAnalyisisRacesStoreListener {

	private final ServiceTracker<RacingEventService, RacingEventService> racingEventServiceTracker;
	private final com.sap.sailing.domain.base.DomainFactory baseDomainFactory;
	private final BundleContext context;
	private static final Logger logger = Logger
			.getLogger(RibDashboardServiceImpl.class.getName());

	/**
	 * Variable contains last {@link TrackedRace} that is or was live
	 * */
	private TrackedRace runningRace;

	/**
	 * {@link MovingAverage} of last 400 start line advantage values during the
	 * last races. See initialization at {@link #RibDashboardServiceImpl()})
	 * Advantage by wind is the advantage in meters that a boat, starting a the
	 * pin end of the line, has, compared to a boat starting at the very right
	 * side of the line.
	 * */
	private MovingAverage averageStartLineAdvantageByWind;

	/**
	 * {@link MovingAverage} of last 400 start line advantage values during the
	 * last races. See initialization at {@link #RibDashboardServiceImpl()})
	 * Advantage by geometry is the delta in meters of the distance from race
	 * committee boat to first mark and pin end mark to first mark. Wind has no
	 * influence on the value.
	 * */
	private MovingAverage averageStartLineAdvantageByGeometry;

	/**
	 * The map values contain a Pair, whose first value is a
	 * {@link MovingAverage} for the true wind speed and the second one contains
	 * an average for the true wind direction for a wind bot.
	 * */
	private Map<String, Pair<MovingAverage, MovingAverage>> speedAndDirectionAverageForWindBotID;

	private Map<String, List<StartAnalysisDTO>> startAnalysisDTOsForCompetitor;

	public RibDashboardServiceImpl() {
		context = Activator.getDefault();
		racingEventServiceTracker = createAndOpenRacingEventServiceTracker(context);
		baseDomainFactory = getRacingEventService().getBaseDomainFactory();

		startAnalysisDTOsForCompetitor = new HashMap<String, List<StartAnalysisDTO>>();
		averageStartLineAdvantageByWind = new MovingAverage(400);
		averageStartLineAdvantageByGeometry = new MovingAverage(400);
		speedAndDirectionAverageForWindBotID = new HashMap<String, Pair<MovingAverage, MovingAverage>>();
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
	 * <param>leaderboardGroupName</param> is used to retrieve the live running
	 * race with the method {@link #getLiveRaceFromLeaderboardName(String)}.
	 * <param>competitorName</param> If competitorName is null, the response
	 * contains only a list of competitors in the live race. Otherwise the
	 * parameter is used to return the right startanalysis for a specific
	 * competitor with in the returned {@link RibDashboardRaceInfoDTO}.
	 * */
	@Override
	public RibDashboardRaceInfoDTO getLiveRaceInfo(String leaderboardGroupName,
			String competitorName) {
		RibDashboardRaceInfoDTO lRInfo = new RibDashboardRaceInfoDTO();
		if (leaderboardGroupName != null) {
			TimePoint timePointOfRequest = MillisecondsTimePoint.now();
			if (checkIfRaceIsStillRunning(timePointOfRequest,
					leaderboardGroupName)) {
				if (competitorName == null) {
					fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
					logger.log(Level.INFO, "NO_COMPETITOR_SELECTED");
					lRInfo.responseMessage = ResponseMessage.NO_COMPETITOR_SELECTED;
					return lRInfo;
				} else {
					fillLiveRaceInfoDTOWithRaceData(lRInfo, timePointOfRequest);
					fillLiveRaceInfoDTOWithWindBotData(lRInfo,
							timePointOfRequest);
					fillLiveRaceInfoDTOWithStartLineAdavantageData(lRInfo,
							timePointOfRequest);
					fillLiveRaceInfoDTOWithStartAnalysisData(lRInfo,
							competitorName);
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

	private void fillLiveRaceInfoDTOWithWindBotData(
			RibDashboardRaceInfoDTO lRInfo, TimePoint timePoint) {
		if (runningRace != null || runningRace.isLive(timePoint) != false) {
			Map<String, WindBotComponentDTO> windBots = new HashMap<String, WindBotComponentDTO>();

			List<String> windSourcesToDeliver = new ArrayList<String>();
			Iterator<WindSource> windsourcedInRace = runningRace
					.getWindSources().iterator();
			while (windsourcedInRace.hasNext()) {
				WindSource currentWindSource = windsourcedInRace.next();
				windSourcesToDeliver.add(currentWindSource.name());
			}
			WindInfoForRaceDTO windInfoForRaceDTO = getAveragedWindInfo(
					runningRace.getRaceIdentifier(),
					new Date(
							runningRace.getTimePointOfNewestEvent().asMillis() - 10000),
					new Date(runningRace.getTimePointOfNewestEvent().asMillis()),
					1000, windSourcesToDeliver, true);

			Set<WindSource> windSourcesWithWind = windInfoForRaceDTO.windTrackInfoByWindSource
					.keySet();
			for (WindSource windSource : windSourcesWithWind) {
				WindTrackInfoDTO windTrackInfoDTOForSource = windInfoForRaceDTO.windTrackInfoByWindSource
						.get(windSource);
				if (windTrackInfoDTOForSource.windFixes != null
						&& windTrackInfoDTOForSource.windFixes.size() > 0) {
					WindDTO windDTO = windTrackInfoDTOForSource.windFixes
							.get(windTrackInfoDTOForSource.windFixes.size() - 1);
					if (windSource.getType() == WindSourceType.EXPEDITION) {
						WindBotComponentDTO windBotDTO = new WindBotComponentDTO();
						windBotDTO.id = windSource.getId().toString();
						windBotDTO.liveWindSpeedInKts = windDTO.trueWindSpeedInKnots;
						windBotDTO.liveWindDirectionInDegrees = windDTO.trueWindBearingDeg;
						windBotDTO.position = windDTO.position;
						Pair<Double, Double> windAverages = getUpdatedWindBotAverages(
								windSource.getId().toString(),
								windDTO.trueWindSpeedInKnots,
								windDTO.trueWindBearingDeg);
						windBotDTO.averageWindSpeedInKts = windAverages.getA();
						windBotDTO.averageWindDirectionInDegrees = windAverages
								.getB();
						windBots.put(windSource.getId().toString(), windBotDTO);
					}
				}
				lRInfo.windBotDTOForID = windBots;
			}
		}
	}

	private void fillLiveRaceInfoDTOWithRaceData(
			RibDashboardRaceInfoDTO lRInfo, TimePoint now) {
		if (runningRace != null) {
			lRInfo.nameOfLastTrackedRace = runningRace.getRace().getName();
			try {
				List<Competitor> competitors = runningRace
						.getCompetitorsFromBestToWorst(now);
				List<String> competitorNames = new ArrayList<String>();
				for (Competitor competitor : competitors) {
					competitorNames.add(competitor.getName());
				}
				Collections.sort(competitorNames);
				lRInfo.competitorNamesFromLastTrackedRace = competitorNames;
			} catch (NoWindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void fillLiveRaceInfoDTOWithStartAnalysisData(
			RibDashboardRaceInfoDTO lRInfo, String competiorName) {
		lRInfo.startAnalysisDTOList = startAnalysisDTOsForCompetitor
				.get(competiorName);
	}

	private Pair<Double, Double> getUpdatedWindBotAverages(String botId,
			double windSpeed, double windDirection) {
		Pair<MovingAverage, MovingAverage> windBotAveragesPair = speedAndDirectionAverageForWindBotID
				.get(botId);
		if (windBotAveragesPair == null) {
			MovingAverage newSpeedAverage = new MovingAverage(400);
			MovingAverage newDirectionAverage = new MovingAverage(400);
			Pair<MovingAverage, MovingAverage> newWindBotAveragesPair = new Pair<MovingAverage, MovingAverage>(
					newSpeedAverage, newDirectionAverage);
			speedAndDirectionAverageForWindBotID.put(botId,
					newWindBotAveragesPair);
			windBotAveragesPair = newWindBotAveragesPair;
		}
		MovingAverage speedAverage = windBotAveragesPair.getA();
		MovingAverage directionAverage = windBotAveragesPair.getB();
		speedAverage.add(windSpeed);
		directionAverage.add(windDirection);
		Pair<Double, Double> windAverages = new Pair<Double, Double>(
				Double.valueOf(speedAverage.getAverage()),
				Double.valueOf(directionAverage.getAverage()));
		return windAverages;
	}

	// returns true if race is still live
	private boolean checkIfRaceIsStillRunning(TimePoint now,
			String leaderboardGroupName) {
		if (runningRace == null || !(runningRace.isLive(now))) {
			runningRace = getLiveRaceFromLeaderboardName(leaderboardGroupName);
			if (runningRace == null) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	private TrackedRace getLiveRaceFromLeaderboardName(
			String leaderboardGroupName) {
		TrackedRace result = null;
		Leaderboard lb = getRacingEventService().getLeaderboardByName(
				leaderboardGroupName);
		if (lb != null) {
			for (RaceColumn column : lb.getRaceColumns()) {
				for (Fleet fleet : column.getFleets()) {
					TrackedRace race = column.getTrackedRace(fleet);
					if (race != null
							&& race.isLive(new MillisecondsTimePoint(new Date()))) {
						result = race;
					}
				}
			}
		}
		return result;
	}

	private void fillLiveRaceInfoDTOWithStartLineAdavantageData(
			RibDashboardRaceInfoDTO lRInfo, TimePoint timepoint) {
		if (runningRace != null && timepoint != null) {
			StartLineAdvantageDTO startLineAdvantageDTO = new StartLineAdvantageDTO();
			startLineAdvantageDTO.liveWindStartLineAdvantage = getWindStartLineAdvantageAtTimePoint(
					runningRace, timepoint);
			startLineAdvantageDTO.liveGeometricStartLineAdvantage = getGeometricStartLineAdvantageAtTimePoint(
					runningRace, timepoint);
			startLineAdvantageDTO.averageWindStartLineAdvantage = averageStartLineAdvantageByWind
					.getAverage();
			startLineAdvantageDTO.averageGeometricStartLineAdvantage = averageStartLineAdvantageByGeometry
					.getAverage();
			lRInfo.startLineAdvantageDTO = startLineAdvantageDTO;
		}
	}

	private double getWindStartLineAdvantageAtTimePoint(
			TrackedRace trackedRace, TimePoint timePoint) {
		double result = trackedRace.getStartLine(timePoint).getAdvantage()
				.getMeters();
		averageStartLineAdvantageByWind.add(result);
		return result;
	}

	private double getGeometricStartLineAdvantageAtTimePoint(
			TrackedRace trackedRace, TimePoint timePoint) {
		double result = trackedRace.getStartLine(timePoint)
				.getGeometricAdvantage().getMeters();
		averageStartLineAdvantageByGeometry.add(result);
		return result;
	}

	protected com.sap.sailing.domain.base.DomainFactory getBaseDomainFactory() {
		return baseDomainFactory;
	}

	private interface PositionAtTimeProvider {
		Position getPosition(TimePoint at);
	}

	protected WindDTO createWindDTOFromAlreadyAveraged(Wind wind,
			TimePoint requestTimepoint) {
		WindDTO windDTO = new WindDTO();
		windDTO.requestTimepoint = requestTimepoint.asMillis();
		windDTO.trueWindBearingDeg = wind.getBearing().getDegrees();
		windDTO.trueWindFromDeg = wind.getBearing().reverse().getDegrees();
		windDTO.trueWindSpeedInKnots = wind.getKnots();
		windDTO.trueWindSpeedInMetersPerSecond = wind.getMetersPerSecond();
		windDTO.dampenedTrueWindBearingDeg = wind.getBearing().getDegrees();
		windDTO.dampenedTrueWindFromDeg = wind.getBearing().reverse()
				.getDegrees();
		windDTO.dampenedTrueWindSpeedInKnots = wind.getKnots();
		windDTO.dampenedTrueWindSpeedInMetersPerSecond = wind
				.getMetersPerSecond();
		if (wind.getPosition() != null) {
			windDTO.position = new PositionDTO(wind.getPosition().getLatDeg(),
					wind.getPosition().getLngDeg());
		}
		if (wind.getTimePoint() != null) {
			windDTO.measureTimepoint = wind.getTimePoint().asMillis();
		}
		return windDTO;
	}

	private WindTrackInfoDTO createWindTrackInfoDTO(TimePoint from,
			long millisecondsStepWidth, int numberOfFixes,
			TrackedRace trackedRace, boolean onlyUpToNewestEvent,
			TimePoint newestEvent, WindSource windSource,
			PositionAtTimeProvider positionProvider) {
		WindTrack windTrack = trackedRace.getOrCreateWindTrack(windSource);
		WindTrackInfoDTO windTrackInfoDTO = new WindTrackInfoDTO();
		windTrackInfoDTO.windFixes = new ArrayList<WindDTO>();
		windTrackInfoDTO.dampeningIntervalInMilliseconds = windTrack
				.getMillisecondsOverWhichToAverageWind();
		TimePoint timePoint = from;

		for (int i = 0; i < numberOfFixes
				&& (!onlyUpToNewestEvent || (newestEvent != null && timePoint
						.before(newestEvent))); i++) {
			WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence = windTrack
					.getAveragedWindWithConfidence(
							positionProvider.getPosition(timePoint), timePoint);
			if (averagedWindWithConfidence != null) {
				WindDTO windDTO = createWindDTOFromAlreadyAveraged(
						averagedWindWithConfidence.getObject(), timePoint);
				windTrackInfoDTO.windFixes.add(windDTO);
			} else {
			}
			timePoint = new MillisecondsTimePoint(timePoint.asMillis()
					+ millisecondsStepWidth);
		}
		return windTrackInfoDTO;
	}

	@Override
	public void startAnalyisisRacesChanged(
			Map<String, List<StartAnalysisDTO>> startAnalysisDTOCompetitorMap) {
		startAnalysisDTOsForCompetitor = startAnalysisDTOCompetitorMap;
	}

	/**
	 * @param to
	 *            if <code>null</code>, data is returned up to end of race; if
	 *            the end of race is not known and <code>null</code> is used for
	 *            this parameter, <code>null</code> is returned.
	 * @param onlyUpToNewestEvent
	 *            if <code>true</code>, no wind data will be returned for time
	 *            points later than
	 *            {@link TrackedRace#getTimePointOfNewestEvent()
	 *            trackedRace.getTimePointOfNewestEvent()}. This is helpful in
	 *            case the client wants to populate a chart during live mode. If
	 *            <code>false</code>, the "best effort" readings are provided
	 *            for the time interval requested, no matter if based on any
	 *            sensor evidence or not, regardless of
	 *            {@link TrackedRace#getTimePointOfNewestEvent()
	 *            trackedRace.getTimePointOfNewestEvent()}.
	 */
	public WindInfoForRaceDTO getAveragedWindInfo(
			RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
			long resolutionInMilliseconds,
			Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent) {
		TrackedRace trackedRace = getExistingTrackedRace(raceIdentifier);
		WindInfoForRaceDTO result = null;
		if (trackedRace != null) {
			TimePoint fromTimePoint = from == null ? trackedRace
					.getStartOfTracking() : new MillisecondsTimePoint(from);
			TimePoint toTimePoint = to == null ? trackedRace.getEndOfRace() == null ? MillisecondsTimePoint
					.now().minus(trackedRace.getDelayToLiveInMillis())
					: trackedRace.getEndOfRace()
					: new MillisecondsTimePoint(to);
			if (fromTimePoint != null && toTimePoint != null) {
				int numberOfFixes = (int) ((toTimePoint.asMillis() - fromTimePoint
						.asMillis()) / resolutionInMilliseconds);
				result = getAveragedWindInfo(fromTimePoint,
						resolutionInMilliseconds, numberOfFixes,
						windSourceTypeNames, trackedRace, onlyUpToNewestEvent, /* includeCombinedWindForAllLegMiddles */
						false);
			}
		}
		return result;
	}

	public TrackedRace getExistingTrackedRace(
			RegattaAndRaceIdentifier regattaNameAndRaceName) {
		return null;
	}

	/**
	 * @param onlyUpToNewestEvent
	 *            if <code>true</code>, no wind data will be returned for time
	 *            points later than
	 *            {@link TrackedRace#getTimePointOfNewestEvent()
	 *            trackedRace.getTimePointOfNewestEvent()}. This is helpful in
	 *            case the client wants to populate a chart during live mode. If
	 *            <code>false</code>, the "best effort" readings are provided
	 *            for the time interval requested, no matter if based on any
	 *            sensor evidence or not, regardless of
	 *            {@link TrackedRace#getTimePointOfNewestEvent()
	 *            trackedRace.getTimePointOfNewestEvent()}.
	 * @param includeCombinedWindForAllLegMiddles
	 *            if <code>true</code>, the result will return non-
	 *            <code>null</code> results for calls to
	 *            {@link WindInfoForRaceDTO#getCombinedWindOnLegMiddle(int)}.
	 */
	private WindInfoForRaceDTO getAveragedWindInfo(TimePoint from,
			long millisecondsStepWidth, int numberOfFixes,
			Collection<String> windSourceTypeNames, TrackedRace trackedRace,
			boolean onlyUpToNewestEvent,
			boolean includeCombinedWindForAllLegMiddles) {
		WindInfoForRaceDTO result = null;
		if (trackedRace != null) {
			TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
			result = new WindInfoForRaceDTO();
			result.raceIsKnownToStartUpwind = trackedRace
					.raceIsKnownToStartUpwind();
			List<WindSource> windSourcesToExclude = new ArrayList<WindSource>();
			for (WindSource windSourceToExclude : trackedRace
					.getWindSourcesToExclude()) {
				windSourcesToExclude.add(windSourceToExclude);
			}
			result.windSourcesToExclude = windSourcesToExclude;
			Map<WindSource, WindTrackInfoDTO> windTrackInfoDTOs = new HashMap<WindSource, WindTrackInfoDTO>();
			result.windTrackInfoByWindSource = windTrackInfoDTOs;
			List<WindSource> windSourcesToDeliver = new ArrayList<WindSource>();
			Util.addAll(trackedRace.getWindSources(), windSourcesToDeliver);
			final WindSource combinedWindSource = new WindSourceImpl(
					WindSourceType.COMBINED);
			windSourcesToDeliver.add(combinedWindSource);
			for (WindSource windSource : windSourcesToDeliver) {
				// TODO consider parallelizing
				if (windSourceTypeNames == null
						|| windSourceTypeNames.contains(windSource.getType()
								.name())) {
					WindTrackInfoDTO windTrackInfoDTO = createWindTrackInfoDTO(
							from, millisecondsStepWidth, numberOfFixes,
							trackedRace, onlyUpToNewestEvent, newestEvent,
							windSource, new PositionAtTimeProvider() {
								@Override
								public Position getPosition(TimePoint at) {
									return null;
								}
							});
					windTrackInfoDTOs.put(windSource, windTrackInfoDTO);
				}
			}
			if (includeCombinedWindForAllLegMiddles) {
				int zeroBasedLegNumber = 0;
				for (final TrackedLeg trackedLeg : trackedRace.getTrackedLegs()) {
					WindTrackInfoDTO windTrackInfoForLegMiddle = createWindTrackInfoDTO(
							from, millisecondsStepWidth, numberOfFixes,
							trackedRace, onlyUpToNewestEvent, newestEvent,
							combinedWindSource, new PositionAtTimeProvider() {
								@Override
								public Position getPosition(TimePoint at) {
									return trackedLeg.getMiddleOfLeg(at);
								}
							});
					result.addWindOnLegMiddle(zeroBasedLegNumber,
							windTrackInfoForLegMiddle);
					zeroBasedLegNumber++;
				}
			}
		}
		return result;
	}
}