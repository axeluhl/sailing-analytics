package com.sap.sailing.gwt.ui.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sailing.datamining.shared.DataMiningSerializationDummy;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.MasterDataImportObjectCreationCount;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;

/**
 * The client side stub for the RPC service. Usually, when a <code>null</code> date is passed to
 * the time-dependent service methods, an empty (non-<code>null</code>) result is returned.
 */
@RemoteServiceRelativePath("sailing")
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception;
    
    List<RegattaDTO> getRegattas();

    List<EventDTO> getEvents();

    Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws Exception;

    void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI,
            String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow, String tracTracUsername, String tracTracPassword) throws Exception;

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs, String hostname, int port,
            boolean canSendRequests, boolean trackWind, boolean correctWindByDeclination) throws Exception;
    
    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) throws Exception;

    void stopTrackingEvent(RegattaIdentifier eventIdentifier) throws Exception;

    void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> racesToStopTracking) throws Exception;
    
    void removeAndUntrackRaces(Iterable<RegattaAndRaceIdentifier> regattaNamesAndRaceNames);

    WindInfoForRaceDTO getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources);

    void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO wind);

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames) throws NoWindException;

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to, long resolutionInMilliseconds,
            Collection<String> windSourceTypeNames);

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSources) throws NoWindException;

    CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date, Map<String, Date> fromPerCompetitorIdAsString,
            Map<String, Date> toPerCompetitorIdAsString, boolean extrapolate) throws NoWindException;
    
    RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier);
    
    List<RaceTimesInfoDTO> getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers);
    
    CoursePositionsDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date);

    RaceCourseDTO getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date);

    void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO);

    public List<String> getLeaderboardNames();
    
    IncrementalOrFullLeaderboardDTO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, String previousLeaderboardId) throws Exception;

    List<StrippedLeaderboardDTO> getLeaderboards();
    
    List<StrippedLeaderboardDTO> getLeaderboardsByRegatta(RegattaDTO regatta);
    
    List<StrippedLeaderboardDTO> getLeaderboardsByEvent(EventDTO event);
    
    StrippedLeaderboardDTO updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName, int[] newDiscardingThreasholds, UUID newCourseAreaId);

    StrippedLeaderboardDTO createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringSchemeType scoringSchemeType, UUID courseAreaId);

    StrippedLeaderboardDTO createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds);

    void removeLeaderboard(String leaderboardName);

    void removeLeaderboards(Collection<String> leaderboardNames);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName);

    void removeLeaderboardColumn(String leaderboardName, String columnName);
    
    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace);
    
    void moveLeaderboardColumnUp(String leaderboardName, String columnName);
    
    void moveLeaderboardColumnDown(String leaderboardName, String columnName);
    
    RegattaDTO createRegatta(String regattaName, String boatClassName,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal, boolean persistent,
            ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId);
    
    void removeRegatta(RegattaIdentifier regattaIdentifier);

    void removeRegattas(Collection<RegattaIdentifier> regattas);
    
    void updateRegatta(RegattaIdentifier regattaIdentifier, UUID defaultCourseAreaUuid, RegattaConfigurationDTO regattaConfiguration);
    
    List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames);

    void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, boolean isMedal,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstRaceIsNonDiscardableCarryForward, boolean hasSplitFleetScore, List<FleetDTO> fleets);

    RaceColumnInSeriesDTO addRaceColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName);

    void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames);

    void removeRaceColumnFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName);

    void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName);

    void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName);

    boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName,
            String fleetName, RegattaAndRaceIdentifier raceIdentifier);
    
    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName);
    
    Map<String, RegattaAndRaceIdentifier> getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints);

    /**
     * @return the new net points in {@link Pair#getA()} and the new total points in {@link Pair#getB()} for time point
     * <code>date</code> after the max points reason has been updated to <code>maxPointsReasonAsString</code>.
     */
    Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString,
            String raceColumnName, MaxPointsReason maxPointsReason, Date date) throws NoWindException;

    Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName, String competitorIdAsString,
            String columnName, Double correctedScore, Date date) throws NoWindException;

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAsString, String displayName);
    
    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace);

    List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations();

    List<SwissTimingRaceRecordDTO> listSwissTimingRaces(String hostname, int port, boolean canSendRequests) throws Exception;

    void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests);

    void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage) throws IllegalArgumentException;

    String[] getCountryCodes();
    
    Map<CompetitorDTO, List<GPSFixDTO>> getDouglasPoints(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to, double meters) throws NoWindException;

    Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to) throws NoWindException;

    List<StrippedLeaderboardDTO> getLeaderboardsByRaceAndRegatta(RaceDTO race, RegattaIdentifier regattaIdentifier);
    
    List<LeaderboardGroupDTO> getLeaderboardGroups(boolean withGeoLocationData);
    
    LeaderboardGroupDTO getLeaderboardGroupByName(String groupName, boolean withGeoLocationData);
    
    void renameLeaderboardGroup(String oldName, String newName);
    
    void removeLeaderboardGroups(Set<String> groupNames);
    
    LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description,
            boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType);
    
    void updateLeaderboardGroup(String oldName, String newName, String description, List<String> leaderboardNames,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

    CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSizeInMs, DetailType detailType, String leaderboardGroupName, String leaderboardName) throws NoWindException;

    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind);

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude);
    
    ReplicationStateDTO getReplicaInfo();

    void startReplicatingFromMaster(String messagingHost, String masterHost, String exchangeName, int servletPort, int messagingPort) throws Exception;

    void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs);

    void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs);

    void updateEvent(String eventName, UUID eventId, VenueDTO venue, String publicationUrl, boolean isPublic,
            List<String> regattaNames);

    EventDTO createEvent(String eventName, String venueName, String publicationUrl, boolean isPublic, List<String> courseAreaNames);

    void removeEvent(UUID eventId);

    void removeEvents(Collection<UUID> eventIds);

    void renameEvent(UUID eventId, String newName);

    EventDTO getEventByName(String eventName);
    
    EventDTO getEventById(UUID id);

    Iterable<String> getScoreCorrectionProviderNames();

    ScoreCorrectionProviderDTO getScoreCorrectionsOfProvider(String providerName) throws Exception;

    RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName, String boatClassName,
            Date timePointWhenResultPublished) throws Exception;

    void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates) throws NoWindException;

    WindInfoForRaceDTO getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier);

    List<String> getResultImportUrls(String resultProviderName);

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove) throws Exception;

    void addResultImportUrl(String resultProviderName, String url) throws Exception;

    Void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity,
            String comment);

    List<String> getUrlResultProviderNames();
    
    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<Pair<ControlPointDTO, PassingInstruction>> controlPoints);

    void addColumnsToLeaderboard(String leaderboardName, List<Pair<String, Boolean>> columnsToAdd);

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove);

    StrippedLeaderboardDTO getLeaderboard(String leaderboardName);

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed);

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor);

    List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl);

    List<Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName,
            Date date, DetailType detailType) throws Exception;

    List<String> getOverallLeaderboardNamesContaining(String leaderboardName);

    List<SwissTimingArchiveConfigurationDTO> getPreviousSwissTimingArchiveConfigurations();

    void storeSwissTimingArchiveConfiguration(String swissTimingUrl);

    PolarSheetGenerationResponse generatePolarSheetForRaces(List<RegattaAndRaceIdentifier> selectedRaces,
            PolarSheetGenerationSettings settings, String name) throws Exception;
    
    CourseAreaDTO createCourseArea(UUID eventId, String courseAreaName);
    
    List<Pair<String, String>> getLeaderboardsNamesOfMetaleaderboard(String metaLeaderboardName);

    Pair<String, LeaderboardType> checkLeaderboardName(String leaderboardName);

    List<RaceGroupDTO> getRegattaStructureForEvent(UUID eventId);
    
    List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreas,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay);
    
    String getBuildVersion();

    void stopReplicatingFromMaster();

    void stopAllReplicas();

    void stopSingleReplicaInstance(String identifier);

    void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet);

    RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet);

    String getRaceLogEvent(String leaderboardName, String raceColumnName, String fleetName, Serializable eventId);

    MasterDataImportObjectCreationCount importMasterData(String host, String[] groupNames, boolean override, boolean compress);
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception;

    Iterable<CompetitorDTO> getCompetitors();
    
    DataMiningSerializationDummy pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy();
    
    Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName);

    CompetitorDTO updateCompetitor(CompetitorDTO competitor);

    void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors);
    
    List<DeviceConfigurationMatcherDTO> getDeviceConfigurationMatchers();
    
    DeviceConfigurationDTO getDeviceConfiguration(DeviceConfigurationMatcherDTO matcher);
    
    DeviceConfigurationMatcherDTO createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO);

    boolean removeDeviceConfiguration(DeviceConfigurationMatcherType type, List<String> clientIds);

    boolean setStartTime(RaceLogSetStartTimeDTO dto);
    
    Pair<Date, Integer> getStartTime(String leaderboardName, String raceColumnName, String fleetName);

    Iterable<String> getAllIgtimiAccountEmailAddresses();

    String getIgtimiAuthorizationUrl();

    boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws Exception;

    void removeIgtimiAccount(String eMailOfAccountToRemove);

    Map<RegattaAndRaceIdentifier, Integer> importWindFromIgtimi(List<RaceDTO> selectedRaces) throws Exception;
    
    void addRaceLogTrackers(String leaderboardName) throws Exception;
    
    void addRaceLogTracker(String leaderboardName, String raceColumnName, String fleetName) throws Exception;
    
    void denoteForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName) throws Exception;
    
    void addOrUpdateRaceLogEvent(String leaderboardName, String raceColumnName, String fleetName, String jsonEvent)
    		throws Exception;
    
    void deleteRaceLogEvent(String leaderboardName, String raceColumnName, String fleetName, Serializable eventId);
}
