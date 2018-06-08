package com.sap.sse.datamining.ui.client.selection.filter;

import com.google.gwt.user.cellview.client.CellTable;

public interface FilterTableResources extends CellTable.Resources {

    @Source({ CellTable.Style.DEFAULT_CSS, "FilterTable.css" })
    FilterTableStyle cellTableStyle();

    interface FilterTableStyle extends CellTable.Style {
    }
}
