package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Named;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoreCorrection;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.Result;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.Util.Pair;

public class LeaderboardImpl implements Named, Leaderboard {
    private final List<RaceInLeaderboard> races;
    private final ScoreCorrection scoreCorrection;
    private final ResultDiscardingRule resultDiscardingRule;
    private String name;
    
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

    /**
     * @param name must not be <code>null</code>
     */
    public LeaderboardImpl(String name, ScoreCorrection scoreCorrection, ResultDiscardingRule resultDiscardingRule) {
        if (name == null) {
            throw new IllegalArgumentException("A leaderboard's name must not be null");
        }
        this.name = name;
        this.races = new ArrayList<RaceInLeaderboard>();
        this.scoreCorrection = scoreCorrection;
        this.resultDiscardingRule = resultDiscardingRule;
    }
    
    @Override
    public void addRaceColumn(String name) {
        RaceInLeaderboardImpl column = new RaceInLeaderboardImpl(this, name);
        races.add(column);
    }
    
    private RaceInLeaderboard getRaceColumnByName(String columnName) {
        RaceInLeaderboard result = null;
        for (RaceInLeaderboard r : races) {
            if (r.getName().equals(columnName)) {
                result = r;
                break;
            }
        }
        return result;
    }
    
    @Override
    public void addRace(TrackedRace race, String columnName) {
        RaceInLeaderboard column = getRaceColumnByName(columnName);
        if (column == null) {
            column = new RaceInLeaderboardImpl(this, columnName);
            column.setTrackedRace(race);
            races.add(column);
        }
        column.setTrackedRace(race);
    }

    @Override
    public Iterable<TrackedRace> getRaces() {
        Set<TrackedRace> trackedRaces = new HashSet<TrackedRace>();
        for (RaceInLeaderboard r : races) {
            TrackedRace trackedRace = r.getTrackedRace();
            if (trackedRace != null) {
                trackedRaces.add(trackedRace);
            }
        }
        return Collections.unmodifiableSet(trackedRaces);
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
        Iterable<TrackedRace> trackedRaces = getRaces();
        return getResultDiscardingRule().getDiscardedRaces(competitor, trackedRaces, timePoint).contains(race);
    }

    @Override
    public int getTotalPoints(Competitor competitor, TrackedRace race, TimePoint timePoint) throws NoWindException {
        return isDiscarded(competitor, race, timePoint) ? 0 : getNetPoints(competitor, race, timePoint);
    }
    
    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = getCarriedPoints(competitor);
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
                    discardedRacesForCompetitor = getResultDiscardingRule().getDiscardedRaces(competitor, getRaces(), timePoint);
                    discardedRaces.put(competitor, discardedRacesForCompetitor);
                }
                boolean discarded = discardedRacesForCompetitor.contains(race);
                Entry entry = new EntryImpl(trackedPoints, correctedResults, discarded);
                result.put(new Pair<Competitor, TrackedRace>(competitor, race), entry);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param newName must not be <code>null</code>
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("A leaderboard's name must not be null");
        }
        this.name = newName;
    }

    @Override
    public int getCarriedPoints(Competitor competitor) {
        return 0;
    }

}
