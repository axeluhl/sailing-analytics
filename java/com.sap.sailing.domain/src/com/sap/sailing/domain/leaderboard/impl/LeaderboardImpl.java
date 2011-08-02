package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.NamedImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LeaderboardImpl extends NamedImpl implements Leaderboard {
    private final List<TrackedRace> races;
    private final ScoreCorrection scoreCorrection;
    private final ResultDiscardingRule resultDiscardingRule;

    public LeaderboardImpl(String name, ScoreCorrection scoreCorrection, ResultDiscardingRule resultDiscardingRule) {
        super(name);
        this.races = new ArrayList<TrackedRace>();
        this.scoreCorrection = scoreCorrection;
        this.resultDiscardingRule = resultDiscardingRule;
    }
    
    @Override
    public void addRace(TrackedRace race) {
        if (!races.contains(race)) {
            races.add(race);
        }
    }

    @Override
    public Iterable<TrackedRace> getRaces() {
        return Collections.unmodifiableList(races);
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (TrackedRace r : getRaces()) {
            for (Competitor c : r.getRace().getCompetitors()) {
                result.add(c);
            }
        }
        return result;
    }

    private ScoreCorrection getScoreCorrection() {
        return scoreCorrection;
    }
    
    private ResultDiscardingRule getResultDiscardingRule() {
        return resultDiscardingRule;
    }

    @Override
    public int getTrackedPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        return race.hasStarted(timePoint) ? race.getRank(competitor, timePoint) : 0;
    }

    @Override
    public int getNetPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        return getScoreCorrection().getCorrectedScore(getTrackedPoints(competitor, race, timePoint), competitor, race, timePoint);
    }
    
    @Override
    public boolean isDiscarded(Competitor competitor, TrackedRace race, TimePoint timePoint) {
        return getResultDiscardingRule().getDiscardedRaces(competitor, races, timePoint).contains(race);
    }

    @Override
    public int getTotalPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        return isDiscarded(competitor, race, timePoint) ? 0 : getNetPoints(competitor, race, timePoint);
    }
    
    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = 0;
        for (TrackedRace r : getRaces()) {
            result += getTotalPoints(competitor, r, timePoint);
        }
        return result;
    }

}
