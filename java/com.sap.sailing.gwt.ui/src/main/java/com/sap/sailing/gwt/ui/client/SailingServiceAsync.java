package com.sap.sailing.gwt.ui.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorAndBoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.IncrementalOrFullLeaderboardDTO;
import com.sap.sailing.domain.common.dto.PairingListDTO;
import com.sap.sailing.domain.common.dto.PairingListTemplateDTO;
import com.sap.sailing.domain.common.dto.PersonDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceColumnInSeriesDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl.PreciseCompactPosition;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.gwt.ui.adminconsole.RaceLogSetTrackingTimesDTO;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTrackDTO;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTracksDTO;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorProviderDTO;
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
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetFinishingAndFinishTimeDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
import com.sap.sailing.gwt.ui.shared.RegattaOverviewEntryDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.RemoteSailingServerReferenceDTO;
import com.sap.sailing.gwt.ui.shared.SailingServiceConstants;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SliceRacePreperationDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TagDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ServerInfoRetriever;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtServiceAsync;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.gwt.client.replication.RemoteReplicationServiceAsync;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * The async counterpart of {@link SailingService}
 */
public interface SailingServiceAsync extends ServerInfoRetriever, FileStorageManagementGwtServiceAsync, RemoteReplicationServiceAsync {

    void getRegattas(AsyncCallback<List<RegattaDTO>> callback);

    void getRegattaByName(String regattaName, AsyncCallback<RegattaDTO> asyncCallback);

    /**
     * The string returned in the callback's pair is the common event name
     * 
     * @param listHiddenRaces
     */
    void listTracTracRacesInEvent(String eventJsonURL, boolean listHiddenRaces,
            AsyncCallback<Util.Pair<String, List<TracTracRaceRecordDTO>>> callback);

