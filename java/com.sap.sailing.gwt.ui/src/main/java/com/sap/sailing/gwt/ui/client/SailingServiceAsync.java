package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.RemoteSailingServerReference;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetGenerationResponse;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Position;
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
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
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
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
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
import com.sap.sse.gwt.client.BuildVersionRetriever;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtServiceAsync;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync extends BuildVersionRetriever, FileStorageManagementGwtServiceAsync {

    void getRegattas(AsyncCallback<List<RegattaDTO>> callback);

    void getRegattaByName(String regattaName, AsyncCallback<RegattaDTO> asyncCallback);


    /**
     * The string returned in the callback's pair is the common event name
     * @param listHiddenRaces 
     */
    void listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces, AsyncCallback<Util.Pair<String, List<TracTracRaceRecordDTO>>> callback);

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
     *            events received such that they seem to be sent in "real-time." So, more or less the time points
     *            attached to the events sent to the receivers will again approximate the wall time.
     * @param useInternalMarkPassingAlgorithm
     *            whether or not to ignore the TracTrac-provided mark passings; if <code>true</code>, a separate mark
     *            passing calculator is used, and the TracTrac-provided ones are ignored.
     * @param storedURImay
     *            be <code>null</code> or the empty string in which case the server will use the
     *            {@link TracTracRaceRecordDTO#storedURI} from the <code>rr</code> race record.
     */
    void trackWithTracTrac(RegattaIdentifier regattaToAddTo,
            Iterable<TracTracRaceRecordDTO> rrs, String liveURI, String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination,
            boolean simulateWithStartTimeNow, boolean useInternalMarkPassingAlgorithm, String tracTracUsername, String tracTracPassword, AsyncCallback<Void> callback);

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs,
            String hostname, int port, boolean trackWind, boolean correctWindByDeclination,
            boolean useInternalMarkPassingAlgorithm, AsyncCallback<Void> asyncCallback);

    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm,
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
     * @param windSourceTypeNames
     *            if <code>null</code>, information from all wind sources is returned; otherwise, information only from
     *            the sources listed in this parameter by name are returned
     * @param onlyUpToNewestEvent
     *            if <code>true</code>, no wind data will be returned for time points later than
     *            {@link TrackedRace#getTimePointOfNewestEvent() trackedRace.getTimePointOfNewestEvent()}. This is
     *            helpful in case the client wants to populate a chart during live mode. If <code>false</code>, the
     *            "best effort" readings are provided for the time interval requested, no matter if based on any sensor
     *            evidence or not, regardless of {@link TrackedRace#getTimePointOfNewestEvent()
     *            trackedRace.getTimePointOfNewestEvent()}.
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
            long resolutionInMilliseconds, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent,
            AsyncCallback<WindInfoForRaceDTO> callback);

    /**
     * @param windSourceTypeNames
     *            if <code>null</code>, data from all available wind sources will be returned, otherwise only from those
     *            whose {@link WindSource} name is contained in the <code>windSources</code> collection.
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth, int numberOfFixes,
            double latDeg, double lngDeg, Collection<String> windSourceTypeNames,
            AsyncCallback<WindInfoForRaceDTO> callback);

    /**
     * @param onlyUpToNewestEvent
     *            if <code>true</code>, no wind data will be returned for time points later than
     *            {@link TrackedRace#getTimePointOfNewestEvent() trackedRace.getTimePointOfNewestEvent()}. This is
     *            helpful in case the client wants to populate a chart during live mode. If <code>false</code>, the
     *            "best effort" readings are provided for the time interval requested, no matter if based on any sensor
     *            evidence or not, regardless of {@link TrackedRace#getTimePointOfNewestEvent()
     *            trackedRace.getTimePointOfNewestEvent()}.
     * @param includeCombinedWindForAllLegMiddles
     *            if <code>true</code>, the result will return non-<code>null</code> results for calls to
     *            {@link WindInfoForRaceDTO#getCombinedWindOnLegMiddle(int)}.
     */
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent,
            boolean includeCombinedWindForAllLegMiddles, AsyncCallback<WindInfoForRaceDTO> callback);

    void getPolarResults(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Boolean> callback);
    
    void getSimulatorResults(LegIdentifier legIdentifier, AsyncCallback<SimulatorResultsDTO> callback);
    
    void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO wind, AsyncCallback<Void> callback);

    void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO, AsyncCallback<Void> callback);

    void getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<RaceTimesInfoDTO> callback);

    void getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers, AsyncCallback<List<RaceTimesInfoDTO>> callback);

    void getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<CoursePositionsDTO> asyncCallback);

    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            boolean addOverallDetails, String previousLeaderboardId, boolean fillNetPointsUncorrected,
            AsyncCallback<IncrementalOrFullLeaderboardDTO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void getLeaderboards(AsyncCallback<List<StrippedLeaderboardDTO>> callback);

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
            MaxPointsReason maxPointsReason, Date date, AsyncCallback<Util.Triple<Double, Double, Boolean>> asyncCallback);

    void updateLeaderboardScoreCorrection(String leaderboardName, String competitorIdAsString, String columnName,
            Double correctedScore, Date date, AsyncCallback<Util.Triple<Double, Double, Boolean>> asyncCallback);

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
     * @param displayName TODO
     * @param displayGroupsInReverseOrder TODO
     */
    void createLeaderboardGroup(String groupName, String description,
            String displayName, boolean displayGroupsInReverseOrder,
            int[] overallLeaderboardDiscardThresholds, ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<LeaderboardGroupDTO> callback);

    void updateLeaderboardGroup(String oldName, String newName, String description, String newDisplayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<Void> callback);


    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind,
            AsyncCallback<Void> callback);

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude,
            AsyncCallback<Void> callback);

    /**
     * @param date
     *            use <code>null</code> to indicate "live" in which case the server live time stamp for the race
     *            identified by <code>raceIdentifier</code> will be used, considering that race's delay.
     */
    void getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date, Map<String, Date> fromPerCompetitorIdAsString,
            Map<String, Date> toPerCompetitorIdAsString, boolean extrapolate, LegIdentifier simulationLegIdentifier, AsyncCallback<CompactRaceMapDataDTO> callback);

    void getReplicaInfo(AsyncCallback<ReplicationStateDTO> callback);

    void startReplicatingFromMaster(String messagingHost, String masterName, String exchangeName, int servletPort, int messagingPort,
            AsyncCallback<Void> callback);

    void getEvents(AsyncCallback<List<EventDTO>> callback);

    void getPublicEventsOfAllSailingServers(AsyncCallback<List<EventBaseDTO>> callback);

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

    void createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue, boolean isPublic,
            List<String> courseAreaNames, Iterable<String> imageURLs, Iterable<String> videoURLs,
            Iterable<String> sponsorImageURLs, String logoImageURL, String officialWebsiteURL, AsyncCallback<EventDTO> callback);

    void updateEvent(UUID eventId, String eventName, String eventDescription, Date startDate, Date endDate,
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURL,
            String logoImageURL, Iterable<String> imageURLs, Iterable<String> videoURLs,
            Iterable<String> sponsorImageURLs, AsyncCallback<EventDTO> callback);

    void createCourseArea(UUID eventId, String courseAreaName, AsyncCallback<Void> callback);

    void removeCourseArea(UUID eventId, UUID courseAreaId, AsyncCallback<Void> callback);

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

    void createRegatta(String regattaName, String boatClassName, Date startDate, Date endDate,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal, boolean persistent,
            ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, boolean useStartTimeInference, AsyncCallback<RegattaDTO> callback);

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

    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<Util.Pair<ControlPointDTO, PassingInstruction>> controlPoints, AsyncCallback<Void> callback);

    void getRemoteSailingServerReferences(AsyncCallback<List<RemoteSailingServerReferenceDTO>> callback);

    void removeSailingServers(Set<String> toRemove, AsyncCallback<Void> callback);

    void addRemoteSailingServerReference(RemoteSailingServerReferenceDTO sailingServer, AsyncCallback<RemoteSailingServerReferenceDTO> callback);

    void getResultImportUrls(String resultProviderName, AsyncCallback<List<String>> callback);

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove, AsyncCallback<Void> callback);

    void addResultImportUrl(String resultProviderName, String url, AsyncCallback<Void> callback);

    void getUrlResultProviderNamesAndOptionalSampleURL(AsyncCallback<List<Pair<String, String>>> callback);

    void addColumnsToLeaderboard(String leaderboardName, List<Util.Pair<String, Boolean>> columnsToAdd,
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


    void updateRegatta(RegattaIdentifier regattaIdentifier, Date startDate, Date endDate, UUID defaultCourseAreaUuid,
            RegattaConfigurationDTO regattaConfiguration, boolean useStartTimeInference, AsyncCallback<Void> callback);

    /**
     * @param detailType
     *            supports {@link DetailType#REGATTA_RANK}, {@link DetailType#REGATTA_TOTAL_POINTS_SUM} and
     *            {@link DetailType#OVERALL_RANK}.
     * 
     * @return the first triple element is the race column name; then follows the list of competitors, and finally the
     *         list of values whose indices correspond with the elements in the {@link CompetitorDTO} list.
     */
    void getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, Date date, DetailType detailType,
            AsyncCallback<List<Util.Triple<String, List<CompetitorDTO>, List<Double>>>> callback);

    void getLeaderboardsNamesOfMetaLeaderboard(String metaLeaderboardName,
            AsyncCallback<List<Util.Pair<String, String>>> callback);

    void checkLeaderboardName(String leaderboardName, AsyncCallback<Util.Pair<String, LeaderboardType>> callback);

    void getBuildVersion(AsyncCallback<String> callback);

    void stopReplicatingFromMaster(AsyncCallback<Void> asyncCallback);

    void getRegattaStructureForEvent(UUID eventId, AsyncCallback<List<RaceGroupDTO>> asyncCallback);

    void getRegattaStructureOfEvent(UUID eventId, AsyncCallback<List<RaceGroupDTO>> callback);

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

    void getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet, AsyncCallback<RaceLogDTO> callback);

    void getRegattaLog(String leaderboardName, AsyncCallback<RegattaLogDTO> callback); 

    void importMasterData(String host, String[] names, boolean override, boolean compress, boolean exportWind,
            AsyncCallback<UUID> asyncCallback);

    void getImportOperationProgress(UUID id, AsyncCallback<DataImportProgress> asyncCallback);
    
    void getStructureImportOperationProgress(AsyncCallback<Integer> asyncCallback);

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
            AsyncCallback<Util.Triple<Date, Integer, RacingProcedureType>> callback);

    void getAllIgtimiAccountEmailAddresses(AsyncCallback<Iterable<String>> callback);
    
    void getIgtimiAuthorizationUrl(AsyncCallback<String> callback);
    
    void authorizeAccessToIgtimiUser(String eMailAddress, String password, AsyncCallback<Boolean> callback);

    void removeIgtimiAccount(String eMailOfAccountToRemove, AsyncCallback<Void> asyncCallback);

    void importWindFromIgtimi(List<RaceDTO> selectedRaces, boolean correctByDeclination, AsyncCallback<Map<RegattaAndRaceIdentifier, Integer>> asyncCallback);

    void getBoatClassNamesWithPolarSheetsAvailable(AsyncCallback<List<String>> asyncCallback);
    
    void getEventById(UUID id, boolean withStatisticalData, AsyncCallback<EventDTO> callback);
    
    void showCachedPolarSheetForBoatClass(String boatClassName,
            AsyncCallback<PolarSheetGenerationResponse> asyncCallback);
    
    void getLeaderboardsByEvent(EventDTO event, AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void denoteForRaceLogTracking(String leaderboardName,
            String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void denoteForRaceLogTracking(String leaderboardName, AsyncCallback<Void> callback);

    void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, boolean trackWind,
            boolean correctWindByDeclination, AsyncCallback<Void> callback);

    /**
     * Set the competitor registrations in the racelog. Unregisters formerly registered competitors
     * that are not listed in {@code competitors}.
     */
    void setCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorDTO> competitors, AsyncCallback<Void> callback);

    /**
     * Set the competitor registrations in the leaderboard. Unregisters formerly registered competitors
     * that are not listed in {@code competitors}.
     */
    void setCompetitorRegistrations(String leaderboardName,Set<CompetitorDTO> competitors,
            AsyncCallback<Void> callback);

    void getCompetitorRegistrations(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Collection<CompetitorDTO>> callback);

    void addMarkToRaceLog(String leaderboardName, String raceColumnName, String fleetName, MarkDTO markDTO,
            AsyncCallback<Void> callback);

    void getMarksInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Iterable<MarkDTO>> callback);

    void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<Util.Pair<ControlPointDTO, PassingInstruction>> course, AsyncCallback<Void> callback);

    void getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<RaceCourseDTO> callback);

    void pingMarkViaRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, MarkDTO mark,
            Position position, AsyncCallback<Void> callback);

    void getDeserializableDeviceIdentifierTypes(AsyncCallback<List<String>> callback);

    /**
     * Do not only use the racelog, but also other logs (e.g. RegattaLogs) in the hierarchy of the race.
     */
    void getDeviceMappingsFromLogHierarchy(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<List<DeviceMappingDTO>> callback);

    void getDeviceMappingsFromRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<List<DeviceMappingDTO>> callback);

    void addDeviceMappingToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO mapping, AsyncCallback<Void> callback);

    void closeOpenEndedDeviceMapping(String leaderboardName, String raceColumnName, String fleetName,
            DeviceMappingDTO mapping, Date closingTimePoint, AsyncCallback<Void> callback);

    void revokeRaceLogEvents(String leaderboardName, String raceColumnName, String fleetName, List<UUID> eventIds,
            AsyncCallback<Void> callback);

    void removeSeries(RegattaIdentifier regattaIdentifier, String seriesName, AsyncCallback<Void> callback);

    void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void copyCourseAndCompetitorsToOtherRaceLogs(Util.Triple<String, String, String> raceLogFrom,
            Set<Util.Triple<String, String, String>> raceLogsTo, AsyncCallback<Void> callback);

    void getGPSFixImporterTypes(AsyncCallback<Collection<String>> callback);

    void getTrackFileImportDeviceIds(List<String> uuids, AsyncCallback<List<TrackFileImportDeviceIdentifierDTO>> callback);

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
    void getSearchServerNames(AsyncCallback<Iterable<String>> callback);

    
    /**
     * Call this method once for each result of {@link #getSearchServerNames()} and once with <code>null</code> for
     * the <code>serverNameOfNullForMain</code> parameter.
     * 
     * @param serverNameOrNullForMain
     *            use <code>null</code> to search on the server to which this request is sent; use a name as retrieved
     *            by {@link #getSearchServerNames()} which corresponds to a name of a
     *            {@link RemoteSailingServerReference}, to search a remote server.
     */
    void search(String serverNameOrNullForMain, KeywordQuery query,
            AsyncCallback<Iterable<LeaderboardSearchResultDTO>> callback);

    void setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived, AsyncCallback<RaceDTO> callback);

    void getCompetitorRegistrations(String leaderboardName, AsyncCallback<Collection<CompetitorDTO>> callback);

    void createXYDiagramForBoatClass(String itemText, AsyncCallback<PolarSheetsXYDiagramData> asyncCallback);

    void getEventsForLeaderboard(String leaderboardName, AsyncCallback<Collection<EventDTO>> callback);
    
    /**
     * Imports regatta structure definitions from an ISAF XRR document
     * 
     * @param manage2SailJsonUrl the URL pointing to a Manage2Sail JSON document that contains the link to the XRR document
     */
    void getRegattas(String manage2SailJsonUrl, AsyncCallback<Iterable<RegattaDTO>> asyncCallback);
    
    void getCompetitorMarkPassings(RegattaAndRaceIdentifier race, CompetitorDTO competitorDTO,
            AsyncCallback<Map<Integer, Date>> callback);

    void getCompetitorRaceLogMarkPassingData(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet, CompetitorDTO competitor,
            AsyncCallback<Map<Integer, Date>> callback);

    void updateSuppressedMarkPassings(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            Integer newZeroBasedIndexOfSuppressedMarkPassing, CompetitorDTO competitorDTO, AsyncCallback<Void> callback);

    void createRegattaStructure(Iterable<RegattaDTO> regattaNames,
			EventDTO newEvent, AsyncCallback<Void> asyncCallback);

    void updateFixedMarkPassing(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet, Integer indexOfWaypoint,
            Date dateOfMarkPassing, CompetitorDTO competitorDTO, AsyncCallback<Void> callback);
            
    void getActiveFileStorageServiceName(AsyncCallback<String> callback);

    void inviteCompetitorsForTrackingViaEmail(String serverUrlWithoutTrailingSlash, EventDTO event,
            String leaderboardName, Set<CompetitorDTO> competitors, String localeInfo, AsyncCallback<Void> callback);

    void getMarksInRaceLogsAndTrackedRaces(String leaderboardName, AsyncCallback<Iterable<MarkDTO>> callback);

    void inviteBuoyTenderViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto, String leaderboardName,
            String emails, String localeInfoName, AsyncCallback<Void> callback);

    void getLeaderboardGroupsByEventId(UUID id, AsyncCallback<ArrayList<LeaderboardGroupDTO>> callback);
}
