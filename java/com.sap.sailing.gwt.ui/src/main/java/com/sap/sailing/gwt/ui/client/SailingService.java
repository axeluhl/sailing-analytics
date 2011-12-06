package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDAO;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;
import com.sap.sailing.gwt.ui.shared.DetailType;
import com.sap.sailing.gwt.ui.shared.EventAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventIdentifier;
import com.sap.sailing.gwt.ui.shared.GPSFixDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.ManeuverDAO;
import com.sap.sailing.gwt.ui.shared.MarkDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.QuickRankDAO;
import com.sap.sailing.gwt.ui.shared.RaceIdentifier;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDAO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;

/**
 * The client side stub for the RPC service. Usually, when a <code>null</code> date is passed to
 * the time-dependent service methods, an empty (non-<code>null</code>) result is returned.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDAO> getPreviousTracTracConfigurations() throws Exception;
    
    List<EventDAO> listEvents();

    Pair<String, List<TracTracRaceRecordDAO>> listTracTracRacesInEvent(String eventJsonURL) throws Exception;

    void track(TracTracRaceRecordDAO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination) throws Exception;

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) throws Exception;

    void stopTrackingEvent(EventIdentifier eventIdentifier) throws Exception;

    void stopTrackingRace(EventAndRaceIdentifier eventAndRaceIdentifier) throws Exception;

    WindInfoForRaceDAO getWindInfo(RaceIdentifier raceIdentifier, Date from, Date to,
            boolean includeTrackBasedWindEstimation);

    void setWind(RaceIdentifier raceIdentifier, WindDAO wind);

    Map<CompetitorDAO, List<GPSFixDAO>> getBoatPositions(RaceIdentifier raceIdentifier, Map<CompetitorDAO, Date> from,
            Map<CompetitorDAO, Date> to, boolean extrapolate);

    List<MarkDAO> getMarkPositions(RaceIdentifier raceIdentifier, Date date);

    List<QuickRankDAO> getQuickRanks(RaceIdentifier raceIdentifier, Date date) throws Exception;

    WindInfoForRaceDAO getWindInfo(RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, boolean includeTrackBasedWindEstimation) throws Exception;

    void setWindSource(RaceIdentifier raceIdentifier, String windSourceName);

    void removeWind(RaceIdentifier raceIdentifier, WindDAO windDAO);

    public List<String> getLeaderboardNames() throws Exception;
    
    LeaderboardDAO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRacesForWhichToLoadLegDetails) throws Exception;

    List<LeaderboardDAO> getLeaderboards();
    
    LeaderboardDAO getLeaderboardByName(String leaderboardName);

    void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThreasholds);

    void createLeaderboard(String leaderboardName, int[] discardThresholds);

    void removeLeaderboard(String leaderboardName);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName);

    void removeLeaderboardColumn(String leaderboardName, String columnName);
    
    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace);
    
    void moveLeaderboardColumnUp(String leaderboardName, String columnName);
    
    void moveLeaderboardColumnDown(String leaderboardName, String columnName);

    void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            RaceIdentifier raceIdentifier);
    
    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName);
    
    Pair<String, String> getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorName, Integer carriedPoints);

    /**
     * @return the new net points in {@link Pair#getA()} and the new total points in {@link Pair#getB()}
     */
    Pair<Integer, Integer> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorName,
            String raceColumnName, String maxPointsReasonAsString, Date date) throws Exception;

    Pair<Integer, Integer> updateLeaderboardScoreCorrection(String leaderboardName, String competitorName,
            String raceName, Integer correctedScore, Date date) throws Exception;

    LeaderboardEntryDAO getLeaderboardEntry(String leaderboardName, String competitorName, String raceName, Date date) throws Exception;

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorName, String displayName);
    
    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace);

    List<SwissTimingConfigurationDAO> getPreviousSwissTimingConfigurations();

    List<SwissTimingRaceRecordDAO> listSwissTimingRaces(String hostname, int port, boolean canSendRequests) throws Exception;

    void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests);

    void trackWithSwissTiming(SwissTimingRaceRecordDAO rr, String hostname, int port, boolean canSendRequests,
            boolean trackWind, boolean correctWindByDeclination) throws Exception;
    
    void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) throws IllegalArgumentException;

    void stressTestLeaderboardByName(String leaderboardName, int times) throws Exception;
    
    String[] getCountryCodes();
    
    List<Pair<CompetitorDAO, Double[]>> getCompetitorRaceData(RaceIdentifier race, CompetitorsAndTimePointsDAO competitorAndTimePointsDAO, DetailType dataType) throws Exception;
    
    CompetitorsAndTimePointsDAO getCompetitorsAndTimePoints(RaceIdentifier race, int steps);
    
    Map<CompetitorDAO, List<GPSFixDAO>> getDouglasPoints(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to, double meters);

    Map<CompetitorDAO, List<ManeuverDAO>> getManeuvers(RaceIdentifier raceIdentifier,
            Map<CompetitorDAO, Date> from, Map<CompetitorDAO, Date> to) throws Exception;
}
