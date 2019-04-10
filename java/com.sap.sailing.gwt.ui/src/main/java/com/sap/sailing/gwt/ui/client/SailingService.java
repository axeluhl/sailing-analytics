package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.shiro.authz.UnauthorizedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogTagEvent;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.tracking.RaceLogDenoteForTrackingEvent;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.CompetitorDescriptor;
import com.sap.sailing.domain.common.CompetitorRegistrationType;
import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.LegIdentifier;
import com.sap.sailing.domain.common.MailInvitationType;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.NotFoundException;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.ServiceException;
import com.sap.sailing.domain.common.UnableToCloseDeviceMappingException;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.abstractlog.NotRevokableException;
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
import com.sap.sailing.domain.common.dto.TagDTO;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.common.racelog.tracking.CompetitorRegistrationOnRaceLogDisabledException;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.racelog.tracking.TransformationException;
import com.sap.sailing.domain.common.tracking.impl.PreciseCompactGPSFixMovingImpl.PreciseCompactPosition;
import com.sap.sailing.domain.common.windfinder.SpotDTO;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.racelog.tracking.SensorFixStore;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.expeditionconnector.ExpeditionDeviceConfiguration;
import com.sap.sailing.gwt.ui.adminconsole.RaceLogSetTrackingTimesDTO;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTrackDTO;
import com.sap.sailing.gwt.ui.client.shared.charts.MarkPositionService.MarkTracksDTO;
import com.sap.sailing.gwt.ui.shared.AccountWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompactBoatPositionsDTO;
import com.sap.sailing.gwt.ui.shared.CompactRaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorProviderDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO.RegattaConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.DeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.MigrateGroupOwnerForHierarchyDTO;
import com.sap.sailing.gwt.ui.shared.QRCodeEvent;
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
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.SerializationDummy;
import com.sap.sailing.gwt.ui.shared.ServerConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SimulatorResultsDTO;
import com.sap.sailing.gwt.ui.shared.SliceRacePreperationDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sailing.gwt.ui.shared.SwissTimingArchiveConfigurationWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingEventRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.TracTracConfigurationWithSecurityDTO;
import com.sap.sailing.gwt.ui.shared.TracTracRaceRecordDTO;
import com.sap.sailing.gwt.ui.shared.TrackFileImportDeviceIdentifierDTO;
import com.sap.sailing.gwt.ui.shared.TypedDeviceMappingDTO;
import com.sap.sailing.gwt.ui.shared.VenueDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.NoCorrespondingServiceRegisteredException;
import com.sap.sse.common.PairingListCreationException;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.common.media.MediaTagConstants;
import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.gwt.client.filestorage.FileStorageManagementGwtService;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.ImageResizingTaskDTO;
import com.sap.sse.gwt.client.media.VideoDTO;
import com.sap.sse.gwt.client.replication.RemoteReplicationService;
import com.sap.sse.security.interfaces.UserStore;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.ui.shared.SuccessInfo;

/**
 * The client side stub for the RPC service. Usually, when a <code>null</code> date is passed to the time-dependent
 * service methods, an empty (non-<code>null</code>) result is returned.
 */
public interface SailingService extends RemoteService, FileStorageManagementGwtService, RemoteReplicationService {
    List<TracTracConfigurationWithSecurityDTO> getPreviousTracTracConfigurations()
            throws UnauthorizedException, Exception;

    void updateTracTracConfiguration(TracTracConfigurationWithSecurityDTO tracTracConfiguration)
            throws UnauthorizedException, Exception;

    void deleteTracTracConfiguration(TracTracConfigurationWithSecurityDTO tracTracConfiguration)
            throws UnauthorizedException, Exception;

    List<RegattaDTO> getRegattas() throws UnauthorizedException;

    List<RegattaDTO> getRegattasWithUpdatePermission() throws UnauthorizedException;

    RegattaDTO getRegattaByName(String regattaName) throws UnauthorizedException;

    List<EventDTO> getEvents() throws UnauthorizedException, Exception;

    List<EventBaseDTO> getPublicEventsOfAllSailingServers() throws UnauthorizedException, Exception;

    Util.Pair<String, List<TracTracRaceRecordDTO>> listTracTracRacesInEvent(String eventJsonURL,
            boolean listHiddenRaces) throws UnauthorizedException, Exception;

