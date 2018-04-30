package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface CompetitorSelectionListener {
    void didClickOKWithSelectedCompetitor(CompetitorDTO competitor);
}
