package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.NumberOfCompetitorsInLeaderboardFetcher;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.util.graph.DirectedEdge;
import com.sap.sse.util.graph.DirectedGraph;
import com.sap.sse.util.topologicalordering.TopologicalComparator;

public class HighPointFirstGets1LastBreaksTie extends HighPointFirstGetsFixedScore {
    private static final long serialVersionUID = 1L;

    public HighPointFirstGets1LastBreaksTie() {
        super(/* score for race winner */ 1.0, /* minimum score from rank */ 0.0);
    }

    @Override
    public ScoringSchemeType getType() {
        return ScoringSchemeType.HIGH_POINT_FIRST_GETS_ONE;
    }

    @Override
    public Double getPenaltyScore(RaceColumn raceColumn, Competitor competitor, MaxPointsReason maxPointsReason, Integer numberOfCompetitorsInRace,
            NumberOfCompetitorsInLeaderboardFetcher numberOfCompetitorsInLeaderboardFetcher, TimePoint timePoint, Leaderboard leaderboard) {
        return -1.0;
    }

    @Override
    public int compareByBetterScore(Competitor o1, List<Util.Pair<RaceColumn, Double>> o1Scores, Competitor o2,
            List<Util.Pair<RaceColumn, Double>> o2Scores, boolean nullScoresAreBetter, TimePoint timePoint, Leaderboard leaderboard) {
        // Construct a graph in which all competitors with equal points are represented as nodes;
        // edges represent a RaceColumn/Fleet where the "from" node in the edge has won over the "to"
        // node in the edge. This graph will be sorted such that competitors precede those over which
        // they won, except if there is a cycle in the graph in which case competitors on the cycle
        // will be ranked equal by this method, leaving the decision to other criteria
        final Double o1NetPoints = leaderboard.getNetPoints(o1, timePoint);
        final Double o2NetPoints = leaderboard.getNetPoints(o2, timePoint);
        assert Math.abs(o1NetPoints-o2NetPoints) < 0.00001;
        final Set<Competitor> nodesInGraph = new HashSet<>();
        nodesInGraph.add(o1);
        nodesInGraph.add(o2);
        for (final Competitor c : leaderboard.getCompetitors()) {
            if (leaderboard.getNetPoints(c, timePoint).equals(o1NetPoints)) {
                nodesInGraph.add(c);
            }
        }
        final Set<DirectedEdge<Competitor>> edges = constructEdges(nodesInGraph, leaderboard, timePoint, nullScoresAreBetter);
        final DirectedGraph<Competitor> graph = DirectedGraph.create(nodesInGraph, edges);
        final TopologicalComparator<Competitor> comparator = new TopologicalComparator<>(graph);
        return comparator.compare(o1, o2);
    }

    /**
     * Finds all races ({@link RaceColumn}/{@link Fleet} combinations) where more than one competitor from
     * {@code nodesInGraph} competed, and adds an edge to the result for each pair of competitors in that same
     * race, leading from the better to the worse competitor.
     */
    private Set<DirectedEdge<Competitor>> constructEdges(Set<Competitor> nodesInGraph, Leaderboard leaderboard, TimePoint timePoint,
            boolean nullScoresAreBetter) {
        final Comparator<Double> pureScoreComparator = getScoreComparator(nullScoresAreBetter);
        final Set<DirectedEdge<Competitor>> edges = new HashSet<>();
        for (final RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for (final Fleet fleet : raceColumn.getFleets()) {
                final Iterable<Competitor> competitorsInRace = leaderboard.getCompetitors(raceColumn, fleet);
                final Set<Competitor> equalRankedCompetitorsInRace = new HashSet<>();
                Util.addAll(competitorsInRace, equalRankedCompetitorsInRace);
                equalRankedCompetitorsInRace.retainAll(nodesInGraph);
                final List<Competitor> competitorsInRaceOrderedByScoreInRace = new ArrayList<>(equalRankedCompetitorsInRace);
                Collections.sort(competitorsInRaceOrderedByScoreInRace, (c1, c2) -> {
                    return pureScoreComparator.compare(leaderboard.getTotalPoints(c1, raceColumn, timePoint),
                            leaderboard.getTotalPoints(c2, raceColumn, timePoint));
                });
                for (int i=0; i<competitorsInRaceOrderedByScoreInRace.size(); i++) {
                    for (int j=i+1; j<competitorsInRaceOrderedByScoreInRace.size(); j++) {
                        edges.add(DirectedEdge.create(competitorsInRaceOrderedByScoreInRace.get(i), competitorsInRaceOrderedByScoreInRace.get(j)));
                    }
                }
            }
        }
        return edges;
    }

    @Override
    public int compareByLastRace(List<Pair<RaceColumn, Double>> o1ScoresIncludingDiscarded,
            List<Pair<RaceColumn, Double>> o2ScoresIncludingDiscarded, boolean nullScoresAreBetter, Competitor o1,
            Competitor o2) {
        return 0; // TODO should this consider the graph, its cycles, and rank by last race if an only if o1/o2 are the only elements in a cycle group?
    }
}
