package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class QuickRankDTO implements IsSerializable {
    public CompetitorDTO competitor;
    
    public int oneBasedRank;
    
    /**
     * The {@link #competitor}'s leg number, starting with 1 for the first leg
     */
    public int legNumberOneBased;

    public QuickRankDTO() {}
    
    public QuickRankDTO(CompetitorDTO competitorDTO, int oneBasedRank, int legNumberOneBased) {
        this.competitor = competitorDTO;
        this.oneBasedRank = oneBasedRank;
        this.legNumberOneBased = legNumberOneBased;
    }

    @Override
    public String toString() {
        return "QuickRankDTO [competitor=" + competitor + ", oneBasedRank=" + oneBasedRank + ", legNumberOneBased="
                + legNumberOneBased + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((competitor == null) ? 0 : competitor.getIdAsString().hashCode());
        result = prime * result + legNumberOneBased;
        result = prime * result + oneBasedRank;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        QuickRankDTO other = (QuickRankDTO) obj;
        if (competitor == null) {
            if (other.competitor != null)
                return false;
        } else if (!competitor.getIdAsString().equals(other.competitor.getIdAsString()))
            return false;
        if (legNumberOneBased != other.legNumberOneBased)
            return false;
        if (oneBasedRank != other.oneBasedRank)
            return false;
        return true;
    }
}
