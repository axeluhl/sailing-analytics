package com.sap.sailing.gwt.ui.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationMatcherDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
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
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync {

    void getRegattas(AsyncCallback<List<RegattaDTO>> callback);

    /**
     * The string returned in the callback's pair is the common event name
     * @param listHiddenRaces 
     */
    void listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces, AsyncCallback<Pair<String, List<TracTracRaceRecordDTO>>> callback);

    /**
     * @param regattaToAddTo
     *            if <code>null</code>, an existing regatta by the name of the TracTrac event with the boat class name
     *            appended in parentheses will be looked up; if not found, a default regatta with that name will be
     *            created, with a single default series and a single default fleet. If a valid {@link RegattaIdentifier}
     *            is specified, a regatta lookup is performed with that identifier; if the regatta is found, it is used
     *            to add the races to. Otherwise, a default regatta as described above will be created and used.
     * @param liveURI
     *            may be <code>null</code> or the empty string in which case the server will use the
     *            {@link TracTracRaceRecordDTO#liveURI} from the <code>rr</code> race record.
     * @param simulateWithStartTimeNow
     *            if <code>true</code>, the connector will adjust the time stamps of all events received such that the
     *            first mark passing for the first waypoint will be set to "now." It will delay the forwarding of all
     *            events received such that they seem to be sent in "real-time." So, more or less the time points attached
     *            to the events sent to the receivers will again approximate the wall time.
     * @param storedURImay
     *            be <code>null</code> or the empty string in which case the server will use the
     *            {@link TracTracRaceRecordDTO#storedURI} from the <code>rr</code> race record.
     */
    void trackWithTracTrac(RegattaIdentifier regattaToAddTo,
            Iterable<TracTracRaceRecordDTO> rrs, String liveURI, String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination,
            boolean simulateWithStartTimeNow, String tracTracUsername, String tracTracPassword, AsyncCallback<Void> callback);

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs,
            String hostname, int port, boolean trackWind, boolean correctWindByDeclination,
            AsyncCallback<Void> asyncCallback);

    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow,
            AsyncCallback<Void> asyncCallback);

    void getPreviousTracTracConfigurations(AsyncCallback<List<TracTracConfigurationDTO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI, String courseDesignUpdateURI,  String tracTracUsername, String tracTracPassword,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(RegattaIdentifier eventIdentifier, AsyncCallback<Void> callback);

    void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> racesToStopTracking, AsyncCallback<Void> asyncCallback);

    /**
     * Untracks the race and removes it from the regatta. It will also be removed in all leaderboards
     * @param regattaNamesAndRaceNames The identifier for the regatta name, and the race name to remove
     */
    void removeAndUntrackRaces(Iterable<RegattaNameAndRaceName> regattaNamesAndRaceNames, AsyncCallback<Void> callback);

    void getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources, AsyncCallback<WindInfoForRaceDTO> callback);

    /**
     * @param from if <code>null</code>, the tracked race's start of tracking is used
     * @param to if <code>null</code>, the tracked race's time point of newest event is used
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to, long resolutionInMilliseconds,
            Collection<String> windSourceTypeNames, AsyncCallback<WindInfoForRaceDTO> callback);

    /**
     * @param windSourceTypeNames
     *            if <code>null</code>, data from all available wind sources will be returned, otherwise only from those
     *            whose {@link WindSource} name is contained in the <code>windSources</code> collection.
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth, int numberOfFixes,
            double latDeg, double lngDeg, Collection<String> windSourceTypeNames,
            AsyncCallback<WindInfoForRaceDTO> callback);

    /**
     * Same as {@link #getWindInfo(RegattaAndRaceIdentifier, Date, long, int, double, double, Collection, AsyncCallback)}, only
     * that the wind is not requested for a specific position, but instead the wind sources associated with the tracked
     * race identified by <code>raceIdentifier</code> are requested to deliver their original position. This will in
     * particular preserve the positions of actual measurements and will deliver the averaged positions for averaged /
     * combined wind read-outs.
     * 
     * @param from
     *            must not be <code>null</code>
     * @param numberOfFixes
     *            no matter how great this value is chosen, never returns data beyond the newest event recorded in the
     *            race
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth, int numberOfFixes,
            Collection<String> windSourceTypeNames, AsyncCallback<WindInfoForRaceDTO> callback);

    void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO wind, AsyncCallback<Void> callback);

    void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO, AsyncCallback<Void> callback);

    void getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<RaceTimesInfoDTO> callback);

    void getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers, AsyncCallback<List<RaceTimesInfoDTO>> callback);

    void getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<CoursePositionsDTO> asyncCallback);

    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            String previousLeaderboardId, AsyncCallback<IncrementalOrFullLeaderboardDTO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void getLeaderboards(AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void getLeaderboardsByRegatta(RegattaDTO regatta, AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void getLeaderboardsByRaceAndRegatta(RaceDTO race, RegattaIdentifier regattaIdentifier,
            AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName,
            int[] newDiscardingThreasholds, UUID newCourseAreaId, AsyncCallback<StrippedLeaderboardDTO> callback);

    void createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds, ScoringSchemeType scoringSchemeType, UUID courseAreaId,
            AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName, int[] discardThresholds,
            AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);

    void removeLeaderboards(Collection<String> leaderboardNames, AsyncCallback<Void> asyncCallback);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName, AsyncCallback<Void> asyncCallback);

    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace,
            AsyncCallback<Void> callback);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName, AsyncCallback<Void> callback);

    void removeLeaderboardColumn(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, String fleetName,
            RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Boolean> asyncCallback);

    /**
     * The key set of the map returned contains all fleets of the race column identified by the combination of
     * <code>leaderboardName</code> and <code>raceColumnName</code>. If a value is <code>null</code>, there is no
     * tracked race currently linked to the fleet in the race column; otherwise, the value is the {@link RaceIdentifier}
     * of the tracked race currently connected for the fleet whose name is the key. The map returned is never <code>null</code>.
     */
    void getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName,
            AsyncCallback<Map<String, RegattaAndRaceIdentifier>> callback);

    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints, AsyncCallback<Void> callback);

    void updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString, String raceColumnName,
            MaxPointsReason maxPointsReason, Date date, AsyncCallback<Triple<Double, Double, Boolean>> asyncCallback);

    void updateLeaderboardScoreCorrection(String leaderboardName, String competitorIdAsString, String columnName,
            Double correctedScore, Date date, AsyncCallback<Triple<Double, Double, Boolean>> asyncCallback);

    void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity,
            String comment, AsyncCallback<Void> callback);

    void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates,
            AsyncCallback<Void> callback);

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorID, String displayName,
            AsyncCallback<Void> callback);

    void moveLeaderboardColumnUp(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    void moveLeaderboardColumnDown(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace, AsyncCallback<Void> callback);

    void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs, AsyncCallback<Void> callback);

    void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs, AsyncCallback<Void> callback);

    void getPreviousSwissTimingConfigurations(AsyncCallback<List<SwissTimingConfigurationDTO>> asyncCallback);

    void getRacesOfSwissTimingEvent(String eventJsonUrl, AsyncCallback<SwissTimingEventRecordDTO> asyncCallback);

    void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, int port, AsyncCallback<Void> asyncCallback);

    void getCountryCodes(AsyncCallback<String[]> callback);

    void getDouglasPoints(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            double meters, AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> callback);

    void getManeuvers(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            AsyncCallback<Map<CompetitorDTO, List<ManeuverDTO>>> callback);

    void getLeaderboardGroups(boolean withGeoLocationData, AsyncCallback<List<LeaderboardGroupDTO>> callback);

    void getLeaderboardGroupByName(String groupName, boolean withGeoLocationData,
            AsyncCallback<LeaderboardGroupDTO> callback);

    /**
     * Renames the group with the name <code>oldName</code> to the <code>newName</code>.<br />
     * If there's no group with the name <code>oldName</code> or there's already a group with the name
     * <code>newName</code> a {@link IllegalArgumentException} is thrown.
     */
    void renameLeaderboardGroup(String oldName, String newName, AsyncCallback<Void> callback);

    /**
     * Removes the leaderboard groups with the given names from the service and the persistant store.
     */
    void removeLeaderboardGroups(Set<String> groupNames, AsyncCallback<Void> asyncCallback);

    /**
     * Creates a new group with the name <code>groupname</code>, the description <code>description</code> and an empty list of leaderboards.<br/>
     * @param displayGroupsInReverseOrder TODO
     */
    void createLeaderboardGroup(String groupName, String description,
            boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<LeaderboardGroupDTO> callback);

    /**
     * Updates the data of the group with the name <code>oldName</code>.
     * 
     * @param oldName The old name of the group
     * @param newName The new name of the group
     * @param description The new description of the group
     * @param leaderboardNames The list of names of the new leaderboards of the group
     */
    void updateLeaderboardGroup(String oldName, String newName, String description,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<Void> callback);


    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind,
            AsyncCallback<Void> callback);

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude,
            AsyncCallback<Void> callback);

    void getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date, Map<String, Date> fromPerCompetitorIdAsString,
            Map<String, Date> toPerCompetitorIdAsString, boolean extrapolate, AsyncCallback<CompactRaceMapDataDTO> callback);

    void getReplicaInfo(AsyncCallback<ReplicationStateDTO> callback);

    void startReplicatingFromMaster(String messagingHost, String masterName, String exchangeName, int servletPort, int messagingPort,
            AsyncCallback<Void> callback);

    void getEvents(AsyncCallback<List<EventDTO>> callback);

    /**
     * Creates a {@link EventDTO} for the {@link com.sap.sailing.domain.base.Event} with the name <code>eventName</code>, which contains the
     * name, the description and a list with {@link RegattaDTO RegattaDTOs} contained in the event.<br />
     * If no event with the name <code>eventName</code> is known, an {@link IllegalArgumentException} is thrown.
     */
    void getEventByName(String eventName, AsyncCallback<EventDTO> callback);

    /**
     * Renames the event with the name <code>oldName</code> to the <code>newName</code>.<br />
     * If there's no event with the name <code>oldName</code> or there's already a event with the name
     * <code>newName</code> a {@link IllegalArgumentException} is thrown.
     */
    void renameEvent(UUID eventId, String newName, AsyncCallback<Void> callback);

    /**
     * Removes the event with the id <code>id</code> from the service and the persistence store.
     */
    void removeEvent(UUID eventId, AsyncCallback<Void> callback);

    void removeEvents(Collection<UUID> eventIds, AsyncCallback<Void> asyncCallback);

    void createEvent(String eventName, Date startDate, Date endDate, String description, boolean isPublic, List<String> courseAreaNames, AsyncCallback<EventDTO> callback);

    void updateEvent(UUID eventId, String eventName, Date startDate, Date endDate, VenueDTO venue, boolean isPublic,
            List<String> regattaNames, AsyncCallback<Void> callback);

    void createCourseArea(UUID eventId, String courseAreaName, AsyncCallback<CourseAreaDTO> callback);

    void removeRegatta(RegattaIdentifier regattaIdentifier, AsyncCallback<Void> callback);

    void removeRegattas(Collection<RegattaIdentifier> regattas, AsyncCallback<Void> asyncCallback);

    void addRaceColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<RaceColumnInSeriesDTO> callback);

    void removeRaceColumnFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName, 
            AsyncCallback<Void> callback);

    void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void createRegatta(String regattaName, String boatClassName,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal, boolean persistent,
            ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, AsyncCallback<RegattaDTO> callback);

    void addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames,
            AsyncCallback<List<RaceColumnInSeriesDTO>> callback);
    
    void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstRaceIsNonDiscardableCarryForward, boolean hasSplitFleetScore, List<FleetDTO> fleets,
            AsyncCallback<Void> callback);

    void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames,
            AsyncCallback<Void> callback);

    void getScoreCorrectionProviderNames(AsyncCallback<Iterable<String>> callback);

    void getScoreCorrectionsOfProvider(String providerName, AsyncCallback<ScoreCorrectionProviderDTO> callback);

    void getScoreCorrections(String scoreCorrectionProviderName, String eventName, String boatClassName,
            Date timePointWhenResultPublished, AsyncCallback<RegattaScoreCorrectionDTO> asyncCallback);

    void getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<WindInfoForRaceDTO> callback);

    void getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<RaceCourseDTO> callback);

    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<Pair<ControlPointDTO, PassingInstruction>> controlPoints, AsyncCallback<Void> callback);

    void getResultImportUrls(String resultProviderName, AsyncCallback<List<String>> callback);

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove, AsyncCallback<Void> callback);

    void addResultImportUrl(String resultProviderName, String url, AsyncCallback<Void> callback);

    void getUrlResultProviderNames(AsyncCallback<List<String>> callback);

    void addColumnsToLeaderboard(String leaderboardName, List<Pair<String, Boolean>> columnsToAdd,
            AsyncCallback<Void> callback);

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove, AsyncCallback<Void> callback);

    void getLeaderboard(String leaderboardName, AsyncCallback<StrippedLeaderboardDTO> callback);

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed, AsyncCallback<Void> asyncCallback);

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor,
            AsyncCallback<Void> callback);

    void listSwissTiminigReplayRaces(String swissTimingUrl, AsyncCallback<List<SwissTimingReplayRaceDTO>> asyncCallback);

    void getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSize, DetailType detailType, String leaderboarGroupName, String leaderboardName, AsyncCallback<CompetitorsRaceDataDTO> callback);

    /**
     * Finds out the names of all {@link com.sap.sailing.domain.leaderboard.MetaLeaderboard}s managed by this server that
     * {@link com.sap.sailing.domain.leaderboard.MetaLeaderboard#getLeaderboards() contain} the leaderboard identified by <code>leaderboardName</code>. The
     * names of those meta-leaderboards are returned. The list returned is never <code>null</code> but may be empty if no such
     * leaderboard is found.
     */
    void getOverallLeaderboardNamesContaining(String leaderboardName, AsyncCallback<List<String>> asyncCallback);

    void getPreviousSwissTimingArchiveConfigurations(
            AsyncCallback<List<SwissTimingArchiveConfigurationDTO>> asyncCallback);

    void storeSwissTimingArchiveConfiguration(String swissTimingUrl, AsyncCallback<Void> asyncCallback);

    void generatePolarSheetForRaces(List<RegattaAndRaceIdentifier> selectedRaces,
            PolarSheetGenerationSettings settings, String name, AsyncCallback<PolarSheetGenerationResponse> asyncCallback);


    void updateRegatta(RegattaIdentifier regattaIdentifier, UUID defaultCourseAreaUuid, RegattaConfigurationDTO regattaConfiguration, AsyncCallback<Void> callback);

    /**
     * @param detailType
     *            supports {@link DetailType#REGATTA_RANK}, {@link DetailType#REGATTA_TOTAL_POINTS} and
     *            {@link DetailType#OVERALL_RANK}.
     * 
     * @return the first triple element is the race column name; then follows the list of competitors, and finally the
     *         list of values whose indices correspond with the elements in the {@link CompetitorDTO} list.
     */
    void getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, Date date, DetailType detailType,
            AsyncCallback<List<Triple<String, List<CompetitorDTO>, List<Double>>>> callback);

    void getLeaderboardsNamesOfMetaleaderboard(String metaLeaderboardName,
            AsyncCallback<List<Pair<String, String>>> callback);

    void checkLeaderboardName(String leaderboardName, AsyncCallback<Pair<String, LeaderboardType>> callback);

    void getBuildVersion(AsyncCallback<String> callback);

    void stopReplicatingFromMaster(AsyncCallback<Void> asyncCallback);
    
    void getRegattaStructureForEvent(UUID eventId, AsyncCallback<List<RaceGroupDTO>> asyncCallback);

    void getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreas,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay,
            AsyncCallback<List<RegattaOverviewEntryDTO>> markedAsyncCallback);

    void getRaceStateEntriesForLeaderboard(String leaderboardName, boolean showOnlyCurrentlyRunningRaces,
            boolean showOnlyRacesOfSameDay, List<String> visibleRegattas,
            AsyncCallback<List<RegattaOverviewEntryDTO>> callback);
    
    void stopAllReplicas(AsyncCallback<Void> asyncCallback);

    void stopSingleReplicaInstance(String identifier, AsyncCallback<Void> asyncCallback);

    void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            AsyncCallback<Void> asyncCallback);

    void getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            AsyncCallback<RaceLogDTO> callback);

    void importMasterData(String host, String[] names, boolean override, boolean compress,
 boolean exportWind,
            AsyncCallback<UUID> asyncCallback);

    void getImportOperationProgress(UUID id, AsyncCallback<DataImportProgress> asyncCallback);

    void getLeaderboardGroupNamesFromRemoteServer(String host, AsyncCallback<List<String>> leaderboardGroupNames);

    void getCompetitors(AsyncCallback<Iterable<CompetitorDTO>> asyncCallback);

    void getCompetitorsOfLeaderboard(String leaderboardName, boolean lookInRaceLogs,
            AsyncCallback<Iterable<CompetitorDTO>> asyncCallback);

    void addOrUpdateCompetitor(CompetitorDTO competitor, AsyncCallback<CompetitorDTO> asyncCallback);

    void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors, AsyncCallback<Void> asyncCallback);

    void getDeviceConfigurationMatchers(AsyncCallback<List<DeviceConfigurationMatcherDTO>> asyncCallback);

    void getDeviceConfiguration(DeviceConfigurationMatcherDTO matcher, AsyncCallback<DeviceConfigurationDTO> callback);

    void createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO, DeviceConfigurationDTO configurationDTO, AsyncCallback<DeviceConfigurationMatcherDTO> callback);

    void removeDeviceConfiguration(DeviceConfigurationMatcherType type, List<String> clientIds, AsyncCallback<Boolean> asyncCallback);

    /**
     * Sets the a new start time.
     * @param dto {@link RaceLogSetStartTimeAndProcedureDTO} identifying the race to set the start time on and the new start time.
     */
    void setStartTimeAndProcedure(RaceLogSetStartTimeAndProcedureDTO dto, AsyncCallback<Boolean> callback);

    /**
     * Gets the race's current start time, current pass identifier and racing procedure. If no start time is set, the
     * pass identifier will still be returned, but the start time field will be <code>null</code>.
     */
    void getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Triple<Date, Integer, RacingProcedureType>> callback);

    void getAllIgtimiAccountEmailAddresses(AsyncCallback<Iterable<String>> callback);
    
    void getIgtimiAuthorizationUrl(AsyncCallback<String> callback);
    
    void authorizeAccessToIgtimiUser(String eMailAddress, String password, AsyncCallback<Boolean> callback);

    void removeIgtimiAccount(String eMailOfAccountToRemove, AsyncCallback<Void> asyncCallback);

    void importWindFromIgtimi(List<RaceDTO> selectedRaces, AsyncCallback<Map<RegattaAndRaceIdentifier, Integer>> asyncCallback);

    void getEventById(UUID id, AsyncCallback<EventDTO> callback);

    void getLeaderboardsByEvent(EventDTO event, AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void addRaceLogTrackers(String leaderboardName, AsyncCallback<Void> callback);

    void addRaceLogTracker(String leaderboardName, String raceColumnName,
            String fleetName, AsyncCallback<Void> callback);

    void denoteForRaceLogTracking(String leaderboardName,
            String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void denoteForRaceLogTracking(String leaderboardName, AsyncCallback<Void> callback);

    void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void setCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorDTO> competitors, AsyncCallback<Void> callback);

    void getCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Iterable<CompetitorDTO>> callback);

    void addMarkToRaceLog(String leaderboardName, String raceColumnName, String fleetName, MarkDTO markDTO,
            AsyncCallback<Void> callback);

    void getMarksInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Collection<MarkDTO>> callback);

    void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<Pair<ControlPointDTO, PassingInstruction>> course, AsyncCallback<Void> callback);

    void getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<RaceCourseDTO> callback);

    void pingMarkViaRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark,
            PositionDTO position, AsyncCallback<Void> callback);

    void copyCourseToOtherRaceLog(String leaderboardFrom, String raceColumnFrom, String fleetFrom,
            String leaderboardTo, String raceColumnTo, String fleetTo, AsyncCallback<Void> callback);

    void getDeserializableDeviceIdentifierTypes(AsyncCallback<List<String>> callback);

    void getDeviceMappingsFromRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<List<DeviceMappingDTO>> callback);

    void addDeviceMappingToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO mapping, AsyncCallback<Void> callback);

    void closeOpenEndedDeviceMapping(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO mapping, Date closingTimePoint, AsyncCallback<Void> callback);

    void revokeRaceLogEvents(String leaderboardName, String raceColumnName, String fleetName, List<Serializable> eventIds,
            AsyncCallback<Void> callback);

    void removeSeries(RegattaIdentifier regattaIdentifier, String seriesName, AsyncCallback<Void> callback);
}

