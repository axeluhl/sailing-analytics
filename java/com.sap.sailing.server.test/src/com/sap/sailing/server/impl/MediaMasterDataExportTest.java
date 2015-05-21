package com.sap.sailing.server.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardGroupImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.masterdataimport.TopLevelMasterData;
import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class MediaMasterDataExportTest {
    
    static final String matchingRegattaName = "Matching Regatta";
    static final String missingRegattaName = "Missing Regatta";
    static final String matchingRace1 = "matching race 1";
    static final String matchingRace2 = "matching race 2";
    static final String matchingRace3 = "matching race 3";
    static final String missingRace1 = "missing race 1";
    static final String missingRace2 = "missing race 2";
    static final String missingRace3 = "missing race 3";
    
    static final boolean displayGroupsInReverseOrder = true;
    static final ThresholdBasedResultDiscardingRule resultDiscardingRule = new ThresholdBasedResultDiscardingRuleImpl(new int[0]);
    static final BoatClass boatClass = new BoatClassImpl("boat class name", BoatClassMasterdata._12M);
    static final TimePoint startDate = MillisecondsTimePoint.now();
    static final TimePoint endDate = startDate.plus(MillisecondsDurationImpl.ONE_DAY);
    static final boolean isMedal = false;
    static final Fleet regattaFleet = new FleetImpl("fleet name");
    static final boolean persistent = false;
    static final ScoringScheme scoringScheme = new LowPoint();
    static final CourseArea courseArea = new CourseAreaImpl("Course Area", UUID.randomUUID());
    static final Serializable regatteId = "regatta id";
    
    @Test
    public void testTrackWithAssignedRaceButEmptyLeaderboardGroup() {
        TimePoint startTime = null;
        Duration duration = null;
        MimeType mimeType = MimeType.mp4;
        Collection<MediaTrack> allMediaTracks = Arrays.asList(
                new MediaTrack("dbId 1", "title 1 for existing regatta and race", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(matchingRegattaName, matchingRace1))),
                new MediaTrack("dbId 2", "title 2 for existing one regatta and race and one missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace2), new RegattaNameAndRaceName(matchingRegattaName, matchingRace2)))),
                new MediaTrack("dbId 3", "title 1 for missing regatta", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(missingRegattaName, matchingRace1))),
                new MediaTrack("dbId 4", "title 2 for missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace3), new RegattaNameAndRaceName(matchingRegattaName, missingRace1))))
        );
        Collection<RaceIdentifier> regattaRaces = Collections.emptyList();
        Collection<RaceIdentifier> flexibleRaces = Collections.emptyList();
        TopLevelMasterData topLevelMasterData = createTopLevelMasterData(regattaRaces , flexibleRaces, allMediaTracks);
        Collection<MediaTrack> filteredMediaTracks = topLevelMasterData.getFilteredMediaTracks();
        assertEquals(0, filteredMediaTracks.size());
    }

    @Test
    public void testTracksWithAssignedRaceAndMatchingRegattaInLeaderboardGroup() {
        TimePoint startTime = null;
        Duration duration = null;
        MimeType mimeType = MimeType.mp4;
        Collection<MediaTrack> allMediaTracks = Arrays.asList(
                new MediaTrack("dbId 1", "title 1 for existing regatta and race", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(matchingRegattaName, matchingRace1))),
                new MediaTrack("dbId 2", "title 2 for existing one regatta and race and one missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace2), new RegattaNameAndRaceName(matchingRegattaName, matchingRace2)))),
                new MediaTrack("dbId 3", "title 3 for missing regatta", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(missingRegattaName, matchingRace1))),
                new MediaTrack("dbId 4", "title 4 for missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace3), new RegattaNameAndRaceName(matchingRegattaName, missingRace1)))),
                new MediaTrack("dbId 5", "title 5 for existing regatta and race", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(matchingRegattaName, matchingRace1)))
        );
        Collection<RaceIdentifier> regattaRaces = Arrays.asList(
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace1),
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace2),
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace3)
        );
        Collection<RaceIdentifier> flexibleRaces = Collections.emptyList();
        TopLevelMasterData topLevelMasterData = createTopLevelMasterData(regattaRaces , flexibleRaces, allMediaTracks);
        Collection<MediaTrack> filteredMediaTracks = topLevelMasterData.getFilteredMediaTracks();
        assertEquals(3, filteredMediaTracks.size());
    }

    @Test
    public void testTrackWithAssignedRaceAndMatchingFlexibleLeaderboardInLeaderboardGroup() {
        TimePoint startTime = null;
        Duration duration = null;
        MimeType mimeType = MimeType.mp4;
        Collection<MediaTrack> allMediaTracks = Arrays.asList(
                new MediaTrack("dbId 1", "title 1 for existing regatta and race", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(matchingRegattaName, matchingRace1))),
                new MediaTrack("dbId 2", "title 2 for existing one regatta and race and one missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace2), new RegattaNameAndRaceName(matchingRegattaName, matchingRace2)))),
                new MediaTrack("dbId 3", "title 3 for missing regatta", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(missingRegattaName, matchingRace1))),
                new MediaTrack("dbId 4", "title 4 for missing regatta", "url", startTime, duration, mimeType, new HashSet<RegattaAndRaceIdentifier>(Arrays.asList(new RegattaNameAndRaceName(missingRegattaName, matchingRace3), new RegattaNameAndRaceName(matchingRegattaName, missingRace1)))),
                new MediaTrack("dbId 5", "title 5 for existing regatta and race", "url", startTime, duration, mimeType, Collections.singleton(new RegattaNameAndRaceName(matchingRegattaName, matchingRace1)))
        );
        Collection<RaceIdentifier> regattaRaces = Collections.emptyList();
        Collection<RaceIdentifier> flexibleRaces = Arrays.asList(
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace1),
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace2),
                new RegattaNameAndRaceName(matchingRegattaName, matchingRace3)
        );
        TopLevelMasterData topLevelMasterData = createTopLevelMasterData(regattaRaces , flexibleRaces, allMediaTracks);
        Collection<MediaTrack> filteredMediaTracks = topLevelMasterData.getFilteredMediaTracks();
        assertEquals(3, filteredMediaTracks.size());
    }

    private TopLevelMasterData createTopLevelMasterData(Collection<RaceIdentifier> regattaRaces, Collection<RaceIdentifier> flexibleRaces, Collection<MediaTrack> allMediaTracks) {
        Set<String> raceColumnNames = new HashSet<>();
        for (RaceIdentifier regattaAndRaceIdentifier : regattaRaces) {
            raceColumnNames.add(regattaAndRaceIdentifier.getRaceName());
        }
        Regatta regatta = createTestRegatta("regatta name",  raceColumnNames);
        RegattaLeaderboard regattaLeaderboard = new RegattaLeaderboardImpl(regatta, resultDiscardingRule);
        assignRacesToRegattaLeaderboardColumns(regattaLeaderboard, regattaRaces);
        
        FlexibleLeaderboard flexibleLeaderboard = new FlexibleLeaderboardImpl("flexible leaderboard", resultDiscardingRule, scoringScheme , courseArea);
        for (RaceIdentifier regattaAndRaceIdentifier : flexibleRaces) {
            RaceColumn raceColumn = flexibleLeaderboard.addRaceColumn(regattaAndRaceIdentifier.getRaceName(), isMedal);
            Fleet defaultFleet = raceColumn.getFleets().iterator().next();
            raceColumn.setRaceIdentifier(defaultFleet, regattaAndRaceIdentifier);
        }
        

        List<? extends Leaderboard> leaderboards = Arrays.asList(regattaLeaderboard, flexibleLeaderboard);
        LeaderboardGroup leaderboardGroup = new LeaderboardGroupImpl("name", "description", "displayName", displayGroupsInReverseOrder , leaderboards);
        Set<LeaderboardGroup> groupsToExport = Collections.singleton(leaderboardGroup);
        Iterable<Event> allEvents = Collections.emptyList();
        Map<String, Regatta> regattaForRaceIdString = Collections.emptyMap();
        GPSFixStore gpsFixStore = mock(GPSFixStore.class);
        boolean exportWind = false;
        TopLevelMasterData topLevelMasterData = new TopLevelMasterData(groupsToExport , allEvents , regattaForRaceIdString, allMediaTracks , gpsFixStore , exportWind);
        return topLevelMasterData;
    }

    public static void assignRacesToRegattaLeaderboardColumns(RegattaLeaderboard leaderboard, Collection<RaceIdentifier> raceIdentifiers) {
        Iterator<RaceIdentifier> regattaRacesIterator = raceIdentifiers.iterator();
        for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            raceColumn.setRaceIdentifier(regattaFleet, regattaRacesIterator.next());
        }
    }

    public static RegattaImpl createTestRegatta(String regattaName, Iterable<String> raceColumnNames) {
        Iterable<? extends Fleet> regattaFleets = Collections.singleton(regattaFleet);
        TrackedRegattaRegistry trackedRegattaRegistry = mock(TrackedRegattaRegistry.class);
        Series series = new SeriesImpl("series name", isMedal, regattaFleets, raceColumnNames, trackedRegattaRegistry);
        Iterable<? extends Series> regattaSeries = Collections.singleton(series);
        return new RegattaImpl(regattaName, boatClass, startDate, endDate, regattaSeries, persistent, scoringScheme, regatteId , courseArea);
    }



}
