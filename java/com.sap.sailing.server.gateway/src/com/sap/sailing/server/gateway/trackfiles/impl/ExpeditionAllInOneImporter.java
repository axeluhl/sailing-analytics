package com.sap.sailing.server.gateway.trackfiles.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

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
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ExpeditionAllInOneImporter {
    
    public static class ImporterResult {
        private final UUID eventId;
        private final String leaderboardName;
        private final RegattaAndRaceIdentifier regattaAndRaceIdentifier;

        public ImporterResult(UUID eventId, String leaderboardName, RegattaAndRaceIdentifier regattaAndRaceIdentifier) {
            this.eventId = eventId;
            this.leaderboardName = leaderboardName;
            this.regattaAndRaceIdentifier = regattaAndRaceIdentifier;
        }
    }

    public ImporterResult importFiles(RacingEventService service, RaceLogTrackingAdapter adapter, String filename, Supplier<InputStream> streamSupplier) {
        String eventName = filename;
        String leaderboardName = filename;
        RegattaIdentifier regattaIdentifier = new RegattaName(leaderboardName);
        String raceColumnName = filename;
        String courseAreaName = "Default";
        UUID courseAreaId = UUID.randomUUID();
        Double buoyZoneRadiusInHullLengths = 3.0;
        String venueName = filename;
        Event event = service.addEvent(eventName, null, new MillisecondsTimePoint(0), MillisecondsTimePoint.now(), venueName, true, UUID.randomUUID());
        service.addCourseAreas(event.getId(), new String[]{courseAreaName}, new UUID[]{courseAreaId});
        
        Series series = new SeriesImpl(Series.DEFAULT_NAME, false, false, Collections.singleton(new FleetImpl(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), Collections.singleton(raceColumnName), (TrackedRegattaRegistry) service);
        ScoringScheme scoringScheme = service.getBaseDomainFactory().createScoringScheme(ScoringSchemeType.HIGH_POINT);
        RankingMetricConstructor rankingMetricConstructor = RankingMetricsFactory.getRankingMetricConstructor(RankingMetrics.ONE_DESIGN);
        service.createRegatta(leaderboardName, null, null, null, UUID.randomUUID(), Collections.singleton(series), true, scoringScheme, courseAreaId, buoyZoneRadiusInHullLengths, true, false, rankingMetricConstructor);
        RegattaLeaderboard regattaLeaderboard = service.addRegattaLeaderboard(regattaIdentifier, null, new int[0]);
        RaceColumn raceColumn = regattaLeaderboard.getRaceColumns().iterator().next();
        Fleet fleet = raceColumn.getFleets().iterator().next();
        
        LeaderboardGroup leaderboardGroup = service.addLeaderboardGroup(UUID.randomUUID(), eventName, null, null, false, Collections.singletonList(leaderboardName), null, null);
        service.updateEvent(event.getId(), event.getName(), event.getDescription(), event.getStartDate(), event.getEndDate(), event.getVenue().getName(), event.isPublic(), Collections.singleton(leaderboardGroup.getId()), event.getOfficialWebsiteURL(), event.getBaseURL(), event.getSailorsInfoWebsiteURLs(), event.getImages(), event.getVideos());
        
        try {
            adapter.denoteRaceForRaceLogTracking(service, regattaLeaderboard, raceColumn, fleet, null);
            RaceHandle raceHandle = adapter.startTracking(service, regattaLeaderboard, raceColumn, fleet, true, true);
            
            TrackedRace trackedRace = raceColumn.getTrackedRace(fleet);
            
            return new ImporterResult(event.getId(), leaderboardName, trackedRace.getRaceIdentifier());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
