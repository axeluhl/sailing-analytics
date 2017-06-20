package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.table;

import com.sap.sailing.domain.common.dto.CompetitorWithoutBoatDTO;

public interface CompetitorTableRowSelectionListener {

    public void didSelectedRowWithCompetitorName(CompetitorWithoutBoatDTO competitor);
}
