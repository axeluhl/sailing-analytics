package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds the same data as a {@link QuickRanksDTO} object, only that the {@link CompetitorDTO} is replaced by the
 * string representation of the competitor's ID.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompactQuickRankDTO implements IsSerializable {
    private final String competitorIdAsString;
    private final int rank;
    private final int legNumber;
    
    public CompactQuickRankDTO(String competitorIdAsString, int rank, int legNumber) {
        super();
        this.competitorIdAsString = competitorIdAsString;
        this.rank = rank;
        this.legNumber = legNumber;
    }

    public String getCompetitorIdAsString() {
        return competitorIdAsString;
    }

    public int getRank() {
        return rank;
    }

    public int getLegNumber() {
        return legNumber;
    }
}
