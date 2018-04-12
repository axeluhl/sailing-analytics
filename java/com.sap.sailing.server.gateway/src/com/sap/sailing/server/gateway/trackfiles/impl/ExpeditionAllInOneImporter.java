package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.osgi.framework.BundleContext;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogStartTrackingEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.ExpeditionAllInOneConstants.ImportMode;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.common.racelog.tracking.NotDenotedForRaceLogTrackingException;
import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.ErrorImportDTO;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.TrackImportDTO;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter.WindImportResult;
import com.sap.sailing.server.gateway.windimport.expedition.WindImporter;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sailing.server.operationaltransformation.AddSpecificRegatta;
import com.sap.sailing.server.operationaltransformation.CreateLeaderboardGroup;
import com.sap.sailing.server.operationaltransformation.CreateRegattaLeaderboard;
import com.sap.sailing.server.operationaltransformation.UpdateEvent;
import com.sap.sailing.server.util.WaitForTrackedRaceUtil;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Importer for expedition data that imports all available data for a boat:
 * <ul>
 * <li>{@link GPSFixMoving} and {@link BravoExtendedFix} instances are imported as a distinct track that needs to be
 * mapped to a specific competitor afterwards</li>
 * <li>Wind fixes are being imported as a new {@link WindTrack}</li>
 * </ul>
 * There are several {@link ImportMode ImportModes} which have specific preconditions and activate different importing
 * behavior:
 * <ul>
 * <li>{@link ImportMode#NEW_EVENT} requires a boatclass name to be given. In this case new {@link Event},
 * {@link Regatta} and {@link RegattaLeaderboard} entities are created with one {@link RaceColumn} for a new
 * {@link TrackedRace} to be the target of the imported data. The names of the created entities as well as the created
 * {@link RaceColumn} and {@link WindSource} are generated from the name of the given {@link FileItem}.</li>
 * <li>{@link ImportMode#NEW_COMPETITOR} requires a regatta name to be given. In this case the wind data is being
 * imported as new {@link WindSource} to any existing race of the given regatta. The name of the {@link WindSource} is
 * determined from the name of the given {@link FileItem}. No new entities are created.</li>
 * <li>{@link ImportMode#NEW_RACE} requires a regatta name to be given. In this case the a new {@link RaceColumn} is
 * created in the last existing {@link Series}. A new {@link WindSource} is added to the {@link TrackedRace} associated
 * with the newly added {@link RaceColumn}. This mode does not support importing in cases where fleet racing is used by
 * a regatta.</li>
 * </ul>
 * The imported {@link GPSFixMoving} and {@link BravoExtendedFix} tracks aren't mapped to a {@link Competitor} by the
 * importer. Instead the IDs of the imported tracks are contained in the result and are expected to be mapped by the
 * user afterwards.
 * 
 * This importer is intended to be used by {@link ExpeditionAllInOneImportServlet}.
 *
 */
public class ExpeditionAllInOneImporter {
    private static final String ERROR_MESSAGE_INVALID_REGATTA_NAME = "Please enter a valid regatta name to proceed";

    private static final String ERROR_MESSAGE_INVALID_IMPORTMODE = "Currently not handled ImportMode ";

    private static final String ERROR_MESSAGE_MULTI_SERIES = "There is more than one series in this regatta, cannot add race";

    private static final String ERROR_MESSAGE_INVALID_SERIES = "There is no series in this regatta, cannot add race";

    private static final String ERROR_MESSAGE_SPLITFLEET_NOT_SUPPORTED = "The expedition importer, cannot handle split fleet racing";

    private static final String ERROR_MESSAGE_INVALID_RACE = "To add competitors, a race must be existing";

    private static final String ERROR_MESSAGE_INVALID_LEADERBOARD_EVENT_LINK = "The Event for the leaderboard could not be obtained, please ensure the leaderboard is properly attached to an leaderboardgroup that is attached to an event";

    private static final String ERROR_MESSAGE_INVALID_LEADERBOARD = "The Leaderboard could not be resolved, please ensure a leaderboard named like the regatta exists";

    private static final String ERROR_MESSAGE_INVALID_REGATTA = "The regatta could not be resolved, please ensure the name is correct";

    private static final Logger logger = Logger.getLogger(ExpeditionAllInOneImporter.class.getName());

    private static final String ERROR_MESSAGE_GPS_DATA_IMPORT_FAILED = "Failed to import GPS data!";
    private static final String ERROR_MESSAGE_SENSOR_DATA_IMPORT_FAILED = "Failed to import sensor data!";
    private static final String ERROR_MESSAGE_BOAT_CLASS_DETERMINATION_FAILED = "Failed to determine boat class!";

    private final RacingEventService service;
    private final RaceLogTrackingAdapter adapter;
    private final TypeBasedServiceFinderFactory serviceFinderFactory;
    private final BundleContext context;

    public static class ImporterResult {
        final UUID eventId;
        final String leaderboardName, leaderboardGroupName, regattaName, raceName, raceColumnName, fleetName;
        final List<TrackImportDTO> importGpsFixData, importSensorFixData;
        final String sensorFixImporterType;
        final List<ErrorImportDTO> errorList = new ArrayList<>();

        public ImporterResult(String error) {
            this(null, "", "", new RegattaNameAndRaceName("", ""), "", "", Collections.emptyList(),
                    Collections.emptyList(), "", Collections.emptyList());
            errorList.add(new ErrorImportDTO(error));
        }

        public ImporterResult(Throwable exception, List<ErrorImportDTO> additionalErrors) {
            this(null, "", "", new RegattaNameAndRaceName("", ""), "", "", Collections.emptyList(),
                    Collections.emptyList(), "", Collections.emptyList());
            errorList.add(new ErrorImportDTO(exception.getClass().getName(), exception.getMessage()));
            if (additionalErrors != null) {
                errorList.addAll(additionalErrors);
            }
        }

        private ImporterResult(final UUID eventId, final String leaderboardName, String leaderboardGroupName,
                final RegattaAndRaceIdentifier regattaAndRaceIdentifier, final String raceColumnName,
                final String fleetName, final List<TrackImportDTO> importGpsFixData,
                final List<TrackImportDTO> importSensorFixData, final String sensorFixImporterType,
                List<ErrorImportDTO> errors) {
            this.eventId = eventId;
            this.leaderboardName = leaderboardName;
            this.leaderboardGroupName = leaderboardGroupName;
            this.regattaName = regattaAndRaceIdentifier.getRegattaName();
            this.raceName = regattaAndRaceIdentifier.getRaceName();
            this.raceColumnName = raceColumnName;
            this.fleetName = fleetName;
            this.importGpsFixData = importGpsFixData;
            this.importSensorFixData = importSensorFixData;
            this.sensorFixImporterType = sensorFixImporterType;
            this.errorList.addAll(errors);
        }
    }

    public ExpeditionAllInOneImporter(final RacingEventService service, RaceLogTrackingAdapter adapter,
            final TypeBasedServiceFinderFactory serviceFinderFactory, final BundleContext context) {
        this.service = service;
        this.adapter = adapter;
        this.serviceFinderFactory = serviceFinderFactory;
        this.context = context;
    }

    public ImporterResult importFiles(final String filenameWithSuffix, final FileItem fileItem,
            final String boatClassName, ImportMode importMode, String existingRegattaName) throws AllinOneImportException {
        final List<ErrorImportDTO> errors = new ArrayList<>();
        final String importTimeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now(ZoneOffset.UTC));

        final String filename = ExpeditionImportFilenameUtils.truncateFilenameExtentions(filenameWithSuffix);
        final String filenameWithDateTimeSuffix = filename + "_" + importTimeString;
        final String trackedRaceName = filenameWithDateTimeSuffix;
        final String windSourceId = filenameWithDateTimeSuffix;

        // TODO wild guess...
        final ScoringSchemeType scoringSchemeType = ScoringSchemeType.LOW_POINT;
        final RankingMetrics rankingMetric = RankingMetrics.ONE_DESIGN;
        final int[] discardThresholds = new int[0];

        final ImportResultDTO jsonHolderForGpsFixImport = new ImportResultDTO(logger);
        final List<Pair<String, FileItem>> filesForGpsFixImport = Arrays.asList(new Pair<>(filenameWithSuffix, fileItem));
        try {
            new TrackFilesImporter(service, serviceFinderFactory, context).importFixes(jsonHolderForGpsFixImport,
                    GPSFixImporter.EXPEDITION_TYPE, filesForGpsFixImport);
            this.ensureSuccessfulImport(jsonHolderForGpsFixImport, ERROR_MESSAGE_GPS_DATA_IMPORT_FAILED);
        } catch (IOException e1) {
            errors.addAll(jsonHolderForGpsFixImport.getErrorList());
            throw new AllinOneImportException(e1, errors);
        }
        errors.addAll(jsonHolderForGpsFixImport.getErrorList());

        final ImportResultDTO jsonHolderForSensorFixImport = new ImportResultDTO(logger);
        final String sensorFixImporterType = DoubleVectorFixImporter.EXPEDITION_EXTENDED_TYPE;
        final Iterable<Pair<String, FileItem>> importerNamesAndFilesForSensorFixImport = Arrays
                .asList(new Pair<>(sensorFixImporterType, fileItem));
        try {
            new SensorDataImporter(service, context).importFiles(false, jsonHolderForSensorFixImport,
                    importerNamesAndFilesForSensorFixImport);
            this.ensureSuccessfulImport(jsonHolderForSensorFixImport, ERROR_MESSAGE_SENSOR_DATA_IMPORT_FAILED);
        } catch (IOException e1) {
            errors.addAll(jsonHolderForSensorFixImport.getErrorList());
            throw new AllinOneImportException(e1, errors);
        }
        errors.addAll(jsonHolderForSensorFixImport.getErrorList());

        TimePoint firstFixAt = null;
        TimePoint lastFixAt = null;
        final ArrayList<TrackImportDTO> allData = new ArrayList<>();
        allData.addAll(jsonHolderForGpsFixImport.getImportResult());
        allData.addAll(jsonHolderForSensorFixImport.getImportResult());

        for (TrackImportDTO result : allData) {
            final TimePoint deviceTrackStart = result.getRange().from();
            final TimePoint deviceTrackEnd = result.getRange().to();
            if (firstFixAt == null || deviceTrackStart.before(firstFixAt)) {
                firstFixAt = deviceTrackStart;
            }
            if (lastFixAt == null || deviceTrackEnd.after(lastFixAt)) {
                lastFixAt = deviceTrackEnd;
            }
        }

        final TimePoint eventStartDate = firstFixAt;
        final TimePoint eventEndDate = lastFixAt;
        
        final UUID eventId;
        final String leaderboardGroupName;
        final String regattaNameAndleaderboardName;
        final Regatta regatta;
        final RegattaLeaderboard regattaLeaderboard;
        // TODO Should we return all TrackedRaces and show several RaceBoard links to the user?
        final DynamicTrackedRace trackedRace;
        final List<DynamicTrackedRace> trackedRaces = new ArrayList<>();
        final String fleetName;
        final String raceColumnName;
        if (importMode == ImportMode.NEW_EVENT) {
            final String eventName = filenameWithDateTimeSuffix;
            final String description = MessageFormat.format("Event imported from expedition file ''{0}'' on {1}",
                    filenameWithSuffix, importTimeString);
            // TODO guess venue based on the reverse geocoder?
            final String venueName = filename;
            leaderboardGroupName = filenameWithDateTimeSuffix;
            regattaNameAndleaderboardName = filenameWithDateTimeSuffix;
            raceColumnName = filename;
            final RegattaIdentifier regattaIdentifier = new RegattaName(filenameWithDateTimeSuffix);
            final String courseAreaName = "Default";
            final UUID courseAreaId = UUID.randomUUID();
            // This is just the default used in the UI
            final Double buoyZoneRadiusInHullLengths = 3.0;

            fleetName = LeaderboardNameConstants.DEFAULT_FLEET_NAME;
            final String seriesName = Series.DEFAULT_NAME;
            
            final Event event = service.addEvent(eventName, description, eventStartDate, eventEndDate, venueName, true,
                    UUID.randomUUID());
            eventId = event.getId();
            service.addCourseAreas(event.getId(), new String[] { courseAreaName }, new UUID[] { courseAreaId });
            
            final ScoringScheme scoringScheme = service.getBaseDomainFactory().createScoringScheme(scoringSchemeType);
            
            final LinkedHashMap<String, SeriesCreationParametersDTO> seriesCreationParameters = new LinkedHashMap<>();
            final List<FleetDTO> fleets = new ArrayList<>();
            fleets.add(new FleetDTO(fleetName, 0, null));
            seriesCreationParameters.put(seriesName,
                    new SeriesCreationParametersDTO(fleets, /*isMedal*/ false,
                            /* isFleetsCanRunInParallel */ false, /*isStartsWithZeroScore*/ false, /*firstColumnIsNonDiscardableCarryForward*/false, /*discardingThresholds*/ null,
                            /*hasSplitFleetContiguousScoring*/ false, /*maximumNumberOfDiscards*/ null));
            final RegattaCreationParametersDTO regattaCreationParameters = new RegattaCreationParametersDTO(
                    seriesCreationParameters);
            
            regatta = service.apply(new AddSpecificRegatta(regattaNameAndleaderboardName, boatClassName,
                /* can boats of competitors change */ false,
                /* start date */ null, /* end date */ null, UUID.randomUUID(),
                    regattaCreationParameters, true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths, true,
                    false, rankingMetric));
            this.ensureBoatClassDetermination(regatta);
            service.apply(new AddColumnToSeries(regattaIdentifier, seriesName, raceColumnName));
            regattaLeaderboard = service.apply(new CreateRegattaLeaderboard(regattaIdentifier, null,
                    discardThresholds));
            
            final LeaderboardGroup leaderboardGroup = service.apply(new CreateLeaderboardGroup(leaderboardGroupName,
                    description, null, false, Collections.singletonList(regattaNameAndleaderboardName), null, null));
            service.apply(new UpdateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                    event.getEndDate(), event.getVenue().getName(), event.isPublic(),
                    Collections.singleton(leaderboardGroup.getId()), event.getOfficialWebsiteURL(), event.getBaseURL(),
                    event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos(), event.getWindFinderReviewedSpotsCollectionIds()));
            
            final RaceColumn raceColumn = regattaLeaderboard.getRaceColumns().iterator().next();
            final Fleet fleet = raceColumn.getFleets().iterator().next();
            
            trackedRace = createTrackedAndSetupRaceTimes(errors, trackedRaceName, firstFixAt, lastFixAt, regatta, regattaLeaderboard,
                    raceColumn, fleet);
            trackedRaces.add(trackedRace);
        } else {
            regattaNameAndleaderboardName = existingRegattaName;
            if (existingRegattaName != null && !existingRegattaName.isEmpty()) {
                regatta = service.getRegattaByName(existingRegattaName);
                if (regatta == null) {
                    return new ImporterResult(ERROR_MESSAGE_INVALID_REGATTA);
                }
                final Leaderboard leaderboard = service.getLeaderboardByName(existingRegattaName);
                if (leaderboard == null || !(leaderboard instanceof RegattaLeaderboard)) {
                    return new ImporterResult(ERROR_MESSAGE_INVALID_LEADERBOARD);
                }
                regattaLeaderboard = (RegattaLeaderboard) leaderboard;
                
                Event foundEvent = null;
                LeaderboardGroup foundLeaderboardGroup = null;
                search: for (Event event : service.getAllEvents()) {
                    for (LeaderboardGroup leaderboardGroup : event.getLeaderboardGroups()) {
                        for (Leaderboard lb : leaderboardGroup.getLeaderboards()) {
                            if (lb.equals(leaderboard)) {
                                foundEvent = event;
                                foundLeaderboardGroup = leaderboardGroup;
                                break search;
                            }
                        }
                    }
                }
                if (foundEvent == null) {
                    return new ImporterResult(ERROR_MESSAGE_INVALID_LEADERBOARD_EVENT_LINK);
                }
                // TODO should we extend the time range of the event to ensure that it includes the newly imported tracks' time ranges?
                eventId = foundEvent.getId();
                leaderboardGroupName = foundLeaderboardGroup.getName();
                
                if (importMode == ImportMode.NEW_COMPETITOR) {
                    final Iterable<RaceColumn> raceColumns = regattaLeaderboard.getRaceColumns();
                    if (Util.isEmpty(raceColumns)) {
                        return new ImporterResult(ERROR_MESSAGE_INVALID_RACE);
                    }
                    try {
                        Fleet firstFleet = null;
                        DynamicTrackedRace firstTrackedRace = null;
                        RaceColumn firstRaceColumn = null;
                        for (RaceColumn raceColumn : raceColumns) {
                            final Iterable<? extends Fleet> fleets = raceColumn.getFleets();
                            if (Util.size(fleets) != 1) {
                                return new ImporterResult(ERROR_MESSAGE_SPLITFLEET_NOT_SUPPORTED);
                            }
                            final Fleet fleet = fleets.iterator().next();
                            DynamicTrackedRace trackedRaceForColumn = (DynamicTrackedRace) raceColumn.getTrackedRace(fleet);
                            if (trackedRaceForColumn == null) {
                                trackedRaceForColumn = trackRace(regattaLeaderboard, raceColumn, fleet);
                            }
                            trackedRaces.add(trackedRaceForColumn);
                            if (firstTrackedRace == null) {
                                firstTrackedRace = trackedRaceForColumn;
                                firstFleet = fleet;
                                firstRaceColumn = raceColumn;
                            }
                        }
                        // TODO we remember the first trackedRace for now. Should we remember all?
                        trackedRace = firstTrackedRace;
                        fleetName = firstFleet.getName();
                        raceColumnName = firstRaceColumn.getName();
                    } catch (Exception e) {
                        throw new AllinOneImportException(e, errors);
                    }
                } else if (importMode == ImportMode.NEW_RACE){
                    // ImportMode.NEW_RACE
                    final Iterable<? extends Series> seriesInRegatta = regatta.getSeries();
                    if (Util.isEmpty(seriesInRegatta)) {
                        return new ImporterResult(ERROR_MESSAGE_INVALID_SERIES);
                    }
                    final Series series = Util.get(seriesInRegatta, Util.size(seriesInRegatta) - 1);
                    final Iterable<? extends Fleet> fleets = series.getFleets();
                    if (Util.size(fleets) != 1) {
                        return new ImporterResult(ERROR_MESSAGE_MULTI_SERIES);
                    }
                    final Fleet fleet = fleets.iterator().next();
                    fleetName = fleet.getName();
                    // When uploading files with identical name, the second RaceColumn will be named with the upload time in its name
                    raceColumnName = regatta.getRaceColumnByName(filename) == null ? filename : filenameWithDateTimeSuffix;
                    final RaceColumn raceColumn = service.apply(new AddColumnToSeries(regatta.getRegattaIdentifier(), series.getName(), raceColumnName));

                    trackedRace = createTrackedAndSetupRaceTimes(errors, trackedRaceName, firstFixAt, lastFixAt, regatta, regattaLeaderboard,
                            raceColumn, fleet);
                    trackedRaces.add(trackedRace);
                } else {
                    return new ImporterResult(ERROR_MESSAGE_INVALID_IMPORTMODE + importMode);
                }
            } else {
                return new ImporterResult(ERROR_MESSAGE_INVALID_REGATTA_NAME);
            }
        }

        try {
            final WindImportResult windImportResult = new AbstractWindImporter.WindImportResult();
            final WindSourceWithAdditionalID windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION,
                    windSourceId);
            final Map<InputStream, String> streamsWithFilenames = new HashMap<>();
            streamsWithFilenames.put(fileItem.getInputStream(), filenameWithSuffix);
            new WindImporter().importWindToWindSourceAndTrackedRaces(service, windImportResult, windSource,
                    trackedRaces, streamsWithFilenames);
            
            return new ImporterResult(eventId, regattaNameAndleaderboardName, leaderboardGroupName,
                    trackedRace.getRaceIdentifier(), raceColumnName, fleetName,
                    jsonHolderForGpsFixImport.getImportResult(), jsonHolderForSensorFixImport.getImportResult(),
                    sensorFixImporterType, errors);
        } catch (Exception e) {
            throw new AllinOneImportException(e, errors);
        }
    }

    private DynamicTrackedRace createTrackedAndSetupRaceTimes(final List<ErrorImportDTO> errors,
            final String trackedRaceName, TimePoint firstFixAt, TimePoint lastFixAt, final Regatta regatta,
            final RegattaLeaderboard regattaLeaderboard, final RaceColumn raceColumn, final Fleet fleet)
            throws AllinOneImportException {
        final RaceLog raceLog = raceColumn.getRaceLog(fleet);

        final AbstractLogEventAuthor author = service.getServerAuthor();

        final TimePoint startOfTracking = firstFixAt;
        final TimePoint endOfTracking = lastFixAt;
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
        // TODO explicitly set startOfRace?

        try {
            TimePoint startTrackingTimePoint = MillisecondsTimePoint.now();
            // this ensures that the events consistently have different timepoints to ensure a consistent result of the
            // state analysis
            // that's why we can't just call adapter.denoteRaceForRaceLogTracking
            final TimePoint denotationTimePoint = startTrackingTimePoint.minus(1);
            raceLog.add(new RaceLogDenoteForTrackingEventImpl(denotationTimePoint, service.getServerAuthor(),
                    raceLog.getCurrentPassId(), trackedRaceName, regatta.getBoatClass(), UUID.randomUUID()));

            raceLog.add(new RaceLogStartTrackingEventImpl(startTrackingTimePoint, author, raceLog.getCurrentPassId()));

            return trackRace(regattaLeaderboard, raceColumn, fleet);
        } catch (Exception e) {
            throw new AllinOneImportException(e, errors);
        }
    }

    private DynamicTrackedRace trackRace(final RegattaLeaderboard regattaLeaderboard, final RaceColumn raceColumn,
            final Fleet fleet) throws NotDenotedForRaceLogTrackingException, Exception {
        DynamicTrackedRace trackedRace;
        final RaceHandle raceHandle = adapter.startTracking(service, regattaLeaderboard, raceColumn, fleet,
                /* trackWind */ true, /* TODO correctWindDirectionByMagneticDeclination */ true);

        // wait for the RaceDefinition to be created
        raceHandle.getRace();

        trackedRace = WaitForTrackedRaceUtil.waitForTrackedRace(raceColumn, fleet, 10);
        if (trackedRace == null) {
            throw new IllegalStateException("Could not obtain imported race");
        }
        return trackedRace;
    }

    private void ensureSuccessfulImport(ImportResultDTO result, String errorMessage) throws AllinOneImportException {
        if (!result.getErrorList().isEmpty()) {
            throw new AllinOneImportException(errorMessage, result.getErrorList());
        }
    }

    private void ensureBoatClassDetermination(Regatta regatta) throws AllinOneImportException {
        if (regatta.getBoatClass() == null) {
            throw new AllinOneImportException(ERROR_MESSAGE_BOAT_CLASS_DETERMINATION_FAILED);
        }
    }
}
