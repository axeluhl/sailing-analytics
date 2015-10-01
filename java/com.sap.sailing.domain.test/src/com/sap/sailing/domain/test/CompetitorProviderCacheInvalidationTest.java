package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.abstractlog.regatta.events.impl.RegattaLogRegisterCompetitorEventImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.base.impl.SeriesImpl;
import com.sap.sailing.domain.common.BoatClassMasterdata;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.leaderboard.impl.CompetitorProviderFromRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.racelog.impl.EmptyRaceLogStore;
import com.sap.sailing.domain.regattalog.impl.EmptyRegattaLogStore;
import com.sap.sailing.domain.test.mock.MockedTrackedRaceWithStartTimeAndRanks;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Tests the behavior of the class {@link CompetitorProviderFromRaceColumnsAndRegattaLike}, paying particular
 * attention to its caching and cache invalidation logic. When competitors are added to regatta logs or race logs
 * or when a tracked race is attached to a column or a column is added or removed, its caches need to be
 * invalidated and re-calculated accordingly.
 *  
 * @author Axel Uhl (d043530)
 *
 */
public class CompetitorProviderCacheInvalidationTest extends AbstractLeaderboardTest {
    private CompetitorProviderFromRaceColumnsAndRegattaLike competitorProviderFlexibleLeaderboard;
    private CompetitorProviderFromRaceColumnsAndRegattaLike competitorProviderRegattaLeaderboard;
    private FlexibleLeaderboardImpl flexibleLeaderboard;
    private RegattaLeaderboardImpl regattaLeaderboard;
    private RegattaImpl regatta;
    final int NUMBER_OF_COMP_LISTS = 4;
    @SuppressWarnings("unchecked")
    private List<Competitor>[] compLists = (List<Competitor>[]) new List<?>[NUMBER_OF_COMP_LISTS];
    
    @Before
    public void setUp() {
        final CourseAreaImpl courseArea = new CourseAreaImpl(
                "Test Course Area", UUID.randomUUID());
        flexibleLeaderboard = new FlexibleLeaderboardImpl("Test Flexible Leaderboard",
                new ThresholdBasedResultDiscardingRuleImpl(new int[0]), new LowPoint(), courseArea);
        competitorProviderFlexibleLeaderboard = new CompetitorProviderFromRaceColumnsAndRegattaLike(flexibleLeaderboard);
        regatta = new RegattaImpl(EmptyRaceLogStore.INSTANCE,
                EmptyRegattaLogStore.INSTANCE, "Test Regatta", new BoatClassImpl("49er", BoatClassMasterdata._49ER), /* startDate */
                null, /* endDate */null, /* trackedRegattaRegistry */null, new LowPoint(), UUID.randomUUID(),
                courseArea);
        regattaLeaderboard = new RegattaLeaderboardImpl(regatta, new ThresholdBasedResultDiscardingRuleImpl(new int[0]));
        regatta.addSeries(new SeriesImpl("Test Series", /* isMedal */false, Arrays.asList(new FleetImpl("Yellow"),
                new FleetImpl("Blue")), Arrays.asList("R1", "R2", "R3"), /* trackedRegattaRegistry */null));
        for (int l = 0; l < NUMBER_OF_COMP_LISTS; l++) {
            compLists[l] = new ArrayList<Competitor>();
            for (int i = 0; i < 10; i++) {
                compLists[l].add(createCompetitor("" + l + "/" + i));
            }
        }
        competitorProviderRegattaLeaderboard = new CompetitorProviderFromRaceColumnsAndRegattaLike(regattaLeaderboard);
    }
    
