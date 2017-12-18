package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.racelogtracking.RaceLogTrackingAdapter;
import com.sap.sailing.domain.ranking.RankingMetricConstructor;
import com.sap.sailing.domain.ranking.RankingMetricsFactory;
import com.sap.sailing.domain.tracking.RaceHandle;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TypeBasedServiceFinderFactory;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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

        public ImporterResult(UUID eventId, String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
            this.eventId = eventId;
            this.leaderboardName = leaderboardName;
            this.regattaName = regattaAndRaceIdentifier.getRegattaName();
            this.raceName = regattaAndRaceIdentifier.getRaceName();
        }
    }
    
    public ExpeditionAllInOneImporter(RacingEventService service, RaceLogTrackingAdapter adapter, TypeBasedServiceFinderFactory serviceFinderFactory, BundleContext context) {
        this.service = service;
        this.adapter = adapter;
        this.serviceFinderFactory = serviceFinderFactory;
        this.context = context;
    }

    public ImporterResult importFiles(String filename, FileItem fileItem) {
        // TODO prevent duplicate event/leaderboard names
        String eventName = filename;
        String regattaNameAndleaderboardName = filename;
        RegattaIdentifier regattaIdentifier = new RegattaName(regattaNameAndleaderboardName);
        String raceColumnName = filename;
        String courseAreaName = "Default";
        UUID courseAreaId = UUID.randomUUID();
        // TODO provide boat class suggest in the UI?
        Double buoyZoneRadiusInHullLengths = 3.0;
        String boatClassName = null;

        // TODO guess venue based on the reverse geocoder?
        String venueName = filename;

        // TODO wild guess...
        ScoringSchemeType scoringSchemeType = ScoringSchemeType.HIGH_POINT;
        RankingMetrics rankingMetric = RankingMetrics.ONE_DESIGN;
        int[] discardThresholds = new int[0];

        Event event = service.addEvent(eventName, null, new MillisecondsTimePoint(0), MillisecondsTimePoint.now(),
                venueName, true, UUID.randomUUID());
        service.addCourseAreas(event.getId(), new String[] { courseAreaName }, new UUID[] { courseAreaId });

        Series series = new SeriesImpl(Series.DEFAULT_NAME, /* isMedal */ false, /* isFleetsCanRunInParallel */ false,
                Collections.singleton(new FleetImpl(LeaderboardNameConstants.DEFAULT_FLEET_NAME)),
                Collections.singleton(raceColumnName), /* trackedRegattaRegistry */ service);
        ScoringScheme scoringScheme = service.getBaseDomainFactory().createScoringScheme(scoringSchemeType);
        RankingMetricConstructor rankingMetricConstructor = RankingMetricsFactory
                .getRankingMetricConstructor(rankingMetric);
        service.createRegatta(regattaNameAndleaderboardName, boatClassName, null, null, UUID.randomUUID(),
                Collections.singleton(series), true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths, true,
                false, rankingMetricConstructor);
        RegattaLeaderboard regattaLeaderboard = service.addRegattaLeaderboard(regattaIdentifier, null,
                discardThresholds);

        LeaderboardGroup leaderboardGroup = service.addLeaderboardGroup(UUID.randomUUID(), eventName, null, null, false,
                Collections.singletonList(regattaNameAndleaderboardName), null, null);
        service.updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(),
                event.getEndDate(), event.getVenue().getName(), event.isPublic(),
                Collections.singleton(leaderboardGroup.getId()), event.getOfficialWebsiteURL(), event.getBaseURL(),
                event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos());

        RaceColumn raceColumn = regattaLeaderboard.getRaceColumns().iterator().next();
        Fleet fleet = raceColumn.getFleets().iterator().next();

        RaceLog raceLog = raceColumn.getRaceLog(fleet);
        // TODO these are just the defaults used in the UI
        final LogEventAuthorImpl author = new LogEventAuthorImpl("Shore", 4);
        
        JsonHolder jsonHolderForGpsFixImport = new JsonHolder(logger);
        List<Pair<String, FileItem>> filesForGpsFixImport = Arrays.asList(new Pair<>(filename, fileItem));
        try {
            new TrackFilesImporter(service, serviceFinderFactory, context).importFixes(jsonHolderForGpsFixImport, /* TODO preferred importer */ null, filesForGpsFixImport);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        JsonHolder jsonHolderForSensorFixImport = new JsonHolder(logger);
        Iterable<Pair<String, FileItem>> importerNamesAndFilesForSensorFixImport = Arrays.asList(new Pair<>(filename, fileItem));
        try {
            new SensorDataImporter(service, context).importFiles(false, jsonHolderForSensorFixImport, importerNamesAndFilesForSensorFixImport);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        // TODO import GPS and extended fixes here to determin start/endOfTracking
        // TODO how to map fixes to a competitor? Just return the IDs to the user and let him do the mapping?

        // TODO determine from imported file
        TimePoint startOfTracking = null;
        TimePoint endOfTracking = null;
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTracking, author, raceLog.getCurrentPassId()));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTracking, author, raceLog.getCurrentPassId()));
        // TODO explicitly set startOfRace?

        try {
            adapter.denoteRaceForRaceLogTracking(service, regattaLeaderboard, raceColumn, fleet, null);
            RaceHandle raceHandle = adapter.startTracking(service, regattaLeaderboard, raceColumn, fleet, true, true);
            
            // TODO do we need to wait or is the TrackedRace guaranteed to be reachable after calling startTracking?
            raceHandle.getRace();

            TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
            
            // TODO import wind here!

            return new ImporterResult(event.getId(), regattaNameAndleaderboardName, trackedRace.getRaceIdentifier());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
