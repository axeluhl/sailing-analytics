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
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceWithAdditionalID;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.ranking.RankingMetricsFactory;
import com.sap.sailing.domain.trackimport.DoubleVectorFixImporter;
import com.sap.sailing.domain.trackimport.GPSFixImporter;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.gateway.trackfiles.impl.ImportResultDTO.TrackImportDTO;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter;
import com.sap.sailing.server.gateway.windimport.AbstractWindImporter.WindImportResult;
import com.sap.sailing.server.gateway.windimport.expedition.WindImporter;
import com.sap.sailing.server.operationaltransformation.AddColumnToSeries;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util.Pair;

public class ExpeditionAllInOneImporter {
    private static final Logger logger = Logger.getLogger(ExpeditionAllInOneImporter.class.getName());

    private final RacingEventService service;
    private final RaceLogTrackingAdapter adapter;
    private final TypeBasedServiceFinderFactory serviceFinderFactory;
    private final BundleContext context;

    public static class ImporterResult {
        final UUID eventId;
        final String leaderboardName;
        final String regattaName;
        final String raceName;

        public ImporterResult(final UUID eventId, final String leaderboardName, final RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
            this.eventId = eventId;
            this.leaderboardName = leaderboardName;
            this.regattaName = regattaAndRaceIdentifier.getRegattaName();
            this.raceName = regattaAndRaceIdentifier.getRaceName();
        }
    }
    
    public ExpeditionAllInOneImporter(final RacingEventService service, RaceLogTrackingAdapter adapter, final TypeBasedServiceFinderFactory serviceFinderFactory, final BundleContext context) {
        this.service = service;
        this.adapter = adapter;
        this.serviceFinderFactory = serviceFinderFactory;
        this.context = context;
    }

    public ImporterResult importFiles(final String filename, final FileItem fileItem) {
        final String importTimeString = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now(ZoneOffset.UTC));
        final String filenameWithDateTimeSuffix = filename + "_" + importTimeString;
        // TODO prevent duplicate event/leaderboard names
        final String eventName = filenameWithDateTimeSuffix;
        final String description = MessageFormat.format("Event imported from expedition file '{0}' on {1}", filename, importTimeString);
        final String leaderboardGroupName = filenameWithDateTimeSuffix;
        final String regattaNameAndleaderboardName = filenameWithDateTimeSuffix;
        final RegattaIdentifier regattaIdentifier = new RegattaName(filenameWithDateTimeSuffix);
        final String raceColumnName = filename;
        final String courseAreaName = "Default";
        final UUID courseAreaId = UUID.randomUUID();
        // TODO provide boat class suggest in the UI?
        final Double buoyZoneRadiusInHullLengths = 3.0;
        final String boatClassName = null;
        // TODO proper id
        final String windSourceId = filenameWithDateTimeSuffix;

        // TODO guess venue based on the reverse geocoder?
        final String venueName = filename;
        
        final String fleetName = LeaderboardNameConstants.DEFAULT_FLEET_NAME;
        final String seriesName = Series.DEFAULT_NAME;

        // TODO wild guess...
        final ScoringSchemeType scoringSchemeType = ScoringSchemeType.HIGH_POINT;
        final RankingMetrics rankingMetric = RankingMetrics.ONE_DESIGN;
        final int[] discardThresholds = new int[0];

        // TODO These are the defaults also used by the UI
        final String raceLogEventAuthorName = "Shore";
        final int raceLogEventPriority = 4;
        
        final ImportResultDTO jsonHolderForGpsFixImport = new ImportResultDTO(logger);
        final List<Pair<String, FileItem>> filesForGpsFixImport = Arrays.asList(new Pair<>(filename, fileItem));
        try {
            new TrackFilesImporter(service, serviceFinderFactory, context).importFixes(jsonHolderForGpsFixImport, GPSFixImporter.EXPEDITION_TYPE, filesForGpsFixImport);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        final ImportResultDTO jsonHolderForSensorFixImport = new ImportResultDTO(logger);
        final Iterable<Pair<String, FileItem>> importerNamesAndFilesForSensorFixImport = Arrays.asList(new Pair<>(DoubleVectorFixImporter.EXPEDITION_EXTENDED_TYPE, fileItem));
        try {
            new SensorDataImporter(service, context).importFiles(false, jsonHolderForSensorFixImport, importerNamesAndFilesForSensorFixImport);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
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

        final Event event = service.addEvent(eventName, description, eventStartDate, eventEndDate,
                venueName, true, UUID.randomUUID());
        service.addCourseAreas(event.getId(), new String[] { courseAreaName }, new UUID[] { courseAreaId });

        final Series series = new SeriesImpl(seriesName, /* isMedal */ false, /* isFleetsCanRunInParallel */ false,
                Collections.singleton(new FleetImpl(fleetName)),
                Collections.emptySet(), /* trackedRegattaRegistry */ service);
        final ScoringScheme scoringScheme = service.getBaseDomainFactory().createScoringScheme(scoringSchemeType);
        final RankingMetricConstructor rankingMetricConstructor = RankingMetricsFactory
                .getRankingMetricConstructor(rankingMetric);
        service.createRegatta(regattaNameAndleaderboardName, boatClassName, null, null, UUID.randomUUID(),
                Collections.singleton(series), true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths, true,
                false, rankingMetricConstructor);
        service.apply(new AddColumnToSeries(regattaIdentifier, seriesName, raceColumnName));
        final RegattaLeaderboard regattaLeaderboard = service.addRegattaLeaderboard(regattaIdentifier, null,
                discardThresholds);

        final LeaderboardGroup leaderboardGroup = service.addLeaderboardGroup(UUID.randomUUID(), leaderboardGroupName, description, null, false,
                Collections.singletonList(regattaNameAndleaderboardName), null, null);
        service.updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(),
                Collections.singleton(leaderboardGroup.getId()), event.getOfficialWebsiteURL(), event.getBaseURL(),
                event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos());

        final RaceColumn raceColumn = regattaLeaderboard.getRaceColumns().iterator().next();
        final Fleet fleet = raceColumn.getFleets().iterator().next();

        final RaceLog raceLog = raceColumn.getRaceLog(fleet);
        // TODO these are just the defaults used in the UI
        final LogEventAuthorImpl author = new LogEventAuthorImpl(raceLogEventAuthorName, raceLogEventPriority);
        
        final TimePoint startOfTracking = firstFixAt;
        final TimePoint endOfTracking = lastFixAt;
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
        // TODO explicitly set startOfRace?

        try {
            adapter.denoteRaceForRaceLogTracking(service, regattaLeaderboard, raceColumn, fleet, null);
            final RaceHandle raceHandle = adapter.startTracking(service, regattaLeaderboard, raceColumn, fleet, true, true);
            
            // TODO do we need to wait or is the TrackedRace guaranteed to be reachable after calling startTracking?
            raceHandle.getRace();

            final DynamicTrackedRace trackedRace = (DynamicTrackedRace) raceColumn.getTrackedRace(fleet);
            
            final WindImportResult windImportResult = new AbstractWindImporter.WindImportResult();
            final WindSourceWithAdditionalID windSource = new WindSourceWithAdditionalID(WindSourceType.EXPEDITION, windSourceId);
            final Map<InputStream, String> streamsWithFilenames = new HashMap<>();
            streamsWithFilenames.put(fileItem.getInputStream(), filename);
            new WindImporter().importWindToWindSourceAndTrackedRaces(service, windImportResult, windSource, Arrays.asList(trackedRace), streamsWithFilenames);

            return new ImporterResult(event.getId(), regattaNameAndleaderboardName, trackedRace.getRaceIdentifier());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
