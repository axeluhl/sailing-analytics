package com.sap.sse.datamining.ui.client.resources;

import com.google.gwt.user.cellview.client.CellTable;

public interface DataMiningCellTableResources extends CellTable.Resources {

    @Source({ CellTable.Style.DEFAULT_CSS, "DataMiningCellTable.css" })
    DataMiningCellTableStyle cellTableStyle();

    interface DataMiningCellTableStyle extends CellTable.Style {
    }
}
