package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.configuration.DeviceConfigurationMatcherType;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.PositionDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.NoCorrespondingServiceRegisteredException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.NotRevokableException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.Revokable;
import com.sap.sailing.domain.racelog.tracking.DenoteForTrackingEvent;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardSearchResultDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.search.KeywordQuery;

/**
 * The client side stub for the RPC service. Usually, when a <code>null</code> date is passed to
 * the time-dependent service methods, an empty (non-<code>null</code>) result is returned.
 */
public interface SailingService extends RemoteService {
    List<TracTracConfigurationDTO> getPreviousTracTracConfigurations() throws Exception;
    
    List<RegattaDTO> getRegattas();

    RegattaDTO getRegattaByName(String regattaName);

    List<EventDTO> getEvents() throws Exception;

    List<EventBaseDTO> getPublicEventsOfAllSailingServers() throws Exception;

    Util.Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces) throws Exception;

    void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI,
            String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow, boolean ignoreTracTracMarkPassings, String tracTracUsername, String tracTracPassword) throws Exception;

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs, String hostname, int port,
            boolean trackWind, boolean correctWindByDeclination) throws Exception;
    
    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword) throws Exception;

    void stopTrackingEvent(RegattaIdentifier eventIdentifier) throws Exception;

    void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> racesToStopTracking) throws Exception;
    
    void removeAndUntrackRaces(Iterable<RegattaAndRaceIdentifier> regattaNamesAndRaceNames);

    WindInfoForRaceDTO getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources);

    void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO wind);

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent,
            boolean includeCombinedWindForAllLegMiddles) throws NoWindException;

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to, long resolutionInMilliseconds,
            Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent);

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSources) throws NoWindException;
    
    SimulatorResultsDTO getSimulatorResults(RegattaAndRaceIdentifier raceIdentifier, Date from, Date prevStartTime);

    CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date, Map<String, Date> fromPerCompetitorIdAsString,
            Map<String, Date> toPerCompetitorIdAsString, boolean extrapolate) throws NoWindException;
    
    RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier);
    
    List<RaceTimesInfoDTO> getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers);
    
    CoursePositionsDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date);

    RaceCourseDTO getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date);

    void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO);

    public List<String> getLeaderboardNames();
    
    IncrementalOrFullLeaderboardDTO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            String previousLeaderboardId, boolean fillNetPointsUncorrected) throws Exception;

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
            ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, boolean useStartTimeInference);
    
    void removeRegatta(RegattaIdentifier regattaIdentifier);

    void removeSeries(RegattaIdentifier regattaIdentifier, String seriesName);

    void removeRegattas(Collection<RegattaIdentifier> regattas);
    
    void updateRegatta(RegattaIdentifier regattaIdentifier, UUID defaultCourseAreaUuid, RegattaConfigurationDTO regattaConfiguration, boolean useStartTimeInference);
    
    List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames);

    void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
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
    Util.Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString,
            String raceColumnName, MaxPointsReason maxPointsReason, Date date) throws NoWindException;

    Util.Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName, String competitorIdAsString,
            String columnName, Double correctedScore, Date date) throws NoWindException;

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAsString, String displayName);
    
    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace);

    List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations();

    SwissTimingEventRecordDTO getRacesOfSwissTimingEvent(String eventJsonURL) throws Exception;

    void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, int port);

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
    
    LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description, String displayName,
            boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType);
    
    void updateLeaderboardGroup(String oldName, String newName, String description, String newDisplayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType);

    CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSizeInMs, DetailType detailType, String leaderboardGroupName, String leaderboardName) throws NoWindException;

    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind);

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude);
    
    ReplicationStateDTO getReplicaInfo();

    void startReplicatingFromMaster(String messagingHost, String masterHost, String exchangeName, int servletPort, int messagingPort) throws Exception;

    void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs);

    void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs);

    EventDTO updateEvent(UUID eventId, String eventName, String eventDescription, Date startDate, Date endDate,
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURL,
            String logoImageURL, Iterable<String> imageURLs, Iterable<String> videoURLs,
            Iterable<String> sponsorImageURLs) throws Exception;

    EventDTO createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue,
            boolean isPublic, List<String> courseAreaNames, Iterable<String> imageURLs,
            Iterable<String> videoURLs, Iterable<String> sponsorImageURLs, String logoImageURL, String officialWebsiteURL) throws Exception;

    void removeEvent(UUID eventId);

    void removeEvents(Collection<UUID> eventIds);

    void renameEvent(UUID eventId, String newName);

    EventDTO getEventById(UUID id, boolean withStatisticalData) throws Exception;

    Iterable<String> getScoreCorrectionProviderNames();

    ScoreCorrectionProviderDTO getScoreCorrectionsOfProvider(String providerName) throws Exception;

    RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName, String boatClassName,
            Date timePointWhenResultPublished) throws Exception;

    void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates) throws NoWindException;

    WindInfoForRaceDTO getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier);

    List<RemoteSailingServerReferenceDTO> getRemoteSailingServerReferences();

    void removeSailingServers(Set<String> toRemove) throws Exception;

    RemoteSailingServerReferenceDTO addRemoteSailingServerReference(RemoteSailingServerReferenceDTO sailingServer) throws Exception;

    List<String> getResultImportUrls(String resultProviderName);

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove) throws Exception;

    void addResultImportUrl(String resultProviderName, String url) throws Exception;

    void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity,
            String comment);

    List<String> getUrlResultProviderNames();
    
    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<Util.Pair<ControlPointDTO, PassingInstruction>> controlPoints);

    void addColumnsToLeaderboard(String leaderboardName, List<Util.Pair<String, Boolean>> columnsToAdd);

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove);

    StrippedLeaderboardDTO getLeaderboard(String leaderboardName);

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed);

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor);

    List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl);

    List<Util.Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName,
            Date date, DetailType detailType) throws Exception;

    List<String> getOverallLeaderboardNamesContaining(String leaderboardName);

    List<SwissTimingArchiveConfigurationDTO> getPreviousSwissTimingArchiveConfigurations();

    void storeSwissTimingArchiveConfiguration(String swissTimingUrl);

    PolarSheetGenerationResponse generatePolarSheetForRaces(List<RegattaAndRaceIdentifier> selectedRaces,
            PolarSheetGenerationSettings settings, String name) throws Exception;
    
    void createCourseArea(UUID eventId, String courseAreaName);
    
    void removeCourseArea(UUID eventId, UUID courseAreaId);

    List<Util.Pair<String, String>> getLeaderboardsNamesOfMetaLeaderboard(String metaLeaderboardName);

    Util.Pair<String, LeaderboardType> checkLeaderboardName(String leaderboardName);

    /** for backward compatibility with the regatta overview */
    List<RaceGroupDTO> getRegattaStructureForEvent(UUID eventId);

    /** the replacement service for getRegattaStructureForEvent() */
    List<RaceGroupDTO> getRegattaStructureOfEvent(UUID eventId);

    List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreas,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay)
            throws Exception;
    
    List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(String leaderboardName,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, List<String> visibleRegattas)
            throws Exception;

    String getBuildVersion();

    void stopReplicatingFromMaster();

    void stopAllReplicas();

    void stopSingleReplicaInstance(String identifier);

    void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet);

    RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet);

    List<String> getLeaderboardGroupNamesFromRemoteServer(String host);

    UUID importMasterData(String host, String[] groupNames, boolean override, boolean compress, boolean exportWind);
    
    DataImportProgress getImportOperationProgress(UUID id);

    Iterable<CompetitorDTO> getCompetitors();
    
    /**
     * 
     * @param leaderboardName
     * @param lookInRaceLogs If set to {@code true}, the {@link RaceLog}s are checked for the competitor registrations.
     * If set to {@code false}, the {@link RaceDefinition}s are checked instead.
     * @return
     */
    Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName, boolean lookInRaceLogs);

    CompetitorDTO addOrUpdateCompetitor(CompetitorDTO competitor);

    void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors);
    
    List<DeviceConfigurationMatcherDTO> getDeviceConfigurationMatchers();
    
    DeviceConfigurationDTO getDeviceConfiguration(DeviceConfigurationMatcherDTO matcher);
    
    DeviceConfigurationMatcherDTO createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO);

    boolean removeDeviceConfiguration(DeviceConfigurationMatcherType type, List<String> clientIds);

    boolean setStartTimeAndProcedure(RaceLogSetStartTimeAndProcedureDTO dto);
    
    Util.Triple<Date, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName);

    Iterable<String> getAllIgtimiAccountEmailAddresses();

    String getIgtimiAuthorizationUrl();

    boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws Exception;

    void removeIgtimiAccount(String eMailOfAccountToRemove);

    Map<RegattaAndRaceIdentifier, Integer> importWindFromIgtimi(List<RaceDTO> selectedRaces, boolean correctByDeclination) throws Exception;
    
    void denoteForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName) throws Exception;
    
    /**
     * Revoke the {@link DenoteForTrackingEvent}. This does not affect an existing {@code RaceLogRaceTracker}
     * or {@link TrackedRace} for this {@code RaceLog}.
     * 
     * @see RaceLogTrackingAdapter#removeDenotationForRaceLogTracking
     */
    void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName);

    void denoteForRaceLogTracking(String leaderboardName) throws Exception;
    
    /**
     * Performs all the necessary steps to start tracking the race.
     * The {@code RaceLog} needs to be denoted for racelog-tracking beforehand.
     * 
     * @see RaceLogTrackingAdapter#startTracking
     */
    void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName)
            throws NotDenotedForRaceLogTrackingException, Exception;
    
    void setCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName, Set<CompetitorDTO> competitors);
    
    Collection<CompetitorDTO> getCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName);
    
    void addMarkToRaceLog(String leaderboardName, String raceColumnName, String fleetName, MarkDTO markDTO);
    
    Collection<MarkDTO> getMarksInRaceLog(String leaderboardName, String raceColumnName, String fleetName);
    
    /**
     * Adds the course definition to the racelog, while trying to reuse existing marks, controlpoints and waypoints
     * from the previous course definition in the racelog.
     */
    void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName, List<Util.Pair<ControlPointDTO, PassingInstruction>> course);
    
    RaceCourseDTO getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName, String fleetName);
    
    /**
     * Adds a fix to the {@link GPSFixStore}, and creates a mapping with a virtual device for exactly the current timepoint.
     */
    void pingMarkViaRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark, PositionDTO position);
    
    void copyCourseAndCompetitorsToOtherRaceLogs(Util.Triple<String, String, String> raceLogFrom,
            Set<Util.Triple<String, String, String>> raceLogsTo);
    
    void addDeviceMappingToRaceLog(String leaderboardName, String raceColumnName, String fleetName, DeviceMappingDTO mapping)
            throws TransformationException;
    
    List<DeviceMappingDTO> getDeviceMappingsFromRaceLog(String leaderboardName, String raceColumnName, String fleetName)
            throws TransformationException;
    
    List<String> getDeserializableDeviceIdentifierTypes();
    
    void closeOpenEndedDeviceMapping(String leaderboardName, String raceColumnName, String fleetName, DeviceMappingDTO mapping,
            Date closingTimePoint) throws NoCorrespondingServiceRegisteredException, TransformationException;
    
    /**
     * Revoke the events in the {@code RaceLog} that are identified by the {@code eventIds}.
     * This only affects such events that implement {@link Revokable}.
     */
    void revokeRaceLogEvents(String leaderboardName, String raceColumnName, String fleetName, List<UUID> eventIds)
            throws NotRevokableException;
    
    Collection<String> getGPSFixImporterTypes();
    
    List<TrackFileImportDeviceIdentifierDTO> getTrackFileImportDeviceIds(List<String> uuids)
            throws NoCorrespondingServiceRegisteredException, TransformationException;
    
    /**
     * A client should search a server in a two-step process. First, the client should ask the server which other
     * servers are available for searching additional content. Then, in a second step, the client should fire the
     * queries by parallel asynchronous calls to the one server, passing the name of the remote server reference to
     * search, or <code>null</code> in order to search the server to which the query is sent by the call. This allows a
     * client to asynchronously receive the results from various servers, not requiring the client to block until all
     * results from all servers have been received. The key reason for this two-step process is that the GWT RPC does
     * not support streaming of results.
     * 
     * @return the list of server reference names, corresponding with {@link RemoteSailingServerReference#getName()}, to
     *         be used as parameter in {@link #search(String, KeywordQuery)}. This list does <em>not</em> contain the
     *         <code>null</code> value used to represent the search on the main server to which the query is sent.
     */
    Iterable<String> getSearchServerNames();
    
    /**
     * Call this method once for each result of {@link #getSearchServerNames()} and once with <code>null</code> for
     * the <code>serverNameOfNullForMain</code> parameter.
     * 
     * @param serverNameOrNullForMain
     *            use <code>null</code> to search on the server to which this request is sent; use a name as retrieved
     *            by {@link #getSearchServerNames()} which corresponds to a name of a
     *            {@link RemoteSailingServerReference}, to search a remote server.
     */
    Iterable<LeaderboardSearchResultDTO> search(String serverNameOrNullForMain, KeywordQuery query) throws Exception;
    
    /**
     * @return The RaceDTO of the modified race or <code>null</code>, if the given newStartTimeReceived was null.
     */
    RaceDTO setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived);

    Map<Integer, Date> getCompetitorMarkPassings(RegattaAndRaceIdentifier race, CompetitorDTO competitorDTO);

    Map<Integer, Date> getCompetitorRaceLogMarkPassingData(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO 
            fleet,
            CompetitorDTO competitor);

    void updateSuppressedMarkPassings(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            Integer newZeroBasedIndexOfSuppressedMarkPassing, CompetitorDTO competitorDTO);

    void updateFixedMarkPassing(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet, Integer indexOfWaypoint,
            Date dateOfMarkPassing, CompetitorDTO competitorDTO);

}
