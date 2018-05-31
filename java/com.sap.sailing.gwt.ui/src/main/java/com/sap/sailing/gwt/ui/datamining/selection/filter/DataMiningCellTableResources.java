package com.sap.sailing.gwt.ui.datamining.selection.filter;

import com.google.gwt.user.cellview.client.CellTable;

public interface DataMiningCellTableResources extends CellTable.Resources {

    @Source({ CellTable.Style.DEFAULT_CSS, "DataMiningCellTable.css" })
    DataMiningCellTableStyle cellTableStyle();

    interface DataMiningCellTableStyle extends CellTable.Style {
    }
}
