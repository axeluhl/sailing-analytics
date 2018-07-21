package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class LeaderboardDescriptorWithEliminations extends LeaderboardDescriptor {
    private final Set<CompetitorDTO> eliminatedCompetitors;
    
    public LeaderboardDescriptorWithEliminations(LeaderboardDescriptor base, Set<CompetitorDTO> eliminatedCompetitors) {
        super(base.getName(), base.getDisplayName(), base.getScoringScheme(), base.getDiscardThresholds(), base.getRegattaName(), base.getCourseAreaId());
        this.eliminatedCompetitors = eliminatedCompetitors;
    }

    public Set<CompetitorDTO> getEliminatedCompetitors() {
        return eliminatedCompetitors;
    }
}
