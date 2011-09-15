package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync {
    void listEvents(AsyncCallback<List<EventDAO>> callback);

    void listRacesInEvent(String eventJsonURL, AsyncCallback<List<RaceRecordDAO>> callback);

    void track(RaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination,
            AsyncCallback<Void> callback);

    void getPreviousConfigurations(AsyncCallback<List<TracTracConfigurationDAO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(String eventName, AsyncCallback<Void> callback);

    void stopTrackingRace(String eventName, String raceName, AsyncCallback<Void> asyncCallback);

    void getWindInfo(String eventName, String raceName, Date from, Date to,
            AsyncCallback<WindInfoForRaceDAO> callback);

    void getWindInfo(String eventName, String raceName, Date from, long millisecondsStepWidth, int numberOfFixes,
            double latDeg, double lngDeg, AsyncCallback<WindInfoForRaceDAO> callback);

    void setWind(String eventName, String raceName, WindDAO wind, AsyncCallback<Void> callback);
    
    void removeWind(String eventName, String raceName, WindDAO wind, AsyncCallback<Void> callback);

    /**
     * @param tailLengthInMilliseconds
     *            the time interval to go back in time, starting at <code>date</code>. The result will contain all fixes
     *            known for the respective competitor starting at <code>date-tailLengthInMilliseconds</code> up to and
     *            including <code>date</code>.
     * @param extrapolate
     *            if <code>true</code> and no position is known for <code>date</code>, the last entry returned in the
     *            list of GPS fixes will be obtained by extrapolating from the competitors last known position before
     *            <code>date</code> and the estimated speed.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    void getBoatPositions(String eventName, String raceName, Date date, long tailLengthInMilliseconds,
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
}
