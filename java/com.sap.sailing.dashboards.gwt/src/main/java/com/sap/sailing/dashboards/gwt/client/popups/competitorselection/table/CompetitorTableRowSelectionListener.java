package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface CompetitorTableRowSelectionListener {
    void didSelectedRowWithCompetitorName(CompetitorDTO competitor);
}
