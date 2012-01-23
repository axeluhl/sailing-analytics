package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QuickRankDTO implements IsSerializable {
    public CompetitorDTO competitor;
    public int rank;
    public int legNumber;

    public QuickRankDTO() {}
    
    public QuickRankDTO(CompetitorDTO competitorDTO, int rank, int legNumber) {
        this.competitor = competitorDTO;
        this.rank = rank;
        this.legNumber = legNumber;
    }
}
