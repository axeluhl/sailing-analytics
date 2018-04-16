package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public interface CompetitorTableRowSelectionListener {

    public void didSelectedRowWithCompetitorName(CompetitorWithBoatDTO competitor);
}
