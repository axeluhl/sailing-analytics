package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;

public interface CompetitorSelectionListener {
    
    public void didClickOKWithSelectedCompetitor(CompetitorWithoutBoatDTO competitor);
}
