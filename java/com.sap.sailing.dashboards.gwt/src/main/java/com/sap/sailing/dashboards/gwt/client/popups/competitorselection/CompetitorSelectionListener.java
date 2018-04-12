package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public interface CompetitorSelectionListener {
    
    public void didClickOKWithSelectedCompetitor(CompetitorWithBoatDTO competitor);
}