    @Test
    public void testSimpleCompetitorListOnOneRaceInFlexibleLeaderboard() {
        TrackedRace trackedRace = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[0]);
        flexibleLeaderboard.addRace(trackedRace, "R1", /* medalRace */ false);
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRace = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R1"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace);
        assertEquals(expected, actualForRace);
    }

    @Test
    public void testSimpleCompetitorListOnOneRaceLogInFlexibleLeaderboard() {
        flexibleLeaderboard.addRaceColumn("R1", /* medalRace */ false);
        RaceLog raceLog = flexibleLeaderboard.getRacelog("R1", LeaderboardNameConstants.DEFAULT_FLEET_NAME);
        for (Competitor c : compLists[0]) {
            raceLog.add(RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), 1, c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRace = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R1"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace);
        assertEquals(expected, actualForRace);
    }

    @Test
    public void testSimpleCompetitorListOnRegattaLogInFlexibleLeaderboard() {
        flexibleLeaderboard.addRaceColumn("R1", /* medalRace */ false);
        RegattaLog regattaLog = flexibleLeaderboard.getRegattaLog();
        for (Competitor c : compLists[0]) {
            regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), MillisecondsTimePoint.now(), UUID.randomUUID(), c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRace = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R1"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace);
        assertEquals(expected, actualForRace);
    }

    @Test
    public void testSimpleCompetitorListOnOneRaceLogAndRegattaLogInFlexibleLeaderboard() {
        flexibleLeaderboard.addRaceColumn("R1", /* medalRace */ false);
        RegattaLog regattaLog = flexibleLeaderboard.getRegattaLog();
        for (Competitor c : compLists[0]) {
            regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), MillisecondsTimePoint.now(), UUID.randomUUID(), c));
        }
        RaceLog raceLog = flexibleLeaderboard.getRacelog("R1", LeaderboardNameConstants.DEFAULT_FLEET_NAME);
        for (Competitor c : compLists[1]) {
            raceLog.add(RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), 1, c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        expected.addAll(compLists[1]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRace = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R1"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace);
        assertEquals(expected, actualForRace);
    }

    @Test
    public void testTwoCompetitorListsOnTwoRacesInFlexibleLeaderboard() {
        TrackedRace trackedRace1 = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[0]);
        flexibleLeaderboard.addRace(trackedRace1, "R1", /* medalRace */ false);
        TrackedRace trackedRace2 = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[1]);
        flexibleLeaderboard.addRace(trackedRace2, "R2", /* medalRace */ false);
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        expected.addAll(compLists[1]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRace1 = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R1"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace1);
        Set<Competitor> actualForRace2 = new HashSet<>();
        Util.addAll(competitorProviderFlexibleLeaderboard.getAllCompetitors(
                flexibleLeaderboard.getRaceColumnByName("R2"),
                flexibleLeaderboard.getFleet(LeaderboardNameConstants.DEFAULT_FLEET_NAME)), actualForRace2);
        assertEquals(new HashSet<>(compLists[0]), actualForRace1);
        assertEquals(new HashSet<>(compLists[1]), actualForRace2);
    }

    @Test
    public void testSimpleCompetitorListOnOneRaceInRegattaLeaderboard() {
        TrackedRace trackedRace = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[0]);
        regattaLeaderboard.getRaceColumnByName("R1").setTrackedRace(regattaLeaderboard.getRaceColumnByName("R1").getFleetByName("Yellow"), trackedRace);
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRaceYellow = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Yellow")), actualForRaceYellow);
        assertEquals(expected, actualForRaceYellow);
        Set<Competitor> actualForRaceBlue = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Blue")), actualForRaceBlue);
        assertEquals(Collections.emptySet(), actualForRaceBlue);
    }

    @Test
    public void testTwoCompetitorListsOnTwoRacesInRegattaLeaderboard() {
        TrackedRace trackedRace1 = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[0]);
        regattaLeaderboard.getRaceColumnByName("R1").setTrackedRace(regattaLeaderboard.getRaceColumnByName("R1").getFleetByName("Yellow"), trackedRace1);
        TrackedRace trackedRace2 = new MockedTrackedRaceWithStartTimeAndRanks(MillisecondsTimePoint.now(), compLists[1]);
        regattaLeaderboard.getRaceColumnByName("R1").setTrackedRace(regattaLeaderboard.getRaceColumnByName("R1").getFleetByName("Blue"), trackedRace2);
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        expected.addAll(compLists[1]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRaceYellow = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Yellow")), actualForRaceYellow);
        assertEquals(new HashSet<>(compLists[0]), actualForRaceYellow);
        Set<Competitor> actualForRaceBlue = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Blue")), actualForRaceBlue);
        assertEquals(new HashSet<>(compLists[1]), actualForRaceBlue);
    }

    @Test
    public void testSimpleCompetitorListOnOneRaceLogInRegattaLeaderboard() {
        RaceLog raceLog = regattaLeaderboard.getRacelog("R1", "Yellow");
        for (Competitor c : compLists[0]) {
            raceLog.add(RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), 1, c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRaceYellow = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Yellow")), actualForRaceYellow);
        assertEquals(new HashSet<>(compLists[0]), actualForRaceYellow);
    }

    @Test
    public void testSimpleCompetitorListOnRegattaLogInRegattaLeaderboard() {
        RegattaLog regattaLog = regattaLeaderboard.getRegatta().getRegattaLog();
        for (Competitor c : compLists[0]) {
            regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), MillisecondsTimePoint.now(), UUID.randomUUID(), c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRaceYellow = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Yellow")), actualForRaceYellow);
        assertEquals(new HashSet<>(compLists[0]), actualForRaceYellow);
        Set<Competitor> actualForRaceBlue = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Blue")), actualForRaceBlue);
        assertEquals(new HashSet<>(compLists[0]), actualForRaceBlue);
    }

    @Test
    public void testSimpleCompetitorListOnOneRaceLogAndRegattaLogInRegattaLeaderboard() {
        RegattaLog regattaLog = regattaLeaderboard.getRegatta().getRegattaLog();
        for (Competitor c : compLists[0]) {
            regattaLog.add(new RegattaLogRegisterCompetitorEventImpl(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), MillisecondsTimePoint.now(), UUID.randomUUID(), c));
        }
        RaceLog raceLog = regattaLeaderboard.getRacelog("R1", "Yellow");
        for (Competitor c : compLists[1]) {
            raceLog.add(RaceLogEventFactory.INSTANCE.createRegisterCompetitorEvent(MillisecondsTimePoint.now(), new LogEventAuthorImpl("Me", 0), 1, c));
        }
        Set<Competitor> expected = new HashSet<>(compLists[0]);
        expected.addAll(compLists[1]);
        Set<Competitor> actual = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(), actual);
        assertEquals(expected, actual);
        Set<Competitor> actualForRaceYellow = new HashSet<>();
        Util.addAll(competitorProviderRegattaLeaderboard.getAllCompetitors(
                regattaLeaderboard.getRaceColumnByName("R1"), regattaLeaderboard.getFleet("Yellow")), actualForRaceYellow);
        assertEquals(expected, actualForRaceYellow);
    }

}