    /**
     * @param regattaToAddTo
     *            if <code>null</code>, the regatta into which the race has previously been loaded will be looked up; if
     *            found, the race will be loaded into that regatta; otherwise, an existing regatta by the name of the
     *            TracTrac event with the boat class name appended in parentheses will be looked up; if not found, a
     *            default regatta with that name will be created, with a single default series and a single default
     *            fleet. If a valid {@link RegattaIdentifier} is specified, a regatta lookup is performed with that
     *            identifier; if the regatta is found, it is used to add the races to. Otherwise, a default regatta as
     *            described above will be created and used.
     * @param liveURI
     *            may be <code>null</code> or the empty string in which case the server will use the
     *            {@link TracTracRaceRecordDTO#liveURI} from the <code>rr</code> race record.
     * @param offsetToStartTimeOfSimulatedRace
     *            if not <code>null</code>, the connector will adjust the time stamps of all events received such that
     *            the first mark passing for the first waypoint will be set to "now." It will delay the forwarding of
     *            all events received such that they seem to be sent in "real-time" plus the
     *            <code>offsetToStartTimeOfSimulatedRace</code>. So, more or less the time points attached to the events
     *            sent to the receivers will again approximate the wall time.
     * @param useInternalMarkPassingAlgorithm
     *            whether or not to ignore the TracTrac-provided mark passings; if <code>true</code>, a separate mark
     *            passing calculator is used, and the TracTrac-provided ones are ignored.
     * @param storedURImay
     *            be <code>null</code> or the empty string in which case the server will use the
     *            {@link TracTracRaceRecordDTO#storedURI} from the <code>rr</code> race record.
     */
    void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI,
            String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination,
            Duration offsetToStartTimeOfSimulatedRace, boolean useInternalMarkPassingAlgorithm, String tracTracUsername,
            String tracTracPassword, AsyncCallback<Void> callback);

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs,
            String hostname, int port, boolean trackWind, boolean correctWindByDeclination,
            boolean useInternalMarkPassingAlgorithm, String updateURL, String updateUsername, String updatePassword, AsyncCallback<Void> asyncCallback);

    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm,
            AsyncCallback<Void> asyncCallback);

    void getPreviousTracTracConfigurations(AsyncCallback<List<TracTracConfigurationDTO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword, AsyncCallback<Void> callback);

    void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> racesToStopTracking, AsyncCallback<Void> asyncCallback);

    /**
     * Untracks the race and removes it from the regatta. It will also be removed in all leaderboards
     * 
     * @param regattaNamesAndRaceNames
     *            The identifier for the regatta name, and the race name to remove
     */
    void removeAndUntrackRaces(Iterable<RegattaNameAndRaceName> regattaNamesAndRaceNames, AsyncCallback<Void> callback);

    void getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources,
            AsyncCallback<WindInfoForRaceDTO> callback);

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
    void getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, long millisecondsStepWidth,
            int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSourceTypeNames,
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

    void getRaceTimesInfoIncludingTags(RegattaAndRaceIdentifier raceIdentifier, TimePoint latestReceivedTagTime,
            AsyncCallback<RaceTimesInfoDTO> callback);

    void getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers,
            AsyncCallback<List<RaceTimesInfoDTO>> callback);

    void getRaceTimesInfosIncludingTags(Collection<RegattaAndRaceIdentifier> raceIdentifiers,
            Map<RegattaAndRaceIdentifier, TimePoint> latestReceivedTagTimes,
            AsyncCallback<List<RaceTimesInfoDTO>> callback);

    void getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date,
            AsyncCallback<CoursePositionsDTO> asyncCallback);

    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            String previousLeaderboardId, boolean fillTotalPointsUncorrected,
            AsyncCallback<IncrementalOrFullLeaderboardDTO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void getLeaderboards(AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void getLeaderboardsByRaceAndRegatta(String raceName, RegattaIdentifier regattaIdentifier,
            AsyncCallback<List<StrippedLeaderboardDTO>> callback);

    void updateLeaderboard(String leaderboardName, String newLeaderboardName, String newLeaderboardDisplayName,
            int[] newDiscardingThreasholds, UUID newCourseAreaId, AsyncCallback<StrippedLeaderboardDTO> callback);

    void createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName, int[] discardThresholds,
            ScoringSchemeType scoringSchemeType, UUID courseAreaId, AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, String leaderboardDisplayName,
            int[] discardThresholds, AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void createRegattaLeaderboardWithEliminations(String name, String displayName, String regattaName,
            AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);

    void removeLeaderboards(Collection<String> leaderboardNames, AsyncCallback<Void> asyncCallback);

    void renameLeaderboard(String leaderboardName, String newLeaderboardName, AsyncCallback<Void> asyncCallback);

    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace,
            AsyncCallback<Void> callback);

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName,
            AsyncCallback<Void> callback);

    void removeLeaderboardColumn(String leaderboardName, String columnName, AsyncCallback<Void> callback);

    void connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, String fleetName,
            RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Boolean> asyncCallback);

    /**
     * The key set of the map returned contains all fleets of the race column identified by the combination of
     * <code>leaderboardName</code> and <code>raceColumnName</code>. If a value is <code>null</code>, there is no
     * tracked race currently linked to the fleet in the race column; otherwise, the value is the {@link RaceIdentifier}
     * of the tracked race currently connected for the fleet whose name is the key. The map returned is never
     * <code>null</code>.
     */
    void getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(String leaderboardName, String raceColumnName,
            AsyncCallback<Map<String, RegattaAndRaceIdentifier>> callback);

    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints,
            AsyncCallback<Void> callback);

    void updateLeaderboardMaxPointsReason(String leaderboardName, String competitorIdAsString, String raceColumnName,
            MaxPointsReason maxPointsReason, Date date,
            AsyncCallback<Util.Triple<Double, Double, Boolean>> asyncCallback);

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

    void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs,
            AsyncCallback<Void> callback);

    void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs,
            AsyncCallback<Void> callback);

    void getPreviousSwissTimingConfigurations(AsyncCallback<List<SwissTimingConfigurationDTO>> asyncCallback);

    void getRacesOfSwissTimingEvent(String eventJsonUrl, AsyncCallback<SwissTimingEventRecordDTO> asyncCallback);

    void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, Integer port,
            String updateURL, String updateUsername, String updatePassword, AsyncCallback<Void> asyncCallback);

    void getCountryCodes(AsyncCallback<String[]> callback);

    void getDouglasPoints(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, TimeRange> competitorTimeRanges,
            double meters, AsyncCallback<Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>>> callback);

    void getManeuvers(RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, TimeRange> competitorTimeRanges,
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
     * Creates a new group with the name <code>groupname</code>, the description <code>description</code> and an empty
     * list of leaderboards.<br/>
     * 
     * @param displayName
     *            TODO
     * @param displayGroupsInReverseOrder
     *            TODO
     */
    void createLeaderboardGroup(String groupName, String description, String displayName,
            boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<LeaderboardGroupDTO> callback);

    void updateLeaderboardGroup(String oldName, String newName, String description, String newDisplayName,
            List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType, AsyncCallback<Void> callback);

    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind,
            AsyncCallback<Void> callback);

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude,
            AsyncCallback<Void> callback);

    void getCompetitorBoats(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Map<CompetitorDTO, BoatDTO>> callback);
    
    void getRaceboardData(String regattaName, String raceName, String leaderboardName, 
            String leaderboardGroupName, UUID eventId, AsyncCallback<RaceboardDataDTO> callback);

    /**
     * @param date
     *            use <code>null</code> to indicate "live" in which case the server live time stamp for the race
     *            identified by <code>raceIdentifier</code> will be used, considering that race's delay.
     * @param md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID
     *            used to decide whether the client requires an update to the race's competitor set. If the server
     *            has the same MD5 hash for the race's competitors, no competitor set is transmitted to the client.
     *            Otherwise, the full race competitor ID's as strings are sent to the client again for update.
     * @param estimatedDurationRequired {@code true} if the estimated duration should be calculated
     * @param timeToGetTheEstimatedDurationFor the time to use for the calculation of the estimated duration.
     *            May be {@code null} if estimatedDurationRequired is {@code false}
     */
    void getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate, LegIdentifier simulationLegIdentifier,
            byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID, Date timeToGetTheEstimatedDurationFor,
            boolean estimatedDurationRequired, AsyncCallback<CompactRaceMapDataDTO> callback);

    void getBoatPositions(RegattaAndRaceIdentifier raceIdentifier, Map<String, Date> fromPerCompetitorIdAsString,
            Map<String, Date> toPerCompetitorIdAsString, boolean extrapolate,
            AsyncCallback<CompactBoatPositionsDTO> callback);

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

    void createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue,
            boolean isPublic, List<String> courseAreaNames, String officialWebsiteURL, String baseURL,
            Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images,
            Iterable<VideoDTO> videos, Iterable<UUID> leaderboardGroupIDs, AsyncCallback<EventDTO> callback);

    void updateEvent(UUID eventId, String eventName, String eventDescription, Date startDate, Date endDate,
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURL,
            String baseURL, Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images,
            Iterable<VideoDTO> videos, Iterable<String> windFinderReviewedSpotCollectionIds, AsyncCallback<EventDTO> callback);

    void resolveImageDimensions(String imageUrlAsString, AsyncCallback<Pair<Integer, Integer>> callback);

    void createCourseAreas(UUID eventId, String[] courseAreaNames, AsyncCallback<Void> callback);

    void removeCourseAreas(UUID eventId, UUID[] idsOfCourseAreasToRemove, AsyncCallback<Void> callback);

    void removeRegatta(RegattaIdentifier regattaIdentifier, AsyncCallback<Void> callback);

    void removeRegattas(Collection<RegattaIdentifier> regattas, AsyncCallback<Void> asyncCallback);

    void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void createRegatta(String regattaName, String boatClassName, boolean canBoatsOfCompetitorsChangePerRace, Date startDate, Date endDate,
            RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal, boolean persistent,
            ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference,
            boolean controlTrackingFromStartAndFinishTimes, RankingMetrics rankingMetricType,
            AsyncCallback<RegattaDTO> callback);

    void addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName,
            List<Pair<String, Integer>> columnNames, AsyncCallback<List<RaceColumnInSeriesDTO>> callback);

    void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
            boolean isFleetsCanRunInParallel, int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstRaceIsNonDiscardableCarryForward, boolean hasSplitFleetScore, Integer maximumNumberOfDiscards,
            List<FleetDTO> fleets, AsyncCallback<Void> callback);

    void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames,
            AsyncCallback<Void> callback);

    void getScoreCorrectionProviderNames(AsyncCallback<Iterable<String>> callback);

    void getScoreCorrectionsOfProvider(String providerName, AsyncCallback<ScoreCorrectionProviderDTO> callback);

    void getScoreCorrections(String scoreCorrectionProviderName, String eventName, String boatClassName,
            Date timePointWhenResultPublished, AsyncCallback<RegattaScoreCorrectionDTO> asyncCallback);

    void getCompetitorProviderNames(AsyncCallback<Iterable<String>> callback);

    void getCompetitorProviderDTOByName(String providerName, AsyncCallback<CompetitorProviderDTO> callback);

    void getCompetitorDescriptors(String competitorProviderName, String eventName, String regattaName, AsyncCallback<List<CompetitorDescriptor>> callback);

    void getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<WindInfoForRaceDTO> callback);

    void getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<RaceCourseDTO> callback);

    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier,
            List<Util.Pair<ControlPointDTO, PassingInstruction>> controlPoints, AsyncCallback<Void> callback);

    void getServerConfiguration(AsyncCallback<ServerConfigurationDTO> callback);

    void updateServerConfiguration(ServerConfigurationDTO serverConfiguration, AsyncCallback<Void> callback);

    void getRemoteSailingServerReferences(AsyncCallback<List<RemoteSailingServerReferenceDTO>> callback);

    void removeSailingServers(Set<String> toRemove, AsyncCallback<Void> callback);

    void addRemoteSailingServerReference(RemoteSailingServerReferenceDTO sailingServer,
            AsyncCallback<RemoteSailingServerReferenceDTO> callback);

    void getResultImportUrls(String resultProviderName, AsyncCallback<List<String>> callback);

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove, AsyncCallback<Void> callback);

    void addResultImportUrl(String resultProviderName, String url, AsyncCallback<Void> callback);

    void getUrlResultProviderNamesAndOptionalSampleURL(AsyncCallback<List<Pair<String, String>>> callback);

    void addColumnsToLeaderboard(String leaderboardName, List<Util.Pair<String, Boolean>> columnsToAdd,
            AsyncCallback<Void> callback);

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove, AsyncCallback<Void> callback);

    void getLeaderboard(String leaderboardName, AsyncCallback<StrippedLeaderboardDTO> callback);

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed,
            AsyncCallback<Void> asyncCallback);

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor,
            AsyncCallback<Void> callback);

    void listSwissTiminigReplayRaces(String swissTimingUrl, AsyncCallback<List<SwissTimingReplayRaceDTO>> asyncCallback);

    /**
     * Callers will be able to obtain at most {@link SailingServiceConstants#MAX_NUMBER_OF_FIXES_TO_QUERY} fixes using
     * this method. If a finer resolution is requested based on {@code from}, {@code to}, and {@code stepSize}, the
     * resolution will automatically be lowered so that no more than
     * {@link SailingServiceConstants#MAX_NUMBER_OF_FIXES_TO_QUERY} are returned per competitor.
     */
    void getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSize, DetailType detailType, String leaderboarGroupName, String leaderboardName,
            AsyncCallback<CompetitorsRaceDataDTO> callback);

    /**
     * Finds out the names of all {@link com.sap.sailing.domain.leaderboard.MetaLeaderboard}s managed by this server
     * that {@link com.sap.sailing.domain.leaderboard.MetaLeaderboard#getLeaderboards() contain} the leaderboard
     * identified by <code>leaderboardName</code>. The names of those meta-leaderboards are returned. The list returned
     * is never <code>null</code> but may be empty if no such leaderboard is found.
     */
    void getOverallLeaderboardNamesContaining(String leaderboardName, AsyncCallback<List<String>> asyncCallback);

    void getPreviousSwissTimingArchiveConfigurations(
            AsyncCallback<List<SwissTimingArchiveConfigurationDTO>> asyncCallback);

    void storeSwissTimingArchiveConfiguration(String swissTimingUrl, AsyncCallback<Void> asyncCallback);

    void updateRegatta(RegattaIdentifier regattaIdentifier, Date startDate, Date endDate, UUID defaultCourseAreaUuid,
            RegattaConfigurationDTO regattaConfiguration, Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference,
            boolean controlTrackingFromStartAndFinishTimes, AsyncCallback<Void> callback);

    /**
     * @param detailType
     *            supports {@link DetailType#REGATTA_RANK}, {@link DetailType#REGATTA_NET_POINTS_SUM} and
     *            {@link DetailType#OVERALL_RANK}.
     * 
     * @return the first triple element is the race column name; then follows the list of competitors, and finally the
     *         list of values whose indices correspond with the elements in the {@link CompetitorWithBoatDTO} list.
     */
    void getLeaderboardDataEntriesForAllRaceColumns(String leaderboardName, Date date, DetailType detailType,
            AsyncCallback<List<Util.Triple<String, List<CompetitorDTO>, List<Double>>>> callback);

    void getLeaderboardsNamesOfMetaLeaderboard(String metaLeaderboardName,
            AsyncCallback<List<Util.Pair<String, String>>> callback);

    void getLeaderboardType(String leaderboardName, AsyncCallback<LeaderboardType> callback);

    void getRegattaStructureForEvent(UUID eventId, AsyncCallback<List<RaceGroupDTO>> asyncCallback);
    
    void getRegattaStructureOfEvent(UUID eventId, AsyncCallback<List<RaceGroupDTO>> callback);

    void getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreas, List<String> visibleRegattas,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay,
            Duration clientTimeZoneOffset, AsyncCallback<List<RegattaOverviewEntryDTO>> markedAsyncCallback);

    void getRaceStateEntriesForLeaderboard(String leaderboardName, boolean showOnlyCurrentlyRunningRaces,
            boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset, List<String> visibleRegattas,
            AsyncCallback<List<RegattaOverviewEntryDTO>> callback);

    void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            AsyncCallback<Void> asyncCallback);

    void getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet,
            AsyncCallback<RaceLogDTO> callback);

    void getRegattaLog(String leaderboardName, AsyncCallback<RegattaLogDTO> callback);

    void importMasterData(String host, String[] names, boolean override, boolean compress, boolean exportWind,
            boolean exportDeviceConfigurations, AsyncCallback<UUID> asyncCallback);

    void getImportOperationProgress(UUID id, AsyncCallback<DataImportProgress> asyncCallback);

    void getStructureImportOperationProgress(AsyncCallback<Integer> asyncCallback);

    void getLeaderboardGroupNamesFromRemoteServer(String host, AsyncCallback<List<String>> leaderboardGroupNames);

    void getCompetitors(boolean filterCompetitorsWithBoat, boolean filterCompetitorsWithoutBoat, AsyncCallback<Iterable<CompetitorDTO>> asyncCallback);

    void getCompetitorsOfLeaderboard(String leaderboardName, AsyncCallback<Iterable<CompetitorDTO>> asyncCallback);

    void getCompetitorsAndBoatsOfRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Map<? extends CompetitorDTO, BoatDTO>> asyncCallback);
    
    void addOrUpdateCompetitors(List<CompetitorDTO> competitors, AsyncCallback<List<CompetitorDTO>> asyncCallback);

    void addOrUpdateCompetitorWithBoat(CompetitorWithBoatDTO competitor, AsyncCallback<CompetitorWithBoatDTO> asyncCallback);

    void addOrUpdateCompetitorWithoutBoat(CompetitorDTO competitor, AsyncCallback<CompetitorDTO> asyncCallback);
    
    void addCompetitors(List<CompetitorDescriptor> competitorsForSaving, String searchTag, AsyncCallback<List<CompetitorWithBoatDTO>> asyncCallback);

    void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors, AsyncCallback<Void> asyncCallback);

    void getAllBoats(AsyncCallback<Iterable<BoatDTO>> asyncCallback);

    void getStandaloneBoats(AsyncCallback<Iterable<BoatDTO>> asyncCallback);

    void addOrUpdateBoat(BoatDTO boat, AsyncCallback<BoatDTO> asyncCallback);

    void allowBoatResetToDefaults(Iterable<BoatDTO> boats, AsyncCallback<Void> asyncCallback);

    void linkBoatToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString, String boatIdAsString, AsyncCallback<Boolean> asyncCallback);

    void unlinkBoatFromCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString, AsyncCallback<Boolean> asyncCallback);

    void getBoatLinkedToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName, String competitorIdAsString, AsyncCallback<BoatDTO> asyncCallback); 
    
    void getDeviceConfigurationMatchers(AsyncCallback<List<DeviceConfigurationMatcherDTO>> asyncCallback);

    void getDeviceConfiguration(DeviceConfigurationMatcherDTO matcher, AsyncCallback<DeviceConfigurationDTO> callback);

    void createOrUpdateDeviceConfiguration(DeviceConfigurationMatcherDTO matcherDTO,
            DeviceConfigurationDTO configurationDTO, AsyncCallback<DeviceConfigurationMatcherDTO> callback);

    void removeDeviceConfiguration(List<String> clientIds, AsyncCallback<Boolean> asyncCallback);

    /**
     * Sets the a new start time.
     * 
     * @param dto
     *            {@link RaceLogSetStartTimeAndProcedureDTO} identifying the race to set the start time on and the new
     *            start time.
     */
    void setStartTimeAndProcedure(RaceLogSetStartTimeAndProcedureDTO dto, AsyncCallback<Boolean> callback);

    /**
     * Sets the a new finishing and end time.
     * 
     * @param dto
     *            {@link RaceLogSetFinishingAndFinishTimeDTO} identifying the race and the new finishing and
     *            end time.
     */
    void setFinishingAndEndTime(RaceLogSetFinishingAndFinishTimeDTO editedObject, AsyncCallback<Pair<Boolean, Boolean>> asyncCallback);
    
    /**
     * Gets the race's current start time, current pass identifier and racing procedure. If no start time is set, the
     * pass identifier will still be returned, but the start time field will be <code>null</code>.
     */
    void getStartTimeAndProcedure(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Util.Triple<Date, Integer, RacingProcedureType>> callback);

    /**
     * Gets the race's current finishing and finish times as well as the current pass identifier. If no finishing or finish time is set, the
     * pass identifier will still be returned, but the finishing/finish time field will be <code>null</code>.
     */
    void getFinishingAndFinishTime(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Util.Triple<Date, Date, Integer>> asyncCallback);
    
    void getAllIgtimiAccountEmailAddresses(AsyncCallback<Iterable<String>> callback);

    void getIgtimiAuthorizationUrl(String redirectProtocol, String redirectHostname, String redirectPort, AsyncCallback<String> callback);

    void authorizeAccessToIgtimiUser(String eMailAddress, String password, AsyncCallback<Boolean> callback);

    void removeIgtimiAccount(String eMailOfAccountToRemove, AsyncCallback<Void> asyncCallback);

    void importWindFromIgtimi(List<RaceDTO> selectedRaces, boolean correctByDeclination,
            AsyncCallback<Map<RegattaAndRaceIdentifier, Integer>> asyncCallback);

    void getEventById(UUID id, boolean withStatisticalData, AsyncCallback<EventDTO> callback);

    /**
     * @return {@code true} if the race was not yet denoted for race log tracking and now has successfully been denoted
     *         so
     */
    void denoteForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Boolean> callback);

    void denoteForRaceLogTracking(String leaderboardName, AsyncCallback<Void> callback);
    
    void denoteForRaceLogTracking(String leaderboardName,String prefix, AsyncCallback<Void> callback);

    void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, boolean trackWind,
            boolean correctWindByDeclination, AsyncCallback<Void> callback);

    void startRaceLogTracking(List<Triple<String, String, String>> leaderboardRaceColumnFleetNames,
            final boolean trackWind, final boolean correctWindByDeclination, AsyncCallback<Void> callback);
    
    /**
     * Set the competitor (with contained boats) registrations in the racelog. Unregisters formerly registered competitors that are not listed
     * in {@code competitors}.
     */
    void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorWithBoatDTO> competitors, AsyncCallback<Void> callback);

    /**
     * Set the competitor and boat registrations in the racelog. Unregisters formerly registered competitors that are not listed
     * in {@code competitors}.
     */
    void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Map<? extends CompetitorDTO, BoatDTO> competitorsAndBoats, AsyncCallback<Void> callback);

    /**
     * Set the competitor registrations in the leaderboard. Unregisters formerly registered competitors that are not
     * listed in {@code competitors}.
     */
    void setCompetitorRegistrationsInRegattaLog(String leaderboardName, Set<? extends CompetitorDTO> competitors,
            AsyncCallback<Void> callback);

    void getCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName, AsyncCallback<Collection<CompetitorAndBoatDTO>> callback);

    void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<Util.Pair<ControlPointDTO, PassingInstruction>> course, AsyncCallback<Void> callback);

    void addTagToRaceLog(String leaderboardName, String raceColumnName, String fleetName, String tag, String comment,
            String imageURL, boolean isPublic, TimePoint raceTimepoint, AsyncCallback<SuccessInfo> asyncCallback);

    void removeTagFromRaceLog(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag,
            AsyncCallback<SuccessInfo> asyncCallback);
    
    void getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<RaceCourseDTO> callback);

    void pingMark(String leaderboardName, MarkDTO mark,
            TimePoint timePoint, Position position, AsyncCallback<Void> callback);

    void getDeserializableDeviceIdentifierTypes(AsyncCallback<List<String>> callback);

    void revokeRaceAndRegattaLogEvents(String leaderboardName, String raceColumnName, String fleetName,
            List<UUID> eventIds, AsyncCallback<Void> callback);

    void removeSeries(RegattaIdentifier regattaIdentifier, String seriesName, AsyncCallback<Void> callback);

    void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> callback);

    void getGPSFixImporterTypes(AsyncCallback<Collection<String>> callback);
    
    void getSensorDataImporterTypes(AsyncCallback<Collection<String>> callback);

    void getTrackFileImportDeviceIds(List<String> uuids,
            AsyncCallback<List<TrackFileImportDeviceIdentifierDTO>> callback);

    void setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived,
            AsyncCallback<RaceDTO> callback);

    void createXYDiagramForBoatClass(String itemText, AsyncCallback<PolarSheetsXYDiagramData> asyncCallback);

    void getEventsForLeaderboard(String leaderboardName, AsyncCallback<Collection<EventDTO>> callback);

    /**
     * Imports regatta structure definitions from an ISAF XRR document
     * 
     * @param manage2SailJsonUrl
     *            the URL pointing to a Manage2Sail JSON document that contains the link to the XRR document
     */
    void getRegattas(String manage2SailJsonUrl, AsyncCallback<Iterable<RegattaDTO>> asyncCallback);
    
    /**
     * Returns mark passings for the competitor. Using the {@code waitForCalculations} parameter callers can control
     * whether to obtain a snapshot immediately of wait for pending updates. Waiting may be desirable, e.g., when having
     * submitted a fixed mark passing into a race log which triggers the re-calculations asynchronously.
     */
    void getCompetitorMarkPassings(RegattaAndRaceIdentifier race, CompetitorDTO competitorDTO,
            boolean waitForCalculations, AsyncCallback<Map<Integer, Date>> callback);

    void getCompetitorRaceLogMarkPassingData(String leaderboardName, String raceColumnName, String fleetName,
            CompetitorDTO competitor, AsyncCallback<Map<Integer, Date>> callback);

    void updateSuppressedMarkPassings(String leaderboardName, String raceColumnName, String fleetName,
            Integer newZeroBasedIndexOfSuppressedMarkPassing, CompetitorDTO competitorDTO, AsyncCallback<Void> callback);

    void createRegattaStructure(Iterable<RegattaDTO> regattaNames, EventDTO newEvent, AsyncCallback<Void> asyncCallback);

    void updateFixedMarkPassing(String leaderboardName, String raceColumnName, String fleetName,
            Integer indexOfWaypoint, Date dateOfMarkPassing, CompetitorDTO competitorDTO, AsyncCallback<Void> callback);

    void getActiveFileStorageServiceName(AsyncCallback<String> callback);

    void inviteCompetitorsForTrackingViaEmail(String serverUrlWithoutTrailingSlash, EventDTO event,
            String leaderboardName, Collection<CompetitorDTO> competitors, String iOSAppUrl, String androidAppUrl,
            String localeInfo,
            AsyncCallback<Void> callback);

    void inviteBuoyTenderViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto, String leaderboardName,
            String emails, String iOSAppUrl, String androidAppUrl, String localeInfoName, AsyncCallback<Void> callback);
            
    void getLeaderboardGroupsByEventId(UUID id, AsyncCallback<ArrayList<LeaderboardGroupDTO>> callback);

    void getMarksInRegattaLog(String leaderboardName, AsyncCallback<Iterable<MarkDTO>> callback);

    void getDeviceMappings(String leaderboardName, AsyncCallback<List<DeviceMappingDTO>> asyncCallback);
    
    void revokeRaceAndRegattaLogEvents(String leaderboardName, List<UUID> eventIds, AsyncCallback<Void> callback);

    void closeOpenEndedDeviceMapping(String leaderboardName, DeviceMappingDTO mappingDto, Date closingTimePoint,
            AsyncCallback<Void> asyncCallback);

    void addDeviceMappingToRegattaLog(String leaderboardName, DeviceMappingDTO dto, AsyncCallback<Void> callback);
    
    void addTypedDeviceMappingToRegattaLog(String leaderboardName, TypedDeviceMappingDTO dto, AsyncCallback<Void> callback);

    void doesRegattaLogContainCompetitors(String name, AsyncCallback<Boolean> regattaLogCallBack);

    void getRaceIdentifier(String regattaLikeName, String raceColumnName, String fleetName,
            AsyncCallback<RegattaAndRaceIdentifier> asyncCallback);

    void setTrackingTimes(RaceLogSetTrackingTimesDTO dto, AsyncCallback<Void> callback);

    void getTrackingTimes(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> callback);

    /**
     * Copies one race's competitor set to other races.
     * @param fromTriple
     *            leaderboard name, race column name, and fleet name identifying the race from which to copy the
     *            competitors
     * @param toTriples leaderboard name, race column name, and fleet name identifying the races to which to copy the
     *            competitors
     */
    void copyCompetitorsToOtherRaceLogs(Triple<String, String, String> fromTriple,
            Set<Triple<String, String, String>> toTriples, AsyncCallback<Void> callback);

    void copyCourseToOtherRaceLogs(Triple<String, String, String> fromTriple,
            Set<Triple<String, String, String>> toTriples, AsyncCallback<Void> callback);

    void addMarkToRegattaLog(String leaderboardName, MarkDTO mark, AsyncCallback<Void> asyncCallback);

    void revokeMarkDefinitionEventInRegattaLog(String leaderboardName, MarkDTO markDTO, AsyncCallback<Void> callback);

    void areCompetitorRegistrationsEnabledForRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Boolean> asyncCallback);

    void disableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> asyncCallback);

    void enableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Void> asyncCallback);

    void checkIfMarksAreUsedInOtherRaceLogs(String leaderboardName, String raceColumnName, String fleetName,
            Set<MarkDTO> marksToRemove, AsyncCallback<Pair<Boolean, String>> asyncCallback);

    void getCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Collection<CompetitorAndBoatDTO>> callback);

    void getCompetitorAndBoatRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Map<CompetitorDTO, BoatDTO>> callback);

    void getCompetitorRegistrationsForLeaderboard(String leaderboardName, AsyncCallback<Collection<CompetitorDTO>> callback);

    void getCompetitorRegistrationsInRegattaLog(String leaderboardName, AsyncCallback<Collection<CompetitorDTO>> setCompetitorsCallback);

    void getBoatRegistrationsInRegattaLog(String leaderboardName, AsyncCallback<Collection<BoatDTO>> setBoatsCallback);

    void setBoatRegistrationsInRegattaLog(String leaderboardName, Set<BoatDTO> boats,
            AsyncCallback<Void> callback);
    
    void getBoatRegistrationsForLeaderboard(String leaderboardName, AsyncCallback<Collection<BoatDTO>> callback);

    void addMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO newFix, AsyncCallback<Void> callback);

    void editMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO oldFix, Position newPosition, AsyncCallback<Void> callback);

    void removeMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO fix, AsyncCallback<Void> callback);

    void canRemoveMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO fix, AsyncCallback<Boolean> callback);
    
    void getMarksInTrackedRace(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<Iterable<MarkDTO>> callback);

    void getMarkTracks(String leaderboardName, String raceColumnName, String fleetName,
            AsyncCallback<MarkTracksDTO> callback);
    
    void getMarkTrack(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString, 
            AsyncCallback<MarkTrackDTO> callback);
    
    void getTrackingTimes(Collection<Triple<String, String, String>> leaderboardRaceColumnFleetNames,
            AsyncCallback<Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>>> asyncCallback);

    void serializationDummy(PersonDTO dummy, CountryCode ccDummy,
			PreciseCompactPosition preciseCompactPosition, AsyncCallback<Pair<PersonDTO, CountryCode>> callback);

    /**
     * @param leaderboardName
     *            expected to match a regatta leaderboard with eliminations
     */
    void getEliminatedCompetitors(String leaderboardName, AsyncCallback<Collection<CompetitorDTO>> asyncCallback);

    void setEliminatedCompetitors(String leaderboardName, Set<CompetitorDTO> eliminatedCompetitors, AsyncCallback<Void> callback);
    
    /**
     * Used to determine for a Chart the available Detailtypes. This is for example used, to only show the RideHeight as
     * an option for charts, if it actually recorded for the race.
     */
    void determineDetailTypesForCompetitorChart(String leaderboardGroupName, RegattaAndRaceIdentifier identifier,
            AsyncCallback<Iterable<DetailType>> callback);

    void getExpeditionDeviceConfigurations(AsyncCallback<List<ExpeditionDeviceConfiguration>> callback);

    void removeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration, AsyncCallback<Void> asyncCallback);

    void addOrReplaceExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration, AsyncCallback<Void> asyncCallback);

    /**
     * Calculates a {@link PairingListTemplate} based on a competitor count, flight count and group count of the
     * leaderboard. Since the competitor count must not be the competitors that are registered on the leaderboard, it
     * can vary.
     * 
     * @param leaderboardName
     *            the name of the leaderboard
     * @param competitorCount
     *            the count of competitors
     * @param flightMultiplier
     *            specifies how often the flights will be cloned
     * @param callback
     *            returns a {@link PairingListTemplateDTO}
     */
    void calculatePairingListTemplate(final int flightCount, final int groupCount, final int competitorCount,
            final int flightMultiplier, AsyncCallback<PairingListTemplateDTO> callback);

    /**
     * Creates a {@link PairingListDTO} in which the competitors will be matched to a {@link PairingList} based on the
     * information that the {@link PairingListTemplate} contains.
     * 
     * @param leaderboardName
     * @param flightMultiplier
     *            specifies how often the flights will be cloned
     * @param callback
     */
    void getPairingListFromTemplate(final String leaderboardName, final int flightMultiplier,
            final Iterable<String> selectedFlightNames,PairingListTemplateDTO templateDTO, AsyncCallback<PairingListDTO> callback);

    /**
     * Creates a {@link PairingListDTO} that is based on the competitors in the race logs of a leaderboard.
     * 
     * @param leaderboardName
     *            the name of the leaderboard
     * @param callback
     *            returns a {@link PairingListDTO}
     */
    void getPairingListFromRaceLogs(final String leaderboardName, AsyncCallback<PairingListDTO> callback);

    /**
     * Registers all competitors of a {@link PairingList} in the respective {@link RaceColumn}s and {@link Fleet}s.
     * 
     * @param leaderboardName
     *            the name of the leaderboard
     * @param flightMultiplier
     *            specifies how often the flights will be cloned
     * @param callback
     */
    void fillRaceLogsFromPairingListTemplate(final String leaderboardName, final int flightMultiplier,
            final Iterable<String> selectedFlightNames,PairingListDTO pairingListDTO, AsyncCallback<Void> callback);
    
    void getRaceDisplayNamesFromLeaderboard(final String leaderboardName, List<String> raceColumnNames, AsyncCallback<List<String>> callback);

    void getAvailableDetailTypesForLeaderboard(String leaderboardName, RegattaAndRaceIdentifier raceOrNull,
            AsyncCallback<Iterable<DetailType>> asyncCallback);

    void canSliceRace(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Boolean> callback);

    void sliceRace(RegattaAndRaceIdentifier raceIdentifier, String newRaceColumnName, TimePoint sliceFrom, TimePoint sliceTo,
            AsyncCallback<RegattaAndRaceIdentifier> callback);

    void prepareForSlicingOfRace(RegattaAndRaceIdentifier raceIdentifier,
            AsyncCallback<SliceRacePreperationDTO> callback);
    
    /**
     * For a given WindFinder spot ID provides a {@link URL} that a web UI can use to link
     * to the WindFinder web site with the content most appropriate given the time point.
     * This could be the report page if the time is about now; the forecast page if the time point
     * is up to ten days in the future, or the statistics page if the time point is out of any
     * of the scopes above.
     */
    void getWindFinderSpot(String spotId, AsyncCallback<SpotDTO> callback);

    /**
     * @see SailingService#checkIfRaceIsTracking(RegattaAndRaceIdentifier)
     */
    void checkIfRaceIsTracking(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Boolean> asyncCallback);
}
