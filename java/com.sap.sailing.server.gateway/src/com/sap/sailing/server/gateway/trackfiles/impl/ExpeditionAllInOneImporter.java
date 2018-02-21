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

import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogDenoteForTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.tracking.impl.RaceLogStartTrackingEventImpl;
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
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceHandle;
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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ExpeditionAllInOneImporter {
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

        public ImporterResult(Throwable exception, List<ErrorImportDTO> additionalErrors) {
            this(null, "", "", new RegattaNameAndRaceName("", ""), "", "", Collections.emptyList(),
                    Collections.emptyList(), "", additionalErrors);
            this.errorList.add(new ErrorImportDTO(exception.getClass().getName(), exception.getMessage()));
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
            final String boatClassName) throws AllinOneImportException {
        final List<ErrorImportDTO> errors = new ArrayList<>();
        final String importTimeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now(ZoneOffset.UTC));

        final String filename = ExpeditionImportFilenameUtils.truncateFilenameExtentions(filenameWithSuffix);
        final String filenameWithDateTimeSuffix = filename + "_" + importTimeString;
        final String eventName = filenameWithDateTimeSuffix;
        final String description = MessageFormat.format("Event imported from expedition file ''{0}'' on {1}",
                filenameWithSuffix, importTimeString);
        final String leaderboardGroupName = filenameWithDateTimeSuffix;
        final String regattaNameAndleaderboardName = filenameWithDateTimeSuffix;
        final RegattaIdentifier regattaIdentifier = new RegattaName(filenameWithDateTimeSuffix);
        final String raceColumnName = filename;
        final String trackedRaceName = filenameWithDateTimeSuffix;
        final String courseAreaName = "Default";
        final UUID courseAreaId = UUID.randomUUID();
        // This is just the default used in the UI
        final Double buoyZoneRadiusInHullLengths = 3.0;
        // TODO is this a proper id for the WindSource used here?
        final String windSourceId = filenameWithDateTimeSuffix;

        // TODO guess venue based on the reverse geocoder?
        final String venueName = filename;

        final String fleetName = LeaderboardNameConstants.DEFAULT_FLEET_NAME;
        final String seriesName = Series.DEFAULT_NAME;

        // TODO wild guess...
        final ScoringSchemeType scoringSchemeType = ScoringSchemeType.LOW_POINT;
        final RankingMetrics rankingMetric = RankingMetrics.ONE_DESIGN;
        final int[] discardThresholds = new int[0];

        // TODO These are the defaults also used by the UI
        final String raceLogEventAuthorName = "Shore";
        final int raceLogEventPriority = 4;
        final boolean correctWindDirectionByMagneticDeclination = true;

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

        final Event event = service.addEvent(eventName, description, eventStartDate, eventEndDate, venueName, true,
                UUID.randomUUID());
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
        
        final Regatta regatta = service.apply(new AddSpecificRegatta(regattaNameAndleaderboardName, boatClassName,
                /* can boats of competitors change */ false,
                /* start date */ null, /* end date */ null, UUID.randomUUID(),
                regattaCreationParameters, true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths, true,
                false, rankingMetric));
        this.ensureBoatClassDetermination(regatta);
        service.apply(new AddColumnToSeries(regattaIdentifier, seriesName, raceColumnName));
        final RegattaLeaderboard regattaLeaderboard = service.apply(new CreateRegattaLeaderboard(regattaIdentifier, null,
                discardThresholds));

        final LeaderboardGroup leaderboardGroup = service.apply(new CreateLeaderboardGroup(leaderboardGroupName,
                description, null, false, Collections.singletonList(regattaNameAndleaderboardName), null, null));
        service.apply(new UpdateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(),
                Collections.singleton(leaderboardGroup.getId()), event.getOfficialWebsiteURL(), event.getBaseURL(),
                event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos()));

        final RaceColumn raceColumn = regattaLeaderboard.getRaceColumns().iterator().next();
        final Fleet fleet = raceColumn.getFleets().iterator().next();

        final RaceLog raceLog = raceColumn.getRaceLog(fleet);
        final LogEventAuthorImpl author = new LogEventAuthorImpl(raceLogEventAuthorName, raceLogEventPriority);

        final TimePoint startOfTracking = firstFixAt;
        final TimePoint endOfTracking = lastFixAt;
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
        // TODO explicitly set startOfRace?

        try {
            TimePoint startTrackingTimePoint = MillisecondsTimePoint.now();
            // this ensures that the events consistently have different timepoints to ensure a consistent result of the state analysis
            // that's why we can't jus call adapter.denoteRaceForRaceLogTracking
            final TimePoint denotationTimePoint = startTrackingTimePoint.minus(1);
            raceLog.add(new RaceLogDenoteForTrackingEventImpl(denotationTimePoint,
                    service.getServerAuthor(), raceLog.getCurrentPassId(), trackedRaceName, regatta.getBoatClass(), UUID.randomUUID()));
            
            raceLog.add(new RaceLogStartTrackingEventImpl(startTrackingTimePoint, author, raceLog.getCurrentPassId()));
            
            final RaceHandle raceHandle = adapter.startTracking(service, regattaLeaderboard, raceColumn, fleet, true,
                    correctWindDirectionByMagneticDeclination);

            // TODO do we need to wait or is the TrackedRace guaranteed to be reachable after calling startTracking?
            raceHandle.getRace();

            final DynamicTrackedRace trackedRace = (DynamicTrackedRace) raceColumn.getTrackedRace(fleet);

            final WindImportResult windImportResult = new AbstractWindImporter.WindImportResult();
            final WindSourceWithAdditionalID windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION,
                    windSourceId);
            final Map<InputStream, String> streamsWithFilenames = new HashMap<>();
            streamsWithFilenames.put(fileItem.getInputStream(), filenameWithSuffix);
            new WindImporter().importWindToWindSourceAndTrackedRaces(service, windImportResult, windSource,
                    Arrays.asList(trackedRace), streamsWithFilenames);

            return new ImporterResult(event.getId(), regattaNameAndleaderboardName, leaderboardGroupName,
                    trackedRace.getRaceIdentifier(), raceColumnName, fleetName,
                    jsonHolderForGpsFixImport.getImportResult(), jsonHolderForSensorFixImport.getImportResult(),
                    sensorFixImporterType, errors);
        } catch (Exception e) {
            throw new AllinOneImportException(e, errors);
        }
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
