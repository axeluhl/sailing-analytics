package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QuickRankDAO implements IsSerializable {
    public CompetitorDAO competitor;
    public int rank;
    public int legNumber;

    public QuickRankDAO() {}
    
    public QuickRankDAO(CompetitorDAO competitorDAO, int rank, int legNumber) {
        this.competitor = competitorDAO;
        this.rank = rank;
        this.legNumber = legNumber;
    }
}
