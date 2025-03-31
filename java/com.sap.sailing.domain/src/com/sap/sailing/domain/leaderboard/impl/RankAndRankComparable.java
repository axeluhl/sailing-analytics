package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.leaderboard.RankComparable;

/**
 * Combines a per-race rank with a {@link RankComparable} that allows for cross-race
 * comparison of competitors.
 */
public class RankAndRankComparable implements Comparable<RankAndRankComparable> {
    private final Integer rank; 
    private final RankComparable rankComparable;
    
    public RankAndRankComparable(Integer rank, RankComparable rankComparable) {
        this.rank = rank;
        this.rankComparable = rankComparable;
    }
    
    public Integer getRank() {
        return rank;
    }
    
    public RankComparable getRankComparable() {
        return rankComparable;
    }
    
    @Override
    public int compareTo(RankAndRankComparable o) {
        return rankComparable.compareTo(o.getRankComparable());
    } 

    @Override
    public String toString() {
        return "RankAndRankComparable [rank=" + rank + ", rankComparable=" + rankComparable + "]";
    }
}