package com.sap.sse.gwt.client.celltable;

import com.google.gwt.user.cellview.client.CellTable;

public interface CellTableWithCheckboxStyle extends CellTable.Style {
    /**
     * Applied to checkbox column
     */
    String cellTableCheckboxColumnCell();

    /**
     * Applied to the checkbox div when the checkbox is selected
     */
    String cellTableCheckboxSelected();
    
    /**
     * Applied to the checkbox div when the checkbox is deselected
     */
    String cellTableCheckboxDeselected();
}
