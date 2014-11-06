package com.sap.sailing.dashboards.gwt.client.startanalysis.rankingtable;

import com.google.gwt.user.cellview.client.CellTable;

public interface StartAnalysisStartRankTableStyleResource extends CellTable.Resources {
    @Source({ CellTable.Style.DEFAULT_CSS, "StartAnalysisStartRankTableStyle.css"})
    
    TableStyle cellTableStyle();

    interface TableStyle extends CellTable.Style {
    }
}
