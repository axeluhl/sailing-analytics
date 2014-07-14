package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class QuickRankDTO implements IsSerializable {
    public CompetitorDTO competitor;
    public int rank;
    
    /**
     * The {@link #competitor}'s leg number, starting with 1 for the first leg
     */
    public int legNumberOneBased;

    public QuickRankDTO() {}
    
    public QuickRankDTO(CompetitorDTO competitorDTO, int rank, int legNumberOneBased) {
        this.competitor = competitorDTO;
        this.rank = rank;
        this.legNumberOneBased = legNumberOneBased;
    }
}
