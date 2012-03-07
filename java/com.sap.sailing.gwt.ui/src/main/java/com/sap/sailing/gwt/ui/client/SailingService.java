package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.EventIdentifier;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MultiCompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;

/**
 * The client side stub for the RPC service. Usually, when a <code>null</code> date is passed to
 * the time-dependent service methods, an empty (non-<code>null</code>) result is returned.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception;
    
    List<EventDTO> listEvents();

    Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL) throws Exception;

    void track(TracTracRaceRecordDTO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination) throws Exception;

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI) throws Exception;

    void stopTrackingEvent(EventIdentifier eventIdentifier) throws Exception;

    void stopTrackingRace(EventAndRaceIdentifier eventAndRaceIdentifier) throws Exception;
    
    void removeAndUntrackRace(EventAndRaceIdentifier eventAndRaceidentifier) throws Exception;

    WindInfoForRaceDTO getWindInfo(RaceIdentifier raceIdentifier, Date from, Date to, WindSource[] windSources);

    void setWind(RaceIdentifier raceIdentifier, WindDTO wind);

    Map<CompetitorDTO, List<GPSFixDTO>> getBoatPositions(RaceIdentifier raceIdentifier, Map<CompetitorDTO, Date> from,
            Map<CompetitorDTO, Date> to, boolean extrapolate) throws NoWindException;

    RaceTimesInfoDTO getRaceTimesInfo(RaceIdentifier raceIdentifier);
    
    List<MarkDTO> getMarkPositions(RaceIdentifier raceIdentifier, Date date);

    List<QuickRankDTO> getQuickRanks(RaceIdentifier raceIdentifier, Date date) throws NoWindException;

    WindInfoForRaceDTO getWindInfo(RaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSources) throws NoWindException;

    void removeWind(RaceIdentifier raceIdentifier, WindDTO windDTO);

    public List<String> getLeaderboardNames() throws Exception;
    
    LeaderboardDTO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRacesForWhichToLoadLegDetails)
            throws NoWindException;

    List<LeaderboardDTO> getLeaderboards();
    
    List<LeaderboardDTO> getLeaderboardsByEvent(EventDTO event);
    
    void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThreasholds);

    LeaderboardDTO createLeaderboard(String leaderboardName, int[] discardThresholds);

    void removeLeaderboard(String leaderboardName);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName);

    void removeLeaderboardColumn(String leaderboardName, String columnName);
    
    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace);
    
    void moveLeaderboardColumnUp(String leaderboardName, String columnName);
    
    void moveLeaderboardColumnDown(String leaderboardName, String columnName);

    boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            RaceIdentifier raceIdentifier);
    
    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName);
    
    Pair<String, String> getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorName, Integer carriedPoints);

    /**
     * @return the new net points in {@link Pair#getA()} and the new total points in {@link Pair#getB()}
     */
    Pair<Integer, Integer> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorName,
            String raceColumnName, String maxPointsReasonAsString, Date date) throws NoWindException;

    Pair<Integer, Integer> updateLeaderboardScoreCorrection(String leaderboardName, String competitorName,
            String raceName, Integer correctedScore, Date date) throws NoWindException;

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorName, String displayName);
    
    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace);

    List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations();

    List<SwissTimingRaceRecordDTO> listSwissTimingRaces(String hostname, int port, boolean canSendRequests) throws Exception;

    void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests);

    void trackWithSwissTiming(SwissTimingRaceRecordDTO rr, String hostname, int port, boolean canSendRequests,
            boolean trackWind, boolean correctWindByDeclination) throws Exception;
    
    void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) throws IllegalArgumentException;

    void stressTestLeaderboardByName(String leaderboardName, int times) throws Exception;
    
    String[] getCountryCodes();
    
    Map<CompetitorDTO, List<GPSFixDTO>> getDouglasPoints(RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, double meters) throws NoWindException;

    Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to) throws NoWindException;

    List<LeaderboardDTO> getLeaderboardsByRace(RaceDTO race);
    
    List<LeaderboardGroupDTO> getLeaderboardGroups();
    
    LeaderboardGroupDTO getLeaderboardGroupByName(String groupName);
    
    void renameLeaderboardGroup(String oldName, String newName);
    
    void removeLeaderboardGroup(String groupName);
    
    LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description);
    
    void updateLeaderboardGroup(String oldName, String newName, String description, List<LeaderboardDTO> leaderboards);

    MultiCompetitorRaceDataDTO getCompetitorsRaceData(RaceIdentifier race, List<Pair<Date, CompetitorDTO>> competitors,
            Date toDate, long stepSize, DetailType detailType) 
            throws NoWindException;

    void setRaceIsKnownToStartUpwind(RaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind);
}
