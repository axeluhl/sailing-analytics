package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

/**
 * Holds the same data as a {@link QuickRanksDTO} object, only that the {@link CompetitorWithBoatDTO} is replaced by the
 * string representation of the competitor's ID.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactQuickRankDTO implements IsSerializable {
    private String competitorIdAsString;
    private int rank;
    private int legNumber;
    
    CompactQuickRankDTO() {}
    
    public CompactQuickRankDTO(String competitorIdAsString, int rank, int legNumber) {
        super();
        this.competitorIdAsString = competitorIdAsString;
        this.rank = rank;
        this.legNumber = legNumber;
    }

    public String getCompetitorIdAsString() {
        return competitorIdAsString;
    }

    public int getOneBasedRank() {
        return rank;
    }

    public int getLegNumber() {
        return legNumber;
    }
}
