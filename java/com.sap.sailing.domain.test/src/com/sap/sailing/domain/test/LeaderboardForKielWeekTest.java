package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.junit.Test;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.Util.Pair;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ResultDiscardingRuleImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;
import com.sap.sailing.domain.tracking.impl.WindImpl;
import com.sap.sailing.domain.tractracadapter.ReceiverType;

public class LeaderboardForKielWeekTest extends OnlineTracTracBasedTest {

    private LeaderboardImpl leaderboard;

    public LeaderboardForKielWeekTest() throws MalformedURLException, URISyntaxException {
        super();
    }

    @Test
    public void leaderboardWithOneRaceTest() throws URISyntaxException, NoWindException, IOException, InterruptedException {
        leaderboard = new LeaderboardImpl("Kiel Week 2011 505s", new ScoreCorrectionImpl(), new ResultDiscardingRuleImpl(new int[] { 3, 6 }));
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        loadRace("357c700a-9d9a-11e0-85be-406186cbf87c"); // 505 Race 2
        Competitor hasso = getCompetitorByName("Dr.Plattner");
        RaceInLeaderboard column = leaderboard.addRace(getTrackedRace(), "Test Race 1", /* medalRace */ false);
        assertEquals(21, leaderboard.getTotalPoints(hasso, now));
        Pair<Competitor, RaceInLeaderboard> key = new Pair<Competitor, RaceInLeaderboard>(hasso, column);
        assertEquals(21, leaderboard.getContent(now).get(key).getTotalPoints());
        assertEquals(21, leaderboard.getEntry(hasso, column, now).getTotalPoints());
        loadRace("e876c3a0-9da8-11e0-85be-406186cbf87c"); // 505 Race 3
        column = leaderboard.addRace(getTrackedRace(), "Test Race 2", /* medalRace */ false);
        key = new Pair<Competitor, RaceInLeaderboard>(hasso, column);
        // In Race 3, Hasso ranked 33th
        assertEquals(54, leaderboard.getTotalPoints(hasso, now));
        assertEquals(33, leaderboard.getContent(now).get(key).getTotalPoints());
        assertEquals(33, leaderboard.getEntry(hasso, column, now).getTotalPoints());
        loadRace("7c666e50-9dde-11e0-85be-406186cbf87c"); // 505 Race 4
        column = leaderboard.addRace(getTrackedRace(), "Test Race 3", /* medalRace */ false);
        key = new Pair<Competitor, RaceInLeaderboard>(hasso, column);
        // now the second race is discarded because Hasso ranked worst compared to the other two; in race 4 he ranked 11th
        assertEquals(32, leaderboard.getTotalPoints(hasso, now));
        assertEquals(11, leaderboard.getContent(now).get(key).getTotalPoints());
        assertEquals(11, leaderboard.getEntry(hasso, column, now).getTotalPoints());
    }

    private void loadRace(String raceId) throws MalformedURLException, IOException, InterruptedException,
            URISyntaxException {
        setUp("event_20110609_KielerWoch", raceId, ReceiverType.RACECOURSE, ReceiverType.RACESTARTFINISH, ReceiverType.MARKPASSINGS);
        getTrackedRace().setWindSource(WindSource.WEB);
        getTrackedRace().recordWind(new WindImpl(/* position */ null, MillisecondsTimePoint.now(),
                new KnotSpeedWithBearingImpl(12, new DegreeBearingImpl(70))), WindSource.WEB);
        fixApproximateMarkPositionsForWindReadOut(getTrackedRace());
    }
}
