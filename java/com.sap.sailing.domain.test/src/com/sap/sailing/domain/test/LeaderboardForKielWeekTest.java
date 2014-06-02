package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;
import com.sap.sse.common.UtilNew;

public class LeaderboardForKielWeekTest extends OnlineTracTracBasedTest {

    private final Logger logger = Logger.getLogger(LeaderboardForKielWeekTest.class.getName());
    private FlexibleLeaderboardImpl leaderboard;

    public LeaderboardForKielWeekTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void leaderboardWithOneRaceTest() throws URISyntaxException, NoWindException, IOException, InterruptedException {
        leaderboard = new FlexibleLeaderboardImpl("Kiel Week 2011 505s", new ThresholdBasedResultDiscardingRuleImpl(new int[] { 3, 6 }),
                new LowPoint(), null);
        Fleet defaultFleet = leaderboard.getFleet(null);
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        loadRace("event_20110609_KielerWoch-505_Race_2.txt", "event_20110609_KielerWoch-505_Race_2.mtb"); // 505 Race 2
        Competitor kevin = getCompetitorByName("Kevin");
        RaceColumn column = leaderboard.addRace(getTrackedRace(), "Test Race 1", /* medalRace */ false, defaultFleet);
        assertEquals(17, leaderboard.getTotalPoints(kevin, now), 0.000000001);
        UtilNew.Pair<Competitor, RaceColumn> key = new UtilNew.Pair<Competitor, RaceColumn>(kevin, column);
        assertEquals(17, leaderboard.getContent(now).get(key).getTotalPoints(), 0.000000001);
        assertEquals(17, leaderboard.getEntry(kevin, column, now).getTotalPoints(), 0.000000001);
        loadRace("event_20110609_KielerWoch-505_Race_3.txt", "event_20110609_KielerWoch-505_Race_3.mtb"); // 505 Race 3
        column = leaderboard.addRace(getTrackedRace(), "Test Race 2", /* medalRace */ false, defaultFleet);
        key = new UtilNew.Pair<Competitor, RaceColumn>(kevin, column);
        // In Race 3, Hasso ranked 31st
        assertEquals(29, leaderboard.getTotalPoints(kevin, now), 0.000000001);
        assertEquals(12, leaderboard.getContent(now).get(key).getTotalPoints(), 0.000000001);
        assertEquals(12, leaderboard.getEntry(kevin, column, now).getTotalPoints(), 0.000000001);
        loadRace("event_20110609_KielerWoch-505_race_4.txt", "event_20110609_KielerWoch-505_race_4.mtb"); // 505 Race 4
        column = leaderboard.addRace(getTrackedRace(), "Test Race 3", /* medalRace */ false, defaultFleet);
        key = new UtilNew.Pair<Competitor, RaceColumn>(kevin, column);
        // now the first race is discarded because Kevin ranked worst compared to the other two; in race 4 he ranked 10th
        assertEquals(22, leaderboard.getTotalPoints(kevin, now), 0.000000001);
        assertEquals(10, leaderboard.getContent(now).get(key).getTotalPoints(), 0.000000001);
        assertEquals(10, leaderboard.getEntry(kevin, column, now).getTotalPoints(), 0.000000001);
    }

    private void loadRace(String paramsFile, String storedDataFile) throws MalformedURLException, IOException, InterruptedException,
            URISyntaxException {
        final String raceName = "505 Race 2 from Kieler Woche 2011";
        logger.info("Loading race "+raceName);
        URI storedUri = new URI("file:///"+new File("resources/"+storedDataFile).getCanonicalPath().replace('\\', '/'));
        super.setUp(new URL("file:///"+new File("resources/"+paramsFile).getCanonicalPath()),
                /* liveUri */ null, /* storedUri */ storedUri,
                new ReceiverType[] { ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.MARKPASSINGS });
        logger.info("Recording wind for " + raceName);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70))), new WindSourceImpl(WindSourceType.WEB));
        logger.info("Fixing mark positions for " + raceName);
        fixApproximateMarkPositionsForWindReadOut(getTrackedRace(), new MillisecondsTimePoint(new GregorianCalendar(2011, 05, 23).getTime()));
        logger.info("Loaded race " + raceName);
    }
}
