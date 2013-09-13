package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebElement;

public class CellTableHeader extends CellTableRow {
    protected static final String HEADER_TAG_NAME = "th"; //$NON-NLS-1$
    
    public CellTableHeader(CellTable2 table, WebElement element) {
        super(table, element);
    }

}
