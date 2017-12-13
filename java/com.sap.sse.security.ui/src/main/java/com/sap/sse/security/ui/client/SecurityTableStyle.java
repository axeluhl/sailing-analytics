package com.sap.sse.security.ui.client;

import com.google.gwt.user.cellview.client.CellTable;

public interface SecurityTableStyle extends CellTable.Style {
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