    void trackWithTracTrac(RegattaIdentifier regattaToAddTo, Iterable<TracTracRaceRecordDTO> rrs, String liveURI,
            String storedURI, String courseDesignUpdateURI, boolean trackWind, boolean correctWindByDeclination,
            Duration offsetToStartTimeOfSimulatedRace, boolean useInternalMarkPassingAlgorithm, String tracTracUsername,
            String tracTracPassword) throws UnauthorizedException, Exception;

    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, Iterable<SwissTimingRaceRecordDTO> rrs, String hostname,
            int port, boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm,
            String updateURL, String updateUsername, String updatePassword) throws UnauthorizedException, Exception;

    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, Iterable<SwissTimingReplayRaceDTO> replayRaces,
            boolean trackWind, boolean correctWindByDeclination, boolean useInternalMarkPassingAlgorithm)
            throws UnauthorizedException;

    void createTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            String courseDesignUpdateURI, String tracTracUsername, String tracTracPassword)
            throws UnauthorizedException, Exception;

    void stopTrackingRaces(Iterable<RegattaAndRaceIdentifier> racesToStopTracking)
            throws UnauthorizedException, Exception;

    void removeAndUntrackRaces(Iterable<RegattaAndRaceIdentifier> regattaNamesAndRaceNames)
            throws UnauthorizedException;

    WindInfoForRaceDTO getRawWindFixes(RegattaAndRaceIdentifier raceIdentifier, Collection<WindSource> windSources)
            throws UnauthorizedException;

    void setWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO wind) throws UnauthorizedException;

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from,
            long millisecondsStepWidth, int numberOfFixes, Collection<String> windSourceTypeNames,
            boolean onlyUpToNewestEvent, boolean includeCombinedWindForAllLegMiddles)
            throws NoWindException, UnauthorizedException;

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from, Date to,
            long resolutionInMilliseconds, Collection<String> windSourceTypeNames, boolean onlyUpToNewestEvent)
            throws UnauthorizedException;

    WindInfoForRaceDTO getAveragedWindInfo(RegattaAndRaceIdentifier raceIdentifier, Date from,
            long millisecondsStepWidth, int numberOfFixes, double latDeg, double lngDeg, Collection<String> windSources)
            throws NoWindException, UnauthorizedException;

    boolean getPolarResults(RegattaAndRaceIdentifier raceIdentifier) throws UnauthorizedException;

    SimulatorResultsDTO getSimulatorResults(LegIdentifier legIdentifier) throws UnauthorizedException;

    RaceboardDataDTO getRaceboardData(String regattaName, String raceName, String leaderboardName,
            String leaderboardGroupName, UUID eventId) throws UnauthorizedException;

    Map<CompetitorDTO, BoatDTO> getCompetitorBoats(RegattaAndRaceIdentifier raceIdentifier)
            throws UnauthorizedException;

    CompactRaceMapDataDTO getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate, LegIdentifier simulationLegIdentifier,
            byte[] md5OfIdsAsStringOfCompetitorParticipatingInRaceInAlphanumericOrderOfTheirID,
            Date timeToGetTheEstimatedDurationFor, boolean estimatedDurationRequired)
            throws NoWindException, UnauthorizedException;

    CompactBoatPositionsDTO getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<String, Date> fromPerCompetitorIdAsString, Map<String, Date> toPerCompetitorIdAsString,
            boolean extrapolate) throws NoWindException, UnauthorizedException;

    RaceTimesInfoDTO getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier) throws UnauthorizedException;

    /**
     * Returns {@link RaceTimesInfoDTO race times info} for specified race (<code>raceIdentifier</code>) including
     * {@link RaceLogTagEvent tag events} since received timestamp (<code>searchSince</code>). Loads tags from
     * {@link ReadonlyRaceState cache} instead of scanning the whole {@link RaceLog} every request.
     */
    RaceTimesInfoDTO getRaceTimesInfoIncludingTags(RegattaAndRaceIdentifier raceIdentifier, TimePoint searchSince)
            throws UnauthorizedException;

    List<RaceTimesInfoDTO> getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers)
            throws UnauthorizedException;

    /**
     * Collects besides {@link RaceTimesInfoDTO race times infos} public {@link RaceLogTagEvent tag events} from
     * {@link ReadonlyRaceState cache} and compares the <code>createdAt</code> timepoint to the received
     * <code>searchSince</code> timepoint. Returns {@link RaceTimesInfoDTO race times infos} including
     * {@link RaceLogTagEvent public tag events} since the latest client-side received tag.
     */
    List<RaceTimesInfoDTO> getRaceTimesInfosIncludingTags(Collection<RegattaAndRaceIdentifier> raceIdentifiers,
            Map<RegattaAndRaceIdentifier, TimePoint> searchSinceMap) throws UnauthorizedException;

    CoursePositionsDTO getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date)
            throws UnauthorizedException;

    RaceCourseDTO getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date) throws UnauthorizedException;

    void removeWind(RegattaAndRaceIdentifier raceIdentifier, WindDTO windDTO) throws UnauthorizedException;

    public List<String> getLeaderboardNames() throws UnauthorizedException;

    IncrementalOrFullLeaderboardDTO getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails, boolean addOverallDetails,
            String previousLeaderboardId, boolean fillTotalPointsUncorrected) throws UnauthorizedException, Exception;

    IncrementalOrFullLeaderboardDTO getLeaderboardForRace(RegattaAndRaceIdentifier raceIdentifer,
            String leaderboardName, Date date, Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            boolean addOverallDetails, String previousLeaderboardId, boolean fillTotalPointsUncorrected)
            throws UnauthorizedException, Exception;

    List<StrippedLeaderboardDTOWithSecurity> getLeaderboardsWithSecurity() throws UnauthorizedException;

    StrippedLeaderboardDTOWithSecurity updateLeaderboard(String leaderboardName, String newLeaderboardName,
            String newLeaderboardDisplayName, int[] newDiscardingThreasholds, UUID newCourseAreaId)
            throws UnauthorizedException;

    StrippedLeaderboardDTOWithSecurity createFlexibleLeaderboard(String leaderboardName, String leaderboardDisplayName,
            int[] discardThresholds, ScoringSchemeType scoringSchemeType, UUID courseAreaId)
            throws UnauthorizedException;

    StrippedLeaderboardDTOWithSecurity createRegattaLeaderboard(RegattaName regattaIdentifier,
            String leaderboardDisplayName, int[] discardThresholds) throws UnauthorizedException;

    StrippedLeaderboardDTOWithSecurity createRegattaLeaderboardWithEliminations(String name, String displayName,
            String regattaName) throws UnauthorizedException;

    void removeLeaderboard(String leaderboardName) throws UnauthorizedException;

    void removeLeaderboards(Collection<String> leaderboardNames) throws UnauthorizedException;

    void renameLeaderboard(String leaderboardName, String newLeaderboardName) throws UnauthorizedException;

    void renameLeaderboardColumn(String leaderboardName, String oldColumnName, String newColumnName)
            throws UnauthorizedException;

    void removeLeaderboardColumn(String leaderboardName, String columnName) throws UnauthorizedException;

    void addColumnToLeaderboard(String columnName, String leaderboardName, boolean medalRace)
            throws UnauthorizedException;

    void moveLeaderboardColumnUp(String leaderboardName, String columnName) throws UnauthorizedException;

    void moveLeaderboardColumnDown(String leaderboardName, String columnName) throws UnauthorizedException;

    RegattaDTO createRegatta(String regattaName, String boatClassName, boolean canBoatsOfCompetitorsChangePerRace,
            CompetitorRegistrationType competitorRegistrationType, String registrationLinkSecret, Date startDate,
            Date endDate, RegattaCreationParametersDTO seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType, UUID defaultCourseAreaId,
            Double buoyZoneRadiusInHullLengths, boolean useStartTimeInference,
            boolean controlTrackingFromStartAndFinishTimes, RankingMetrics rankingMetricType)
            throws UnauthorizedException;

    void removeRegatta(RegattaIdentifier regattaIdentifier) throws UnauthorizedException;

    void removeSeries(RegattaIdentifier regattaIdentifier, String seriesName) throws UnauthorizedException;

    void removeRegattas(Collection<RegattaIdentifier> regattas) throws UnauthorizedException;

    void updateRegatta(RegattaIdentifier regattaIdentifier, Date startDate, Date endDate, UUID defaultCourseAreaUuid,
            RegattaConfigurationDTO regattaConfiguration, Double buoyZoneRadiusInHullLengths,
            boolean useStartTimeInference, boolean controlTrackingFromStartAndFinishTimes,
            String registrationLinkSecret, CompetitorRegistrationType registrationType) throws UnauthorizedException;

    List<RaceColumnInSeriesDTO> addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName,
            List<Pair<String, Integer>> columnNames) throws UnauthorizedException;

    void updateSeries(RegattaIdentifier regattaIdentifier, String seriesName, String newSeriesName, boolean isMedal,
            boolean isFleetsCanRunInParallel, int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstRaceIsNonDiscardableCarryForward, boolean hasSplitFleetScore, Integer maximumNumberOfDiscards,
            List<FleetDTO> fleets) throws UnauthorizedException;

    void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames)
            throws UnauthorizedException;

    void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName)
            throws UnauthorizedException;

    void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName)
            throws UnauthorizedException;

    boolean connectTrackedRaceToLeaderboardColumn(String leaderboardName, String raceColumnName, String fleetName,
            RegattaAndRaceIdentifier raceIdentifier) throws UnauthorizedException;

    void disconnectLeaderboardColumnFromTrackedRace(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    Map<String, RegattaAndRaceIdentifier> getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(
            String leaderboardName, String raceColumnName) throws UnauthorizedException;

    void updateLeaderboardCarryValue(String leaderboardName, String competitorIdAsString, Double carriedPoints)
            throws UnauthorizedException;

    /**
     * @return the new net points in {@link Pair#getA()} and the new total points in {@link Pair#getB()} for time point
     *         <code>date</code> after the max points reason has been updated to <code>maxPointsReasonAsString</code>.
     */
    Util.Triple<Double, Double, Boolean> updateLeaderboardMaxPointsReason(String leaderboardName,
            String competitorIdAsString, String raceColumnName, MaxPointsReason maxPointsReason, Date date)
            throws NoWindException, UnauthorizedException;

    Util.Triple<Double, Double, Boolean> updateLeaderboardScoreCorrection(String leaderboardName,
            String competitorIdAsString, String columnName, Double correctedScore, Date date)
            throws NoWindException, UnauthorizedException;

    void updateCompetitorDisplayNameInLeaderboard(String leaderboardName, String competitorIdAsString,
            String displayName) throws UnauthorizedException;

    void updateIsMedalRace(String leaderboardName, String columnName, boolean isMedalRace) throws UnauthorizedException;

    List<SwissTimingConfigurationDTO> getPreviousSwissTimingConfigurations() throws UnauthorizedException;

    SwissTimingEventRecordDTO getRacesOfSwissTimingEvent(String eventJsonURL) throws UnauthorizedException, Exception;

    void storeSwissTimingConfiguration(String configName, String jsonURL, String hostname, Integer port,
            String updateURL, String updateUsername, String updatePassword) throws UnauthorizedException, Exception;

    String[] getCountryCodes() throws UnauthorizedException;

    Map<CompetitorDTO, List<GPSFixDTOWithSpeedWindTackAndLegType>> getDouglasPoints(
            RegattaAndRaceIdentifier raceIdentifier, Map<CompetitorDTO, TimeRange> competitorTimeRanges, double meters)
            throws NoWindException, UnauthorizedException;

    Map<CompetitorDTO, List<ManeuverDTO>> getManeuvers(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, TimeRange> competitorTimeRanges) throws NoWindException, UnauthorizedException;

    List<StrippedLeaderboardDTO> getLeaderboardsByRaceAndRegatta(String raceName, RegattaIdentifier regattaIdentifier)
            throws UnauthorizedException;

    List<LeaderboardGroupDTO> getLeaderboardGroups(boolean withGeoLocationData) throws UnauthorizedException;

    LeaderboardGroupDTO getLeaderboardGroupByName(String groupName, boolean withGeoLocationData)
            throws UnauthorizedException;

    void renameLeaderboardGroup(String oldName, String newName) throws UnauthorizedException;

    void removeLeaderboardGroups(Set<String> groupNames) throws UnauthorizedException;

    LeaderboardGroupDTO createLeaderboardGroup(String groupName, String description, String displayName,
            boolean displayGroupsInReverseOrder, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType) throws UnauthorizedException;

    void updateLeaderboardGroup(UUID leaderboardGroupId, String oldName, String newName, String description,
            String newDisplayName, List<String> leaderboardNames, int[] overallLeaderboardDiscardThresholds,
            ScoringSchemeType overallLeaderboardScoringSchemeType) throws UnauthorizedException;

    CompetitorsRaceDataDTO getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors,
            Date from, Date to, long stepSizeInMs, DetailType detailType, String leaderboardGroupName,
            String leaderboardName) throws NoWindException, UnauthorizedException;

    void setRaceIsKnownToStartUpwind(RegattaAndRaceIdentifier raceIdentifier, boolean raceIsKnownToStartUpwind)
            throws UnauthorizedException;

    void setWindSourcesToExclude(RegattaAndRaceIdentifier raceIdentifier, Iterable<WindSource> windSourcesToExclude)
            throws UnauthorizedException;

    void updateRaceDelayToLive(RegattaAndRaceIdentifier regattaAndRaceIdentifier, long delayToLiveInMs)
            throws UnauthorizedException;

    void updateRacesDelayToLive(List<RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, long delayToLiveInMs)
            throws UnauthorizedException;

    Pair<Integer, Integer> resolveImageDimensions(String imageUrlAsString) throws UnauthorizedException, Exception;

    EventDTO updateEvent(UUID eventId, String eventName, String eventDescription, Date startDate, Date endDate,
            VenueDTO venue, boolean isPublic, Iterable<UUID> leaderboardGroupIds, String officialWebsiteURL,
            String baseURL, Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images,
            Iterable<VideoDTO> videos, Iterable<String> windFinderReviewedSpotCollectionIds)
            throws UnauthorizedException, Exception;

    EventDTO createEvent(String eventName, String eventDescription, Date startDate, Date endDate, String venue,
            boolean isPublic, List<String> courseAreaNames, String officialWebsiteURL, String baseURLAsString,
            Map<String, String> sailorsInfoWebsiteURLsByLocaleName, Iterable<ImageDTO> images,
            Iterable<VideoDTO> videos, Iterable<UUID> leaderboardGroupIds) throws UnauthorizedException, Exception;

    void removeEvent(UUID eventId) throws UnauthorizedException;

    void removeEvents(Collection<UUID> eventIds) throws UnauthorizedException;

    void renameEvent(UUID eventId, String newName) throws UnauthorizedException;

    EventDTO getEventById(UUID id, boolean withStatisticalData) throws UnauthorizedException, Exception;

    Iterable<String> getScoreCorrectionProviderNames() throws UnauthorizedException;

    ScoreCorrectionProviderDTO getScoreCorrectionsOfProvider(String providerName)
            throws UnauthorizedException, Exception;

    RegattaScoreCorrectionDTO getScoreCorrections(String scoreCorrectionProviderName, String eventName,
            String boatClassName, Date timePointWhenResultPublished) throws UnauthorizedException, Exception;

    void updateLeaderboardScoreCorrectionsAndMaxPointsReasons(BulkScoreCorrectionDTO updates)
            throws UnauthorizedException, NoWindException;

    Iterable<String> getCompetitorProviderNames() throws UnauthorizedException;

    CompetitorProviderDTO getCompetitorProviderDTOByName(String providerName) throws UnauthorizedException, Exception;

    List<CompetitorDescriptor> getCompetitorDescriptors(String competitorProviderName, String eventName,
            String regattaName) throws UnauthorizedException, Exception;

    WindInfoForRaceDTO getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier) throws UnauthorizedException;

    ServerConfigurationDTO getServerConfiguration() throws UnauthorizedException;

    void updateServerConfiguration(ServerConfigurationDTO serverConfiguration) throws UnauthorizedException;

    List<RemoteSailingServerReferenceDTO> getRemoteSailingServerReferences() throws UnauthorizedException;

    void removeSailingServers(Set<String> toRemove) throws UnauthorizedException, Exception;

    RemoteSailingServerReferenceDTO addRemoteSailingServerReference(RemoteSailingServerReferenceDTO sailingServer)
            throws UnauthorizedException, Exception;

    List<String> getResultImportUrls(String resultProviderName) throws UnauthorizedException;

    void removeResultImportURLs(String resultProviderName, Set<String> toRemove)
            throws UnauthorizedException, Exception;

    void addResultImportUrl(String resultProviderName, String url) throws UnauthorizedException, Exception;

    void updateLeaderboardScoreCorrectionMetadata(String leaderboardName, Date timePointOfLastCorrectionValidity,
            String comment) throws UnauthorizedException;

    List<Pair<String, String>> getUrlResultProviderNamesAndOptionalSampleURL() throws UnauthorizedException;

    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier,
            List<Util.Pair<ControlPointDTO, PassingInstruction>> controlPoints) throws UnauthorizedException;

    void addColumnsToLeaderboard(String leaderboardName, List<Util.Pair<String, Boolean>> columnsToAdd)
            throws UnauthorizedException;

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove) throws UnauthorizedException;

    StrippedLeaderboardDTO getLeaderboard(String leaderboardName) throws UnauthorizedException;

    StrippedLeaderboardDTOWithSecurity getLeaderboardWithSecurity(String leaderboardName) throws UnauthorizedException;

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed)
            throws UnauthorizedException;

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor)
            throws UnauthorizedException;

    List<SwissTimingReplayRaceDTO> listSwissTiminigReplayRaces(String swissTimingUrl) throws UnauthorizedException;

    List<Triple<String, List<CompetitorDTO>, List<Double>>> getLeaderboardDataEntriesForAllRaceColumns(
            String leaderboardName, Date date, DetailType detailType) throws UnauthorizedException, Exception;

    List<String> getOverallLeaderboardNamesContaining(String leaderboardName) throws UnauthorizedException;

    List<SwissTimingArchiveConfigurationWithSecurityDTO> getPreviousSwissTimingArchiveConfigurations() throws UnauthorizedException;

    void createSwissTimingArchiveConfiguration(String jsonUrl)
            throws UnauthorizedException, Exception;

    void updateSwissTimingArchiveConfiguration(SwissTimingArchiveConfigurationWithSecurityDTO dto)
            throws UnauthorizedException, Exception;

    void deleteSwissTimingArchiveConfiguration(SwissTimingArchiveConfigurationWithSecurityDTO dto)
            throws UnauthorizedException, Exception;

    void createCourseAreas(UUID eventId, String[] courseAreaNames) throws UnauthorizedException;

    void removeCourseAreas(UUID eventId, UUID[] courseAreaIds) throws UnauthorizedException;

    List<Util.Pair<String, String>> getLeaderboardsNamesOfMetaLeaderboard(String metaLeaderboardName)
            throws UnauthorizedException;

    LeaderboardType getLeaderboardType(String leaderboardName) throws UnauthorizedException;

    /** for backward compatibility with the regatta overview */
    List<RaceGroupDTO> getRegattaStructureForEvent(UUID eventId) throws UnauthorizedException;

    /** the replacement service for getRegattaStructureForEvent() */
    List<RaceGroupDTO> getRegattaStructureOfEvent(UUID eventId) throws UnauthorizedException;

    List<RegattaOverviewEntryDTO> getRaceStateEntriesForRaceGroup(UUID eventId, List<UUID> visibleCourseAreas,
            List<String> visibleRegattas, boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay,
            Duration clientTimeZoneOffset) throws UnauthorizedException, Exception;

    List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(String leaderboardName,
            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, Duration clientTimeZoneOffset,
            List<String> visibleRegattas) throws UnauthorizedException, Exception;

    void reloadRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet)
            throws UnauthorizedException, NotFoundException;

    RaceLogDTO getRaceLog(String leaderboardName, RaceColumnDTO raceColumnDTO, FleetDTO fleet)
            throws UnauthorizedException;

    RegattaLogDTO getRegattaLog(String leaderboardName) throws UnauthorizedException, DoesNotHaveRegattaLogException;

    List<String> getLeaderboardGroupNamesFromRemoteServer(String url, String username, String password)
            throws UnauthorizedException;

    UUID importMasterData(String host, String[] groupNames, boolean override, boolean compress, boolean exportWind,
            boolean exportDeviceConfigurations, String targetServerUsername, String targetServerPassword)
            throws UnauthorizedException;

    DataImportProgress getImportOperationProgress(UUID id) throws UnauthorizedException;

    Iterable<CompetitorDTO> getCompetitors(boolean filterCompetitorsWithBoat, boolean filterCompetitorsWithoutBoat)
            throws UnauthorizedException;

    Iterable<CompetitorDTO> getCompetitorsOfLeaderboard(String leaderboardName) throws UnauthorizedException;

    Map<? extends CompetitorDTO, BoatDTO> getCompetitorsAndBoatsOfRace(String leaderboardName, String raceColumnName,
            String fleetName) throws UnauthorizedException, NotFoundException;

    List<CompetitorDTO> addOrUpdateCompetitors(List<CompetitorDTO> competitors) throws UnauthorizedException, Exception;

    CompetitorWithBoatDTO addOrUpdateCompetitorWithBoat(CompetitorWithBoatDTO competitor)
            throws UnauthorizedException, Exception;

    CompetitorDTO addOrUpdateCompetitorWithoutBoat(CompetitorDTO competitor) throws UnauthorizedException, Exception;

    List<CompetitorWithBoatDTO> addCompetitors(List<CompetitorDescriptor> competitorsForSaving, String searchTag)
            throws UnauthorizedException, Exception;

    void allowCompetitorResetToDefaults(Iterable<CompetitorDTO> competitors) throws UnauthorizedException;

    Iterable<BoatDTO> getAllBoats() throws UnauthorizedException;

    Iterable<BoatDTO> getStandaloneBoats() throws UnauthorizedException;

    BoatDTO addOrUpdateBoat(BoatDTO boat) throws UnauthorizedException, Exception;

    void allowBoatResetToDefaults(Iterable<BoatDTO> boats) throws UnauthorizedException;

    boolean linkBoatToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName,
            String competitorIdAsString, String boatIdAsString) throws UnauthorizedException, NotFoundException;

    boolean unlinkBoatFromCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName,
            String competitorIdAsString) throws UnauthorizedException, NotFoundException;

    BoatDTO getBoatLinkedToCompetitorForRace(String leaderboardName, String raceColumnName, String fleetName,
            String competitorIdAsString) throws UnauthorizedException, NotFoundException;

    List<DeviceConfigurationWithSecurityDTO> getDeviceConfigurations() throws UnauthorizedException;

    DeviceConfigurationDTO getDeviceConfiguration(UUID id) throws UnauthorizedException;

    void createOrUpdateDeviceConfiguration(DeviceConfigurationDTO configurationDTO) throws UnauthorizedException;

    boolean removeDeviceConfiguration(UUID deviceConfigurationId) throws UnauthorizedException;

    boolean setStartTimeAndProcedure(RaceLogSetStartTimeAndProcedureDTO dto)
            throws UnauthorizedException, NotFoundException;

    Pair<Boolean, Boolean> setFinishingAndEndTime(RaceLogSetFinishingAndFinishTimeDTO dto)
            throws UnauthorizedException, NotFoundException;

    Util.Triple<Date, Integer, RacingProcedureType> getStartTimeAndProcedure(String leaderboardName,
            String raceColumnName, String fleetName) throws UnauthorizedException, NotFoundException;

    Util.Triple<Date, Date, Integer> getFinishingAndFinishTime(String leaderboardName, String raceColumnName,
            String fleetName) throws UnauthorizedException, NotFoundException;

    Iterable<String> getAllIgtimiAccountEmailAddresses() throws UnauthorizedException;

    String getIgtimiAuthorizationUrl(String redirectProtocol, String redirectHostname, String redirectPort)
            throws UnauthorizedException, Exception;

    boolean authorizeAccessToIgtimiUser(String eMailAddress, String password) throws UnauthorizedException, Exception;

    void removeIgtimiAccount(String eMailOfAccountToRemove) throws UnauthorizedException;

    Map<RegattaAndRaceIdentifier, Integer> importWindFromIgtimi(List<RaceDTO> selectedRaces,
            boolean correctByDeclination) throws UnauthorizedException, Exception;

    Boolean denoteForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, Exception;

    /**
     * Revoke the {@link RaceLogDenoteForTrackingEvent}. This does not affect an existing {@code RaceLogRaceTracker} or
     * {@link TrackedRace} for this {@code RaceLog}.
     * 
     * @throws UnauthorizedException,
     *             NotFoundException
     * 
     * @see RaceLogTrackingAdapter#removeDenotationForRaceLogTracking
     */
    void removeDenotationForRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, NotFoundException;

    void denoteForRaceLogTracking(String leaderboardName) throws UnauthorizedException, Exception;

    void denoteForRaceLogTracking(String leaderboardName, String prefix) throws UnauthorizedException, Exception;

    /**
     * Performs all the necessary steps to start tracking the race. The {@code RaceLog} needs to be denoted for
     * racelog-tracking beforehand.
     * 
     * @see RaceLogTrackingAdapter#startTracking
     */
    void startRaceLogTracking(String leaderboardName, String raceColumnName, String fleetName, boolean trackWind,
            boolean correctWindByDeclination)
            throws UnauthorizedException, NotDenotedForRaceLogTrackingException, Exception;

    void startRaceLogTracking(List<Triple<String, String, String>> leaderboardRaceColumnFleetNames, boolean trackWind,
            boolean correctWindByDeclination)
            throws UnauthorizedException, NotDenotedForRaceLogTrackingException, Exception;

    void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Set<CompetitorWithBoatDTO> competitors)
            throws UnauthorizedException, CompetitorRegistrationOnRaceLogDisabledException, NotFoundException;

    void setCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            Map<? extends CompetitorDTO, BoatDTO> competitorsAndBoats)
            throws UnauthorizedException, CompetitorRegistrationOnRaceLogDisabledException, NotFoundException;

    /**
     * Adds the course definition to the racelog, while trying to reuse existing marks, controlpoints and waypoints from
     * the previous course definition in the racelog.
     * 
     * @param priority
     *            TODO
     * @throws UnauthorizedException,
     *             NotFoundException
     */
    void addCourseDefinitionToRaceLog(String leaderboardName, String raceColumnName, String fleetName,
            List<Util.Pair<ControlPointDTO, PassingInstruction>> course, int priority)
            throws UnauthorizedException, NotFoundException;

    /**
     * Adds public tag as {@link RaceLogTagEvent} to {@link RaceLog} and private tag to
     * {@link com.sap.sse.security.interfaces.UserStore UserStore}.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tag
     *            title of tag, must <b>NOT</b> be <code>null</code>
     * @param comment
     *            optional comment of tag
     * @param imageURLs
     *            optional image URLs of tag
     * @param visibleForPublic
     *            when set to <code>true</code> tag will be saved as public tag (visible for every user), when set to
     *            <code>false</code> tag will be saved as private tag (visible only for creator)
     * @param raceTimepoint
     *            timepoint in race where user created tag, must <b>NOT</b> be <code>null</code>
     * @return <code>successful</code> {@link SuccessInfo} if tag was added successfully, otherwise
     *         <code>non-successful</code> {@link SuccessInfo}
     */
    SuccessInfo addTag(String leaderboardName, String raceColumnName, String fleetName, String tag, String comment,
            String imageURL, String resizedImageURL, boolean visibleForPublic, TimePoint raceTimepoint)
            throws UnauthorizedException;

    /**
     * Removes public {@link TagDTO tag} from {@link RaceLog} and private {@link TagDTO tag} from {@link UserStore}.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tag
     *            tag to remove
     * @return <code>successful</code> {@link SuccessInfo} if tag was removed successfully, otherwise
     *         <code>non-successful</code> {@link SuccessInfo}
     */
    SuccessInfo removeTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tag)
            throws UnauthorizedException;

    /**
     * Updates given <code>tagToUpdate</code> with the given attributes <code>tag</code>, <code>comment</code>,
     * <code>imageURL</code> and <code>visibleForPublic</code>. Tags are not really updated, instead public tags are
     * revoked/private tags removed first and then the new tags gets saved depending on the new value
     * <code>visibleForPublic</code>.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param tagToUpdate
     *            tag to be updated
     * @param tag
     *            new tag title
     * @param comment
     *            new comment
     * @param imageURL
     *            new image url
     * @param visibleForPublic
     *            new visibility status
     * @return <code>successful</code> {@link SuccessInfo} if tag was updated successfully, otherwise
     *         <code>non-successful</code> {@link SuccessInfo}
     */
    SuccessInfo updateTag(String leaderboardName, String raceColumnName, String fleetName, TagDTO tagToUpdate,
            String tag, String comment, String imageURL, String resizedImageURL, boolean visibleForPublic)
            throws UnauthorizedException;

    /**
     * Returns all public and private tags of specified race and current user.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of {@link TagDTO tags}
     */
    List<TagDTO> getAllTags(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    /**
     * Returns all public {@link TagDTO tags} of specified race.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of public {@link TagDTO tags}
     */
    List<TagDTO> getPublicTags(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    /**
     * Returns all private {@link TagDTO tags} of specified race and current user.
     * 
     * @param leaderboardName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param raceColumnName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @param fleetName
     *            required to identify {@link RaceLog}, must <b>NOT</b> be <code>null</code>
     * @return list of private {@link TagDTO tags}
     */
    List<TagDTO> getPrivateTags(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    RaceCourseDTO getLastCourseDefinitionInRaceLog(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, NotFoundException;

    /**
     * Adds a fix to the {@link SensorFixStore}, and creates a mapping with a virtual device for exactly the current
     * timepoint.
     * 
     * @param timePoint
     *            the time point for the fix; if {@code null}, the current time is used
     * 
     * @throws DoesNotHaveRegattaLogException
     * @throws NotFoundException
     */
    void pingMark(String leaderboardName, MarkDTO mark, TimePoint timePoint, Position position)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    List<String> getDeserializableDeviceIdentifierTypes() throws UnauthorizedException;

    /**
     * Revoke the events in the {@code RaceLog} that are identified by the {@code eventIds}. This only affects such
     * events that implement {@link Revokable}.
     * 
     * @throws NotFoundException
     */
    void revokeRaceAndRegattaLogEvents(String leaderboardName, String raceColumnName, String fleetName,
            List<UUID> eventIds) throws UnauthorizedException, NotRevokableException, NotFoundException;

    Collection<String> getGPSFixImporterTypes() throws UnauthorizedException;

    Collection<String> getSensorDataImporterTypes() throws UnauthorizedException;

    List<TrackFileImportDeviceIdentifierDTO> getTrackFileImportDeviceIds(List<String> uuids)
            throws NoCorrespondingServiceRegisteredException, TransformationException;

    /**
     * @return The RaceDTO of the modified race or <code>null</code>, if the given newStartTimeReceived was null.
     */
    RaceDTO setStartTimeReceivedForRace(RaceIdentifier raceIdentifier, Date newStartTimeReceived)
            throws UnauthorizedException;

    PolarSheetsXYDiagramData createXYDiagramForBoatClass(String itemText) throws UnauthorizedException;

    /**
     * @see SailingServiceAsync#getCompetitorMarkPassings(RegattaAndRaceIdentifier, CompetitorWithBoatDTO, boolean,
     *      com.google.gwt.user.client.rpc.AsyncCallback)
     */
    Map<Integer, Date> getCompetitorMarkPassings(RegattaAndRaceIdentifier race, CompetitorDTO competitorDTO,
            boolean waitForCalculations) throws UnauthorizedException;

    /**
     * Obtains fixed mark passings and mark passing suppressions from the race log identified by
     * <code>leaderboardName</code>, <code>raceColumnDTO</code> and <code>fleet</code>. The result contains pairs of
     * zero-based waypoint numbers and times where <code>null</code> represents a suppressed mark passing and a valid
     * {@link Date} objects represents a fixed mark passing.
     * 
     * @throws NotFoundException
     */
    Map<Integer, Date> getCompetitorRaceLogMarkPassingData(String leaderboardName, String raceColumnName,
            String fleetName, CompetitorDTO competitor) throws UnauthorizedException, NotFoundException;

    void updateSuppressedMarkPassings(String leaderboardName, String raceColumnName, String fleetName,
            Integer newZeroBasedIndexOfSuppressedMarkPassing, CompetitorDTO competitorDTO)
            throws UnauthorizedException, NotFoundException;

    void updateFixedMarkPassing(String leaderboardName, String raceColumnName, String fleetName,
            Integer indexOfWaypoint, Date dateOfMarkPassing, CompetitorDTO competitorDTO)
            throws UnauthorizedException, NotFoundException;

    void setCompetitorRegistrationsInRegattaLog(String leaderboardName, Set<? extends CompetitorDTO> competitors)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    /**
     * A leaderboard may be situated under multiple events (connected via a leaderboardgroup). This method traverses all
     * events and leaderboardgroup to build the collection of events this leaderboard is coupled to.
     */
    Collection<EventDTO> getEventsForLeaderboard(String leaderboardName) throws UnauthorizedException;

    /**
     * Imports regatta structure definitions from an ISAF XRR document
     * 
     * @param manage2SailJsonUrl
     *            the URL pointing to a Manage2Sail JSON document that contains the link to the XRR document
     */
    Iterable<RegattaDTO> getRegattas(String manage2SailJsonUrl) throws UnauthorizedException;

    void createRegattaStructure(Iterable<RegattaDTO> regattas, EventDTO newEvent)
            throws UnauthorizedException, Exception;

    Integer getStructureImportOperationProgress() throws UnauthorizedException;

    void inviteCompetitorsForTrackingViaEmail(String serverUrlWithoutTrailingSlash, EventDTO event,
            String leaderboardName, Collection<CompetitorDTO> competitors, String iOSAppUrl, String androidAppUrl,
            String localeInfo) throws UnauthorizedException, MailException;

    void inviteBuoyTenderViaEmail(String serverUrlWithoutTrailingSlash, EventDTO eventDto, String leaderboardName,
            String emails, String iOSAppUrl, String androidAppUrl, String localeInfoName)
            throws UnauthorizedException, MailException;

    ArrayList<LeaderboardGroupDTO> getLeaderboardGroupsByEventId(UUID id) throws UnauthorizedException;

    Iterable<MarkDTO> getMarksInRegattaLog(String leaderboardName)
            throws UnauthorizedException, DoesNotHaveRegattaLogException;

    List<DeviceMappingDTO> getDeviceMappings(String leaderboardName)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, TransformationException, NotFoundException;

    void revokeRaceAndRegattaLogEvents(String leaderboardName, List<UUID> eventIds)
            throws UnauthorizedException, NotRevokableException, DoesNotHaveRegattaLogException, NotFoundException;

    void closeOpenEndedDeviceMapping(String leaderboardName, DeviceMappingDTO mappingDto, Date closingTimePoint)
            throws UnauthorizedException, TransformationException, DoesNotHaveRegattaLogException,
            UnableToCloseDeviceMappingException, NotFoundException;

    void addDeviceMappingToRegattaLog(String leaderboardName, DeviceMappingDTO dto) throws UnauthorizedException,
            NoCorrespondingServiceRegisteredException, TransformationException, DoesNotHaveRegattaLogException;

    void addTypedDeviceMappingToRegattaLog(String leaderboardName, TypedDeviceMappingDTO dto)
            throws UnauthorizedException, NoCorrespondingServiceRegisteredException, TransformationException,
            DoesNotHaveRegattaLogException, NotFoundException;

    boolean doesRegattaLogContainCompetitors(String name)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    RegattaAndRaceIdentifier getRaceIdentifier(String regattaLikeName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    void setTrackingTimes(RaceLogSetTrackingTimesDTO dto) throws UnauthorizedException, NotFoundException;

    Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> getTrackingTimes(String leaderboardName,
            String raceColumnName, String fleetName) throws UnauthorizedException, NotFoundException;

    /**
     * @param raceLogFrom
     *            identifies the race log to copy from by its leaderboard name, race column name and fleet name
     * @param raceLogsTo
     *            identifies the race log to copy from by their leaderboard name, race column name and fleet name
     * @throws NotFoundException
     */
    void copyCompetitorsToOtherRaceLogs(Triple<String, String, String> fromTriple,
            Set<Triple<String, String, String>> toTriples) throws UnauthorizedException, NotFoundException;

    /**
     * @param priority
     *            TODO
     * @param raceLogFrom
     *            identifies the race log to copy from by its leaderboard name, race column name and fleet name
     * @param raceLogsTo
     *            identifies the race log to copy from by their leaderboard name, race column name and fleet name
     * @throws NotFoundException
     */
    void copyCourseToOtherRaceLogs(Triple<String, String, String> fromTriple,
            Set<Triple<String, String, String>> toTriples, int priority)
            throws UnauthorizedException, NotFoundException;

    /**
     * Get the competitors registered for a certain race. This automatically checks, whether competitors are registered
     * in the raceLog (in case of e.g. splitFleets) or in the RegattaLog (default)
     * 
     * @throws NotFoundException
     */
    Collection<CompetitorAndBoatDTO> getCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName,
            String fleetName) throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    void addMarkToRegattaLog(String leaderboardName, MarkDTO mark)
            throws UnauthorizedException, DoesNotHaveRegattaLogException;

    void revokeMarkDefinitionEventInRegattaLog(String leaderboardName, MarkDTO markDTO)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    Collection<CompetitorDTO> getCompetitorRegistrationsInRegattaLog(String leaderboardName)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    Collection<BoatDTO> getBoatRegistrationsInRegattaLog(String leaderboardName)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    void setBoatRegistrationsInRegattaLog(String leaderboardName, Set<BoatDTO> boats)
            throws UnauthorizedException, DoesNotHaveRegattaLogException, NotFoundException;

    Collection<BoatDTO> getBoatRegistrationsForLeaderboard(String leaderboardName)
            throws UnauthorizedException, NotFoundException;

    Boolean areCompetitorRegistrationsEnabledForRace(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, NotFoundException;

    void disableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, NotRevokableException, NotFoundException;

    void enableCompetitorRegistrationsForRace(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException, IllegalArgumentException, NotFoundException;

    Pair<Boolean, String> checkIfMarksAreUsedInOtherRaceLogs(String leaderboardName, String raceColumnName,
            String fleetName, Set<MarkDTO> marksToRemove) throws UnauthorizedException, NotFoundException;

    Collection<CompetitorAndBoatDTO> getCompetitorRegistrationsInRaceLog(String leaderboardName, String raceColumnName,
            String fleetName) throws UnauthorizedException, NotFoundException;

    Map<CompetitorDTO, BoatDTO> getCompetitorAndBoatRegistrationsInRaceLog(String leaderboardName,
            String raceColumnName, String fleetName) throws UnauthorizedException, NotFoundException;

    Collection<CompetitorDTO> getCompetitorRegistrationsForLeaderboard(String leaderboardName)
            throws UnauthorizedException, NotFoundException;

    Iterable<MarkDTO> getMarksInTrackedRace(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    MarkTracksDTO getMarkTracks(String leaderboardName, String raceColumnName, String fleetName)
            throws UnauthorizedException;

    MarkTrackDTO getMarkTrack(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString)
            throws UnauthorizedException;

    /**
     * The service may decide whether a mark fix can be removed. This is generally possible if there is a mark device
     * mapping that can be manipulated in such a way that the {@code fix} will no longer be mapped.
     */
    boolean canRemoveMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO fix) throws UnauthorizedException;

    void removeMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO fix) throws UnauthorizedException, NotRevokableException;

    void addMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO newFix) throws UnauthorizedException;

    void editMarkFix(String leaderboardName, String raceColumnName, String fleetName, String markIdAsString,
            GPSFixDTO oldFix, Position newPosition) throws UnauthorizedException, NotRevokableException;

    Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> getTrackingTimes(
            Collection<Triple<String, String, String>> raceColumnsAndFleets) throws UnauthorizedException;

    SerializationDummy serializationDummy(PersonDTO dummy, CountryCode ccDummy,
            PreciseCompactPosition preciseCompactPosition, TypeRelativeObjectIdentifier typeRelativeObjectIdentifier)
            throws UnauthorizedException;

    Collection<CompetitorDTO> getEliminatedCompetitors(String leaderboardName) throws UnauthorizedException;

    void setEliminatedCompetitors(String leaderboardName, Set<CompetitorDTO> eliminatedCompetitors)
            throws UnauthorizedException, NotFoundException;

    /**
     * Used to determine for a Chart the available Detailtypes. This is for example used to only show the RideHeight as
     * an option for charts if it actually recorded for the race.
     */
    Iterable<DetailType> determineDetailTypesForCompetitorChart(String leaderboardGroupName,
            RegattaAndRaceIdentifier identifier) throws UnauthorizedException;

    List<ExpeditionDeviceConfiguration> getExpeditionDeviceConfigurations() throws UnauthorizedException;

    void addOrReplaceExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration)
            throws UnauthorizedException;

    void removeExpeditionDeviceConfiguration(ExpeditionDeviceConfiguration expeditionDeviceConfiguration)
            throws UnauthorizedException;

    /**
     * @throws NotFoundException
     *             is thrown if the leaderboard is not found by name
     */
    PairingListTemplateDTO calculatePairingListTemplate(final int flightCount, final int groupCount,
            final int competitorCount, final int flightMultiplier, final int tolerance)
            throws UnauthorizedException, NotFoundException, IllegalArgumentException;

    PairingListDTO getPairingListFromTemplate(String leaderboardName, int flightMultiplier,
            Iterable<String> selectedFlightNames, PairingListTemplateDTO templateDTO)
            throws UnauthorizedException, NotFoundException, PairingListCreationException;

    PairingListDTO getPairingListFromRaceLogs(String leaderboardName) throws UnauthorizedException, NotFoundException;

    void fillRaceLogsFromPairingListTemplate(String leaderboardName, int flightMultiplier,
            Iterable<String> selectedFlightNames, PairingListDTO pairingListDTO)
            throws UnauthorizedException, NotFoundException, CompetitorRegistrationOnRaceLogDisabledException;

    List<String> getRaceDisplayNamesFromLeaderboard(String leaderboardName, List<String> raceColumnNames)
            throws UnauthorizedException, NotFoundException;

    Iterable<DetailType> getAvailableDetailTypesForLeaderboard(String leaderboardName,
            RegattaAndRaceIdentifier raceOrNull) throws UnauthorizedException;

    SpotDTO getWindFinderSpot(String spotId) throws UnauthorizedException, Exception;

    /**
     * Returns {@code true} if the given race can be sliced. Only Smarthphone tracked races can be sliced. In addition
     * the race must be part of a {@link RegattaLeaderboard}.
     */
    boolean canSliceRace(RegattaAndRaceIdentifier raceIdentifier) throws UnauthorizedException;

    /**
     * Slices a new race from the race specified by the given {@link RegattaAndRaceIdentifier} using the given time
     * range. A new {@link RaceColumn} with the given name is added to the {@link RegattaLeaderboard}.
     * 
     * @throws ServiceException
     */
    RegattaAndRaceIdentifier sliceRace(RegattaAndRaceIdentifier raceIdentifier, String newRaceColumnName,
            TimePoint sliceFrom, TimePoint sliceTo) throws UnauthorizedException, ServiceException;

    /**
     * Returns specific data needed for the slicing UI.
     */
    SliceRacePreperationDTO prepareForSlicingOfRace(RegattaAndRaceIdentifier raceIdentifier)
            throws UnauthorizedException;

    /**
     * Checks if the given race is currently in state tracking or loading.
     */
    Boolean checkIfRaceIsTracking(RegattaAndRaceIdentifier race) throws UnauthorizedException;

    /**
     * Resizes an {@Link ImageDTO} that is part of an {@link ImageResizingTaskDTO} into a set of resized versions. This
     * set only contains one image in most cases, because most {@Link ImageDTO} only hold one predefined
     * {@link MediaTagConstants}. All {@link MediaTagConstants} stored in the resizingTask of the
     * {@link ImageResizingTaskDTO} create a resize. Since no {@link MediaTagConstants} have the same defined bounds,
     * there will be no merge of these {@Link ImageDTO}. Uses the {@link FileStorageService} to store the resized
     * images. If an error occurs during resize or storing process, it will be tried to restore the previous state.
     * 
     * @author Robin Fleige (D067799)
     * 
     * @param imageResizingTask
     *            is an {@link ImageResizingTaskDTO} with the information on how the image saved in the {@Link ImageDTO}
     *            should be resized. The resizingTask attribute should not be null or empty at this point
     * @return returns a set of {@Link ImageDTO}, that contain the resized variants of the {@Link ImageDTO} in
     *         toResizeImage
     * @throws UnauthorizedException,
     *             Exception can throw different type of exceptions
     */
    Set<ImageDTO> resizeImage(ImageResizingTaskDTO imageResizingTask) throws UnauthorizedException, Exception;

    MailInvitationType getMailType() throws UnauthorizedException;

    /**
     * Generates a base64-encoded qrcode for the branch.io url used to allow registrations for open regattas.
     * 
     * @param url
     *            complete deeplink url for registration on open regattas
     * @return base64 encoded string containg a png-image of the genrated qrcode
     */
    String openRegattaRegistrationQrCode(String url) throws UnauthorizedException;

    void setDefaultTenantForCurrentServer(String tennant) throws UnauthorizedException;

    List<String> getPossibleTennants() throws UnauthorizedException;

    void updateGroupOwnerForEventHierarchy(UUID eventId,
            MigrateGroupOwnerForHierarchyDTO migrateGroupOwnerForHierarchyDTO) throws UnauthorizedException;

    void updateGroupOwnerForLeaderboardGroupHierarchy(UUID leaderboardGroupId,
            MigrateGroupOwnerForHierarchyDTO migrateGroupOwnerForHierarchyDTO) throws UnauthorizedException;

    String getSecretForRegattaByName(String regattaName) throws UnauthorizedException;

    Iterable<AccountWithSecurityDTO> getAllIgtimiAccountsWithSecurity() throws UnauthorizedException;

    /**
     * Allows reading public Boats, or Boats that are registered in races belonging in the given regatta
     */
    BoatDTO getBoat(UUID boatId, String regattaName, String regattaRegistrationLinkSecret);

    /**
     * Allows reading public Marks, or Marks that are registered in the given regatta
     */
    MarkDTO getMark(UUID markId, String regattaName, String regattaRegistrationLinkSecret);

    /**
     * Allows reading public Events, or Events that are related to the given regatta
     */
    QRCodeEvent getEvent(UUID eventId, String regattaName, String regattaRegistrationLinkSecret);

    /**
     * Allows reading public Competitors, or Competitors that are registered in the given regatta
     */
    CompetitorDTO getCompetitor(UUID competitorId, String leaderboardName,
            String regattaRegistrationLinkSecret);
}
