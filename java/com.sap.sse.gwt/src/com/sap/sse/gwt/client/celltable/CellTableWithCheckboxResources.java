package com.sap.sse.gwt.client.celltable;

import com.google.gwt.user.cellview.client.CellTable;

public interface CellTableWithCheckboxResources extends CellTable.Resources {
    @Override
    @Source({CellTable.Style.DEFAULT_CSS, "com/sap/sse/gwt/client/celltable/CellTableWithCheckboxResources.css"})
    CellTableWithCheckboxStyle cellTableStyle();
}
