package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.shared.BulkScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.ControlPointDTO;
import com.sap.sailing.gwt.ui.shared.CourseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceCourseMarksDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnInSeriesDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.ManeuverDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.QuickRankDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceMapDataDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.ReplicationStateDTO;
import com.sap.sailing.gwt.ui.shared.RegattaScoreCorrectionDTO;
import com.sap.sailing.gwt.ui.shared.ScoreCorrectionProviderDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingReplayRaceDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.SwissTimingRaceRecordDTO;
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
     */
    void listTracTracRacesInEvent(String eventJsonURL, AsyncCallback<Pair<String, List<TracTracRaceRecordDTO>>> callback);

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
            TracTracRaceRecordDTO rr, String liveURI, String storedURI, boolean trackWind, boolean correctWindByDeclination,
            boolean simulateWithStartTimeNow, AsyncCallback<Void> callback);

    void getPreviousTracTracConfigurations(AsyncCallback<List<TracTracConfigurationDTO>> callback);

    void storeTracTracConfiguration(String name, String jsonURL, String liveDataURI, String storedDataURI,
            AsyncCallback<Void> callback);

    void stopTrackingEvent(RegattaIdentifier eventIdentifier, AsyncCallback<Void> callback);

    void stopTrackingRace(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<Void> asyncCallback);
    
    /**
     * Untracks the race and removes it from the regatta. It will also be removed in all leaderboards
     * @param regattaAndRaceidentifier The identifier for the regatta name, and the race name to remove
     */
    void removeAndUntrackRace(RegattaAndRaceIdentifier regattaAndRaceidentifier, AsyncCallback<Void> callback);

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

    /**
     * @param from
     *            for the list of competitors provided as keys of this map, requests the GPS fixes starting with the
     *            date provided as value
     * @param to
     *            for the list of competitors provided as keys (expected to be equal to the set of competitors used as
     *            keys in the <code>from</code> parameter, requests the GPS fixes up to but excluding the date provided
     *            as value
     * @param extrapolate
     *            if <code>true</code> and no position is known for <code>date</code>, the last entry returned in the
     *            list of GPS fixes will be obtained by extrapolating from the competitors last known position before
     *            <code>date</code> and the estimated speed.
     * @return a map where for each competitor participating in the race the list of GPS fixes in increasing
     *         chronological order is provided. The last one is the last position at or before <code>date</code>.
     */
    void getBoatPositions(RegattaAndRaceIdentifier raceIdentifier,
            Map<CompetitorDTO, Date> from, Map<CompetitorDTO, Date> to,
            boolean extrapolate, AsyncCallback<Map<CompetitorDTO, List<GPSFixDTO>>> callback);

    void getRaceTimesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<RaceTimesInfoDTO> callback);
    
    void getRaceTimesInfos(Collection<RegattaAndRaceIdentifier> raceIdentifiers, AsyncCallback<List<RaceTimesInfoDTO>> callback);

    void getCoursePositions(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<CourseDTO> asyncCallback);

    void getQuickRanks(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<List<QuickRankDTO>> callback);

    /**
     * Returns a {@link LeaderboardDTO} will information about all races, their points and competitor display names
     * filled in. The column details are filled for the races whose named are provided in
     * <code>namesOfRacesForWhichToLoadLegDetails</code>.
     * 
     * @param date
     *            the time point for the leaderboard data to retrieve, or <code>null</code> for "live mode" which means
     *            that the server will produce whatever it has ready for the currently active live delay set on the
     *            server; in live mode, data served may be taken from caches that lag up to a few seconds.
     * @param namesOfRaceColumnsForWhichToLoadLegDetails
     *            if <code>null</code>, no {@link LeaderboardEntryDTO#legDetails leg details} will be present in the
     *            result ({@link LeaderboardEntryDTO#legDetails} will be <code>null</code> for all
     *            {@link LeaderboardEntryDTO} objects contained). Otherwise, the {@link LeaderboardEntryDTO#legDetails}
     *            list will contain one entry per leg of the race {@link Course} for those race columns whose
     *            {@link RaceColumn#getType() name} is contained in <code>namesOfRacesForWhichToLoadLegDetails</code>.
     *            For all other columns, {@link LeaderboardEntryDTO#legDetails} is <code>null</code>.
     */
    void getLeaderboardByName(String leaderboardName, Date date,
            Collection<String> namesOfRaceColumnsForWhichToLoadLegDetails,
            AsyncCallback<LeaderboardDTO> callback);

    void getLeaderboardNames(AsyncCallback<List<String>> callback);

    void getLeaderboards(AsyncCallback<List<StrippedLeaderboardDTO>> callback);
    
    void getLeaderboardsByEvent(RegattaDTO regatta, AsyncCallback<List<StrippedLeaderboardDTO>> callback);
    
    void getLeaderboardsByRace(RaceDTO race, AsyncCallback<List<StrippedLeaderboardDTO>> callback);
    
    void updateLeaderboard(String leaderboardName, String newLeaderboardName, int[] newDiscardingThreasholds,
            AsyncCallback<Void> callback);

    void createFlexibleLeaderboard(String leaderboardName, int[] discardThresholds, ScoringSchemeType scoringSchemeType,
            AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void createRegattaLeaderboard(RegattaIdentifier regattaIdentifier, int[] discardThresholds,
            AsyncCallback<StrippedLeaderboardDTO> asyncCallback);

    void removeLeaderboard(String leaderboardName, AsyncCallback<Void> asyncCallback);
    
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

    void listSwissTimingRaces(String hostname, int port, boolean canSendRequests,
            AsyncCallback<List<SwissTimingRaceRecordDTO>> asyncCallback);

    void storeSwissTimingConfiguration(String configName, String hostname, int port, boolean canSendRequests, AsyncCallback<Void> asyncCallback);

    /**
     * @param regattaToAddTo
     *            if <code>null</code>, an existing regatta by the name of the TracTrac event with the boat class name
     *            appended in parentheses will be looked up; if not found, a default regatta with that name will be
     *            created, with a single default series and a single default fleet. If a valid {@link RegattaIdentifier}
     *            is specified, a regatta lookup is performed with that identifier; if the regatta is found, it is used
     *            to add the races to. Otherwise, a default regatta as described above will be created and used.
     */
    void trackWithSwissTiming(RegattaIdentifier regattaToAddTo, SwissTimingRaceRecordDTO rr, String hostname, int port,
            boolean canSendRequests, boolean trackWind, boolean correctWindByDeclination,
            AsyncCallback<Void> asyncCallback);

    void sendSwissTimingDummyRace(String racMessage, String stlMesssage, String ccgMessage, AsyncCallback<Void> callback);
    
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
     * Removes the leaderboard group with the name <code>groupName</code> from the service and the persistant store.
     */
    void removeLeaderboardGroup(String groupName, AsyncCallback<Void> callback);
    
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

    void getRaceMapData(RegattaAndRaceIdentifier raceIdentifier, Date date, Map<CompetitorDTO, Date> from,
            Map<CompetitorDTO, Date> to, boolean extrapolate, AsyncCallback<RaceMapDataDTO> callback);

    void getReplicaInfo(AsyncCallback<ReplicationStateDTO> callback);

    void startReplicatingFromMaster(String masterName, String exchangeName, int servletPort, int messagingPort,
            AsyncCallback<Void> callback);

    void getEvents(AsyncCallback<List<EventDTO>> callback);

    /**
     * Creates a {@link EventDTO} for the {@link Event} with the name <code>eventName</code>, which contains the
     * name, the description and a list with {@link RegattaDTO RegattaDTOs} contained in the event.<br />
     * If no event with the name <code>eventName</code> is known, an {@link IllegalArgumentException} is thrown.
     */
    void getEventByName(String eventName, AsyncCallback<EventDTO> callback);
    
    /**
    * Renames the event with the name <code>oldName</code> to the <code>newName</code>.<br />
    * If there's no event with the name <code>oldName</code> or there's already a event with the name
    * <code>newName</code> a {@link IllegalArgumentException} is thrown.
    */
    void renameEvent(String oldName, String newName, AsyncCallback<Void> callback);
    
    /**
     * Removes the event with the name <code>eventName</code> from the service and the persistence store.
     */
    void removeEvent(String eventName, AsyncCallback<Void> callback);
    
    void createEvent(String eventName, String description, String publicationUrl, boolean isPublic, AsyncCallback<EventDTO> callback);
    
    void updateEvent(String eventName, VenueDTO venue, String publicationUrl, boolean isPublic, List<String> regattaNames,
            AsyncCallback<Void> callback);

    void removeRegatta(RegattaIdentifier regattaIdentifier, AsyncCallback<Void> callback);

    void addRaceColumnToSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<RaceColumnInSeriesDTO> callback);

    void removeRaceColumnFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void moveRaceColumnInSeriesUp(RegattaIdentifier regattaIdentifier, String seriesName, String columnName, 
            AsyncCallback<Void> callback);

    void moveRaceColumnInSeriesDown(RegattaIdentifier regattaIdentifier, String seriesName, String columnName,
            AsyncCallback<Void> callback);

    void createRegatta(String regattaName, String boatClassName,
            LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> seriesNamesWithFleetNamesAndFleetOrderingAndMedal,
            boolean persistent, ScoringSchemeType scoringSchemeType, AsyncCallback<RegattaDTO> callback);

    void addRaceColumnsToSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames,
            AsyncCallback<List<RaceColumnInSeriesDTO>> callback);

    void removeRaceColumnsFromSeries(RegattaIdentifier regattaIdentifier, String seriesName, List<String> columnNames,
            AsyncCallback<Void> callback);

    void getScoreCorrectionProviderDTOs(AsyncCallback<Iterable<ScoreCorrectionProviderDTO>> callback);

    void getScoreCorrections(String scoreCorrectionProviderName, String eventName, String boatClassName,
            Date timePointWhenResultPublished, AsyncCallback<RegattaScoreCorrectionDTO> asyncCallback);

    void getWindSourcesInfo(RegattaAndRaceIdentifier raceIdentifier, AsyncCallback<WindInfoForRaceDTO> callback);

    void getRaceCourse(RegattaAndRaceIdentifier raceIdentifier, Date date, AsyncCallback<List<ControlPointDTO>> callback);

    void updateRaceCourse(RegattaAndRaceIdentifier raceIdentifier, List<ControlPointDTO> controlPoints, AsyncCallback<Void> callback);

    void getFregResultUrls(AsyncCallback<List<String>> asyncCallback);

    void removeFregURLs(Set<String> toRemove, AsyncCallback<Void> asyncCallback);

    void addFragUrl(String result, AsyncCallback<Void> asyncCallback);

    void getRaceCourseMarks(RegattaAndRaceIdentifier raceIdentifier, Date date,	AsyncCallback<RaceCourseMarksDTO> callback);

    void addColumnsToLeaderboard(String leaderboardName, List<Pair<String, Boolean>> columnsToAdd,
            AsyncCallback<Void> callback);

    void removeLeaderboardColumns(String leaderboardName, List<String> columnsToRemove, AsyncCallback<Void> callback);

    void getLeaderboard(String leaderboardName, AsyncCallback<StrippedLeaderboardDTO> callback);

    void suppressCompetitorInLeaderboard(String leaderboardName, String competitorIdAsString, boolean suppressed, AsyncCallback<Void> asyncCallback);

    void updateLeaderboardColumnFactor(String leaderboardName, String columnName, Double newFactor,
            AsyncCallback<Void> callback);

    void listSwissTiminigReplayRaces(String swissTimingUrl, AsyncCallback<List<SwissTimingReplayRaceDTO>> asyncCallback);

    void replaySwissTimingRace(RegattaIdentifier regattaIdentifier, SwissTimingReplayRaceDTO replayRace,
            boolean trackWind, boolean correctWindByDeclination, boolean simulateWithStartTimeNow,
            AsyncCallback<Void> asyncCallback);

    void getRankedCompetitorsFromBestToWorstAfterEachRaceColumn(String leaderboardName, Date date,
            AsyncCallback<List<Pair<String, List<CompetitorDTO>>>> callback);

    void getCompetitorsRaceData(RegattaAndRaceIdentifier race, List<CompetitorDTO> competitors, Date from, Date to,
            long stepSize, DetailType detailType, AsyncCallback<CompetitorsRaceDataDTO> callback);

    /**
     * Finds out the names of all {@link MetaLeaderboard}s managed by this server that
     * {@link MetaLeaderboard#getLeaderboards() contain} the leaderboard identified by <code>leaderboardName</code>. The
     * names of those meta-leaderboards are returned. The list returned is never <code>null</code> but may be empty if no such
     * leaderboard is found.
     */
    void getOverallLeaderboardNamesContaining(String leaderboardName, AsyncCallback<List<String>> asyncCallback);
}
