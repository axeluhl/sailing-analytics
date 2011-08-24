package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
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

    void getLeaderboardByName(String leaderboardName, Date date, AsyncCallback<LeaderboardDAO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void createLeaderboard(String leaderboardName, int[] discardThresholds, AsyncCallback<Void> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);

}
