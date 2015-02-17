package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface CompetitorTableRowSelectionListener {

    public void didSelectedRowWithCompetitorName(CompetitorDTO competitor);
}
