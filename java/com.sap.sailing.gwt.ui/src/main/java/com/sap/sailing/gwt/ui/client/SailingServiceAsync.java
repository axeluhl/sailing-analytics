package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorWithRaceDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

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

    void stopTrackingEvent(String eventName, AsyncCallback<Void> callback);

    void stopTrackingRace(String eventName, String raceName, AsyncCallback<Void> asyncCallback);

    /**
     * Obtains wind data for an event/race based on the wind data as recorded in the currently
     * selected wind track for the race, with raw and dampened numbers, for the time stamps as
     * provided by the recording. The interval for which to retrieve wind data must be specified
     * using <code>from</code> and <code>to</code>.
     * 
     * @param includeTrackBasedWindEstimation if <code>true</code>, for each time point for which an
     * {@link WindSource#EXPEDITION} estimation exists for the event/race requested, a wind estimation
     * based on the GPS tracks will be performed and included in the result. In this case, the
     * result will contain a so far non-existing wind source name "ESTIMATION".
     */
    void getWindInfo(String eventName, String raceName, Date from, Date to,
            boolean includeTrackBasedWindEstimation, AsyncCallback<WindInfoForRaceDAO> callback);

    /**
     * Obtains wind information starting at <code>from</code> and stepping in intervals as specified by
     * <code>millisecondsStepWidth</code>, delivering <code>numberOfFixes</code> fixes. Those don't have to
     * correspond exactly with when wind measurements were taken; instead, the selected race's selected wind
     * source is interpolated to estimate the wind for the time/position requested.
     */
    void getWindInfo(String eventName, String raceName, Date from, long millisecondsStepWidth, int numberOfFixes,
            double latDeg, double lngDeg, boolean includeTrackBasedWindEstimation, AsyncCallback<WindInfoForRaceDAO> callback);

    void setWind(String eventName, String raceName, WindDAO wind, AsyncCallback<Void> callback);
    
    void removeWind(String eventName, String raceName, WindDAO wind, AsyncCallback<Void> callback);

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
    void getBoatPositions(String eventName, String raceName,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to,
            boolean extrapolate, AsyncCallback<Map<CompetitorDAO, List<GPSFixDAO>>> callback);

    void getMarkPositions(String eventName, String raceName, Date date, AsyncCallback<List<MarkDAO>> asyncCallback);

    void getQuickRanks(String eventName, String raceName, Date date, AsyncCallback<List<QuickRankDAO>> callback);

    void setWindSource(String eventName, String raceName, String windSourceName, AsyncCallback<Void> callback);

    /**
     * @param namesOfRacesForWhichToLoadLegDetails
     *            if <code>null</code>, no {@link LeaderboardEntryDAO#legDetails leg details} will be present in the
     *            result ({@link LeaderboardEntryDAO#legDetails} will be <code>null</code> for all
     *            {@link LeaderboardEntryDAO} objects contained). Otherwise, the {@link LeaderboardEntryDAO#legDetails}
     *            list will contain one entry per leg of the race {@link Course} for those race columns whose
     *            {@link RaceInLeaderboard#getName() name} is contained in <code>namesOfRacesForWhichToLoadLegDetails</code>.
     *            For all other columns, {@link LeaderboardEntryDAO#legDetails} is <code>null</code>.
     */
    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRacesForWhichToLoadLegDetails, AsyncCallback<LeaderboardDAO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void createLeaderboard(String leaderboardName, int[] discardThresholds, AsyncCallback<Void> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName, AsyncCallback<Void> asyncCallback);

    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace,
            AsyncCallback<Void> callback);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName, AsyncCallback<Void> callback);

    void removeLeaderboardColumn(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    void connectTrackedRaceToLeaderboardColumn(String selectedLeaderboardName, String selectedRaceColumnName,
            String name, String name2, AsyncCallback<Void> asyncCallback);

    void getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName,
            AsyncCallback<Pair<String, String>> callback);

    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName,
            AsyncCallback<Void> callback);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorName, Integer carriedPoints, AsyncCallback<Void> callback);

    void updateLeaderboardMaxPointsReason(String leaderboardName, String competitorName, String raceColumnName,
            String maxPointsReasonAsString, Date date, AsyncCallback<Pair<Integer, Integer>> asyncCallback);

    void updateLeaderboardScoreCorrection(String leaderboardName, String competitorName, String raceName,
            Integer correctedScore, Date date, AsyncCallback<Pair<Integer, Integer>> asyncCallback);

    void getLeaderboardEntry(String leaderboardName, String competitorName, String raceName, Date date,
            AsyncCallback<LeaderboardEntryDAO> callback);

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

    void getCompetitorRaceData(String eventName, String raceName, List<CompetitorDAO> competitors, int timeStep,
            AsyncCallback<CompetitorWithRaceDAO[][]> callback);
}
