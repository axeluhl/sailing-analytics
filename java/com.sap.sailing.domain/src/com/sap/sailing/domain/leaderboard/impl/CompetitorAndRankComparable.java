package com.sap.sailing.domain.leaderboard.impl;

import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.leaderboard.RankComparable;

/**
 * A {@link Competitor} and a corresponding {@link RankComparable} for that competitor.
 * The {@link Comparable} implementation is based on the comparison of the {@link RankComparable}'s
 * "natural order."
 */
public class CompetitorAndRankComparable implements Comparable<CompetitorAndRankComparable> {
    private final RankComparable rankComparable; 
    private final Competitor competitor;
    
    public CompetitorAndRankComparable(Competitor competitor, RankComparable rankComparable) {
        this.rankComparable = rankComparable;
        this.competitor = competitor;
    }
    
    public CompetitorAndRankComparable(Map.Entry<Competitor, RankComparable> entry) {
        this.competitor = entry.getKey();
        this.rankComparable = entry.getValue();
    }
    
    public RankComparable getRankComparable() {
        return rankComparable;
    }
    
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public int compareTo(CompetitorAndRankComparable o) {
        return rankComparable.compareTo(o.getRankComparable());
    } 
    
}
