package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.server.api.DetailType;
import com.sap.sailing.server.api.EventAndRaceIdentifier;
import com.sap.sailing.server.api.EventIdentifier;
import com.sap.sailing.server.api.RaceIdentifier;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync {
    void listEvents(AsyncCallback<List<EventDAO>> callback);

    /**
     * The string returned in the callback's pair is the common event name
     */
    void listTracTracRacesInEvent(String eventJsonURL, AsyncCallback<Pair<String, List<TracTracRaceRecordDAO>>> callback);

    /**
     * @param liveURI may be <code>null</code> or the empty string in which case the server will
     * use the {@link TracTracRaceRecordDAO#liveURI} from the <code>rr</code> race record.
     * @param storedURImay be <code>null</code> or the empty string in which case the server will
     * use the {@link TracTracRaceRecordDAO#storedURI} from the <code>rr</code> race record.
     */
    void track(TracTracRaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination,
            AsyncCallback<Void> callback);

    void getPreviousTracTracConfigurations(AsyncCallback<List<TracTracConfigurationDAO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(EventIdentifier eventIdentifier, AsyncCallback<Void> callback);

    void stopTrackingRace(EventAndRaceIdentifier raceIdentifier, AsyncCallback<Void> asyncCallback);
    
    /**
     * Untracks the race and removes it from the event. It will also be removed in all leaderboards
     * @param eventAndRaceidentifier The identifier for the event name, and the race name to remove
     * @throws Exception
     */
    void removeAndUntrackRace(EventAndRaceIdentifier eventAndRaceidentifier, AsyncCallback<Void> callback);

    /**
     * @param windSources if <code>null</code>, information about all available wind sources will be provided
     */
    void getWindInfo(RaceIdentifier raceIdentifier, Date from, Date to, WindSource[] windSources,
            AsyncCallback<WindInfoForRaceDAO> callback);

    /**
     * @param windSources
     *            if <code>null</code>, data from all available wind sources will be returned, otherwise only from those
     *            whose {@link WindSource} name is contained in the <code>windSources</code> collection.
     */
    void getWindInfo(RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth, int numberOfFixes,
            double latDeg, double lngDeg, Collection<String> windSources,
            AsyncCallback<WindInfoForRaceDAO> callback);

    void setWind(RaceIdentifier raceIdentifier, WindDAO wind, AsyncCallback<Void> callback);
    
    void removeWind(RaceIdentifier raceIdentifier, WindDAO windDAO, AsyncCallback<Void> callback);

    /**
     * @param from
     *            for the list of competitors provided as keys of this map, requests the GPS fixes starting with the
     *            date provided as value
     * @param to
     *            for the list of competitors provided as keys (expected to be equal to the set of competitors used as
     *            keys in the <code>from</code> parameter, requests the GPS fixes up to but excluding the date provided
     *            as value
     * @param extrapolate
     *            if <code>true</code> and no position is known for <code>date</code>, the last entry returned in the
     *            list of GPS fixes will be obtained by extrapolating from the competitors last known position before
     *            <code>date</code> and the estimated speed.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    void getBoatPositions(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            boolean extrapolate, AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>> callback);

    void getMarkPositions(RaceIdentifier raceIdentifier, Date date, AsyncCallback<List<MarkDAO>> asyncCallback);

    void getQuickRanks(RaceIdentifier raceIdentifier, Date date, AsyncCallback<List<QuickRankDAO>> callback);

    void setWindSource(RaceIdentifier raceIdentifier, String windSourceName, boolean raceIsKnownToStartUpwind, AsyncCallback<Void> callback);

    /**
     * Returns a {@link LeaderboardDAO} will information about all races, their points and competitor display names
     * filled in. The column details are filled for the races whose named are provided in
     * <code>namesOfRacesForWhichToLoadLegDetails</code>.
     * 
     * @param namesOfRacesForWhichToLoadLegDetails
     *            if <code>null</code>, no {@link LeaderboardEntryDAO#legDetails leg details} will be present in the
     *            result ({@link LeaderboardEntryDAO#legDetails} will be <code>null</code> for all
     *            {@link LeaderboardEntryDAO} objects contained). Otherwise, the {@link LeaderboardEntryDAO#legDetails}
     *            list will contain one entry per leg of the race {@link Course} for those race columns whose
     *            {@link RaceInLeaderboard#getName() name} is contained in
     *            <code>namesOfRacesForWhichToLoadLegDetails</code>. For all other columns,
     *            {@link LeaderboardEntryDAO#legDetails} is <code>null</code>.
     */
    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRacesForWhichToLoadLegDetails, AsyncCallback<LeaderboardDAO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    /**
     * Creates a {@link LeaderboardDAO} for each leaderboard known by the server and fills in the name, race master data
     * in the form of {@link RaceInLeaderboardDAO}s, whether or not there are {@link LeaderboardDAO#hasCarriedPoints carried points}
     * and the {@link LeaderboardDAO#discardThresholds discarding thresholds} for the leaderboard. No data about the points
     * is filled into the result object. No data about the competitor display names is filled in; instead, an empty map
     * is used for {@link LeaderboardDAO#competitorDisplayNames}.
     */
    void getLeaderboards(AsyncCallback<List<LeaderboardDAO>> callback);
    
    void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThreasholds,
            AsyncCallback<Void> callback);

    /**
     * Creates a leaderboard with the name specified by <code>leaderboardName</code> and the initial discarding thesholds
     * as specified by <code>discardThresholds</code>. The leaderboard returned has the leaderboard name and the master
     * data about the race columns filled in, but no details about the race points. As such, the result structure
     * equals that of the result of {@link #getLeaderboards(AsyncCallback)}.
     */
    void createLeaderboard(String leaderboardName, int[] discardThresholds, AsyncCallback<LeaderboardDAO> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);
    
    void renameLeaderboard(String leaderboardName, String newLeaderboardName, AsyncCallback<Void> asyncCallback);

    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace,
            AsyncCallback<Void> callback);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName, AsyncCallback<Void> callback);

    void removeLeaderboardColumn(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    /**
     * @param asyncCallback receives <code>true</code> if connecting was successful
     */
    void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            RaceIdentifier raceIdentifier, AsyncCallback<Boolean> asyncCallback);

    void getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName,
            AsyncCallback<Pair<String, String>> callback);

    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName,
            AsyncCallback<Void> callback);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorName, Integer carriedPoints, AsyncCallback<Void> callback);

    void updateLeaderboardMaxPointsReason(String leaderboardName, String competitorName, String raceColumnName,
            String maxPointsReasonAsString, Date date, AsyncCallback<Pair<Integer, Integer>> asyncCallback);

    void updateLeaderboardScoreCorrection(String leaderboardName, String competitorName, String raceName,
            Integer correctedScore, Date date, AsyncCallback<Pair<Integer, Integer>> asyncCallback);

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorName, String displayName,
            AsyncCallback<Void> callback);

	void moveLeaderboardColumnUp(String leaderboardName, String columnName,
			AsyncCallback<Void> callback);

	void moveLeaderboardColumnDown(String leaderboardName, String columnName,
			AsyncCallback<Void> callback);

	void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace,
			AsyncCallback<Void> callback);

    void getPreviousSwissTimingConfigurations(AsyncCallback<List<SwissTimingConfigurationDAO>> asyncCallback);

    void listSwissTimingRaces(String hostname, int port, boolean canSendRequests,
            AsyncCallback<List<SwissTimingRaceRecordDAO>> asyncCallback);

    void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests, AsyncCallback<Void> asyncCallback);

    void trackWithSwissTiming(SwissTimingRaceRecordDAO rr, String hostname, int port, boolean canSendRequests,
            boolean trackWind, boolean correctWindByDeclination, AsyncCallback<Void> asyncCallback);

    void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage, AsyncCallback<Void> callback);
    
    /**
     * Requests the computation of the {@link LeaderboardDAO} for <code>leaderboardName</code> <code>times</code> times.
     * The date used for the {@link #getLeaderboardByName(String, Date, Collection, AsyncCallback)} call is iterated
     * in 10ms time steps, going backwards from "now." For all races, all details are requested.
     */
    void stressTestLeaderboardByName(String leaderboardName, int times, AsyncCallback<Void> callback);

    void getCountryCodes(AsyncCallback<String[]> callback);

    /**
     * This method computes the in {@code dataType} selected data for the in {@code race} specified race
     * for all competitors returned by {@link CompetitorsAndTimePointsDAO#getCompetitor()} at the timepoints 
     * returned by {@link CompetitorsAndTimePointsDAO#getTimePoints()}.
     * The returned {@link CompetitorInRaceDAO} contains the values for the timepoints as well as the values for the markpassings.
     * 
     * @see DetailType
     * 
     * @throws NullPointerException Thrown if any of the parameters is null.
     * 
     * @param race 
     * @param competitorsAndTimePointsDAO An object that contains the competitors and timepoints.
     * @param dataType The type of data that should be computed (eg {@link DetailType#WINDWARD_DISTANCE_TO_OVERALL_LEADER}).
     * @param callback An AsyncCallback that returns the computed data as a parameter in the {@link AsyncCallback#onSuccess(Object)} method.
     */
    void getCompetitorRaceData(RaceIdentifier race,
			CompetitorsAndTimePointsDAO competitorsAndTimePointsDAO,
			DetailType dataType, AsyncCallback<CompetitorInRaceDAO> callback);
    
    void getDouglasPoints(RaceIdentifier raceIdentifier, Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            double meters, AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>> callback);

    void getManeuvers(RaceIdentifier raceIdentifier, Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            AsyncCallback<Map<CompetitorDAO, List<ManeuverDAO>>> callback);

    /**
     * For the race identified by <code>race</code> computes <code>steps</code> equidistant time points starting at a
     * few seconds before the race starts, up to the end of the race. The result describes the race's competitors, their
     * mark passing times, the race start time and the list of time points according to the above specification.
     */
    void getCompetitorsAndTimePoints(RaceIdentifier race, int steps, AsyncCallback<CompetitorsAndTimePointsDAO> callback);
}
