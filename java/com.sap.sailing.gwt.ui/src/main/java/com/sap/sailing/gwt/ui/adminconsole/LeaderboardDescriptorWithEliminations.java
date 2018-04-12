package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public class LeaderboardDescriptorWithEliminations extends LeaderboardDescriptor {
    private final Set<CompetitorWithBoatDTO> eliminatedCompetitors;
    
    public LeaderboardDescriptorWithEliminations(LeaderboardDescriptor base, Set<CompetitorWithBoatDTO> eliminatedCompetitors) {
        super(base.getName(), base.getDisplayName(), base.getScoringScheme(), base.getDiscardThresholds(), base.getRegattaName(), base.getCourseAreaId());
        this.eliminatedCompetitors = eliminatedCompetitors;
    }

    public Set<CompetitorWithBoatDTO> getEliminatedCompetitors() {
        return eliminatedCompetitors;
    }
}
