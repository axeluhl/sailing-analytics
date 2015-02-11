package com.sap.sailing.dashboards.gwt.client.popups.competitorselection;

import com.google.gwt.user.cellview.client.CellTable;

public interface CompetitorTableStyleResource extends CellTable.Resources {
    @Source({ CellTable.Style.DEFAULT_CSS, "CompetitorTableStyle.css"})
    
    TableStyle cellTableStyle();

    interface TableStyle extends CellTable.Style {
    }
}
