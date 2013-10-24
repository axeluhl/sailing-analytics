package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;

public abstract class CellTableRow extends PageArea {
    protected static final String ROW_TAG_NAME = "tr"; //$NON-NLS-1$
    
    protected final CellTable2<?> table;
    
    public CellTableRow(CellTable2<?> table, WebElement element) {
        super(table.getWebDriver(), element);
        
        this.table = table;
    }
    
    /**
     * <p>NOTE: This constructor is only used for creating an alias!</p>
     */
    protected CellTableRow() {
        super(null, null);
        
        this.table = null;
    }
    
    @Override
    protected void verify() {
        WebElement element = (WebElement) this.context;
        
        if(!ROW_TAG_NAME.equalsIgnoreCase(element.getTagName()))
            throw new IllegalArgumentException("WebElement does not represent a Row"); //$NON-NLS-1$
    }

}
