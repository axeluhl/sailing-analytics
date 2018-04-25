package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

public class CompetitorAndBoatDTO implements Serializable {
    private static final long serialVersionUID = -6380330790682688885L;
    private CompetitorDTO competitor;
    private BoatDTO boat;

    @Deprecated
    CompetitorAndBoatDTO() {} // for GWT serialization only
    
    public CompetitorAndBoatDTO(CompetitorDTO competitor, BoatDTO boat) {
        super();
        this.competitor = competitor;
        this.boat = boat;
    }

    public CompetitorDTO getCompetitor() {
        return competitor;
    }

    public BoatDTO getBoat() {
        return boat;
    }
}
