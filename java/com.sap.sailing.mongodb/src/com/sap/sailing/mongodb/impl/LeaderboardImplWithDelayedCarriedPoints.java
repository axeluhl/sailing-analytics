package com.sap.sailing.mongodb.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.RaceInLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrection.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.LeaderboardImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Keeps a record of carried points and score corrections to be applied, keyed by the competitor names to which they
 * apply. This allows the {@link DomainObjectFactoryImpl} to set carried points and score corrections by competitor
 * names even when the {@link TrackedRace}s haven't been associated yet with the leaderboard. The carried points and
 * score corrections are then assigned to the {@link Competitor}s once they show up by
 * {@link #addRace(TrackedRace, String, boolean) adding races} to this leaderboard.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class LeaderboardImplWithDelayedCarriedPoints extends LeaderboardImpl {
    private final Map<String, Integer> carriedPointsByCompetitorName;
    private final Map<String, Map<RaceInLeaderboard, MaxPointsReason>> maxPointsReasonsByCompetitorName;
    private final Map<String, Map<RaceInLeaderboard, Integer>> correctedScoresByCompetitorName;
    
    public LeaderboardImplWithDelayedCarriedPoints(String name, SettableScoreCorrection scoreCorrection,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(name, scoreCorrection, resultDiscardingRule);
        carriedPointsByCompetitorName = new HashMap<String, Integer>();
        maxPointsReasonsByCompetitorName = new HashMap<String, Map<RaceInLeaderboard,MaxPointsReason>>();
        correctedScoresByCompetitorName = new HashMap<String, Map<RaceInLeaderboard,Integer>>();
    }

    void setCarriedPoints(String competitorName, int carriedPoints) {
        carriedPointsByCompetitorName.put(competitorName, carriedPoints);
    }
    
    void setMaxPointsReason(String competitorName, RaceInLeaderboard raceColumn, MaxPointsReason maxPointsReason) {
        Map<RaceInLeaderboard, MaxPointsReason> map = maxPointsReasonsByCompetitorName.get(competitorName);
        if (map == null) {
            map = new HashMap<RaceInLeaderboard, MaxPointsReason>();
            maxPointsReasonsByCompetitorName.put(competitorName, map);
        }
        map.put(raceColumn, maxPointsReason);
    }

    void correctScore(String competitorName, RaceInLeaderboard raceColumn, int correctedScore) {
        Map<RaceInLeaderboard, Integer> map = correctedScoresByCompetitorName.get(competitorName);
        if (map == null) {
            map = new HashMap<RaceInLeaderboard, Integer>();
            correctedScoresByCompetitorName.put(competitorName, map);
        }
        map.put(raceColumn, correctedScore);
    }

    /**
     * Performs the regular {@link LeaderboardImpl#addRace(TrackedRace, String, boolean)} and then checks if
     * there are any carried points etc. left over to assign to the competitor objects (see
     * {@link #assignLeftOvers(TrackedRace)}).
     */
    @Override
    public RaceInLeaderboard addRace(TrackedRace race, String columnName, boolean medalRace) {
        RaceInLeaderboard result = super.addRace(race, columnName, medalRace);
        assignLeftOvers(race);
        return result;
    }

    /**
     * Checks if there are any carried points, max points reasons or corrected scores left over that may now receive
     * their competitor record. If so, {@link #setCarriedPoints(com.sap.sailing.domain.base.Competitor, int)},
     * {@link #setMaxPointsReason(String, RaceInLeaderboard, MaxPointsReason)} and/or
     * {@link #correctedScoresByCompetitorName} is invoked respectively, based on the data from
     * {@link #carriedPointsByCompetitorName}, {@link #maxPointsReasonsByCompetitorName} and
     * {@link #correctedScoresByCompetitorName}, respectively, which is removed afterwards.
     */
    private void assignLeftOvers(TrackedRace race) {
        Map<String, Competitor> competitorsByName = new HashMap<String, Competitor>();
        for (Competitor competitor : race.getRace().getCompetitors()) {
            competitorsByName.put(competitor.getName(), competitor);
        }
        for (Iterator<Map.Entry<String, Integer>> carryEntryIter = carriedPointsByCompetitorName.entrySet().iterator(); carryEntryIter.hasNext(); ) {
            Map.Entry<String, Integer> carryEntry = carryEntryIter.next();
            if (competitorsByName.containsKey(carryEntry.getKey())) {
                setCarriedPoints(competitorsByName.get(carryEntry.getKey()), carryEntry.getValue());
                carryEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, Map<RaceInLeaderboard, MaxPointsReason>>> maxPointsReasonsEntryIter =
                maxPointsReasonsByCompetitorName.entrySet().iterator(); maxPointsReasonsEntryIter.hasNext();) {
            java.util.Map.Entry<String, Map<RaceInLeaderboard, MaxPointsReason>> maxPointsReasonEntries = maxPointsReasonsEntryIter.next();
            if (competitorsByName.containsKey(maxPointsReasonEntries.getKey())) {
                for (Map.Entry<RaceInLeaderboard, MaxPointsReason> maxPointsReasonEntry : maxPointsReasonEntries.getValue().entrySet()) {
                    getScoreCorrection().setMaxPointsReason(competitorsByName.get(maxPointsReasonEntry.getKey()),
                            maxPointsReasonEntry.getKey(), maxPointsReasonEntry.getValue());
                }
                maxPointsReasonsEntryIter.remove();
            }
        }
        for (Iterator<java.util.Map.Entry<String, Map<RaceInLeaderboard, Integer>>> correctedScoresEntryIter =
                correctedScoresByCompetitorName.entrySet().iterator(); correctedScoresEntryIter.hasNext();) {
            java.util.Map.Entry<String, Map<RaceInLeaderboard, Integer>> correctedScoresEntries = correctedScoresEntryIter.next();
            if (competitorsByName.containsKey(correctedScoresEntries.getKey())) {
                for (java.util.Map.Entry<RaceInLeaderboard, Integer> correctedScoreEntry : correctedScoresEntries.getValue().entrySet()) {
                    getScoreCorrection().correctScore(competitorsByName.get(correctedScoreEntry.getKey()),
                            correctedScoreEntry.getKey(), correctedScoreEntry.getValue());
                }
                correctedScoresEntryIter.remove();
            }
        }
    }

}
