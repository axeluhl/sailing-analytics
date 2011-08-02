package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.NamedImpl;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util.Pair;

public class LeaderboardImpl extends NamedImpl implements Leaderboard {
    private final List<TrackedRace> races;
    private final ScoreCorrection scoreCorrection;
    private final ResultDiscardingRule resultDiscardingRule;
    
    /**
     * A leaderboard entry representing a snapshot of a cell at a given time point for a single race/competitor.
     * 
     * @author Axel Uhl (d043530)
     *
     */
    private class EntryImpl implements Entry {
        private final int trackedPoints;
        private final int netPoints;
        private final MaxPointsReason maxPointsReason;
        private final boolean discarded;
        private EntryImpl(int trackedPoints, Result scoreCorrectionResult, boolean discarded) {
            super();
            this.trackedPoints = trackedPoints;
            this.netPoints = scoreCorrectionResult.getCorrectedScore();
            this.maxPointsReason = scoreCorrectionResult.getMaxPointsReason();
            this.discarded = discarded;
        }
        @Override
        public int getTrackedPoints() {
            return trackedPoints;
        }
        @Override
        public int getNetPoints() {
            return netPoints;
        }
        @Override
        public int getTotalPoints() {
            return discarded ? 0 : getNetPoints();
        }
        @Override
        public MaxPointsReason getMaxPointsReason() {
            return maxPointsReason;
        }
        @Override
        public boolean isDiscarded() {
            return discarded;
        }
    }

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
        return getScoreCorrection().getCorrectedScore(getTrackedPoints(competitor, race, timePoint), competitor, race,
                timePoint).getCorrectedScore();
    }
    
    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        return getScoreCorrection().getCorrectedScore(getTrackedPoints(competitor, race, timePoint), competitor, race,
                timePoint).getMaxPointsReason();
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

    @Override
    public Entry getEntry(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        int trackedPoints = getTrackedPoints(competitor, race, timePoint);
        Result correctedResults = getScoreCorrection().getCorrectedScore(trackedPoints, competitor, race, timePoint);
        return new EntryImpl(trackedPoints, correctedResults, isDiscarded(competitor, race, timePoint));
    }
    
    @Override
    public Map<Pair<Competitor, TrackedRace>, Entry> getContent(TimePoint timePoint) throws NoWindException {
        Map<Pair<Competitor, TrackedRace>, Entry> result = new HashMap<Pair<Competitor, TrackedRace>, Entry>();
        Map<Competitor, Set<TrackedRace>> discardedRaces = new HashMap<Competitor, Set<TrackedRace>>();
        for (TrackedRace race : getRaces()) {
            for (Competitor competitor : race.getRace().getCompetitors()) {
                int trackedPoints;
                if (race.hasStarted(timePoint)) {
                    trackedPoints = race.getRank(competitor, timePoint);
                } else {
                    trackedPoints = 0;
                }
                Result correctedResults = getScoreCorrection().getCorrectedScore(trackedPoints, competitor, race, timePoint);
                Set<TrackedRace> discardedRacesForCompetitor = discardedRaces.get(competitor);
                if (discardedRacesForCompetitor == null) {
                    discardedRacesForCompetitor = getResultDiscardingRule().getDiscardedRaces(competitor, races, timePoint);
                    discardedRaces.put(competitor, discardedRacesForCompetitor);
                }
                boolean discarded = discardedRacesForCompetitor.contains(race);
                Entry entry = new EntryImpl(trackedPoints, correctedResults, discarded);
                result.put(new Pair<Competitor, TrackedRace>(competitor, race), entry);
            }
        }
        return result;
    }

}
