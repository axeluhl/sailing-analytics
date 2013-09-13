package com.sap.sailing.selenium.pages.gwt;

import java.util.List;

import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.common.CSSHelper;

public class DataEntry extends CellTableRow {
    protected static final String CELL_TAG_NAME = "td"; //$NON-NLS-1$
    
    @FindBy(how = ByTagName.class, using = CELL_TAG_NAME)
    private List<WebElement> columns;
    
    public DataEntry(CellTable2<?> table, WebElement element) {
        super(table, element);
    }
    
    public String getColumnContent(int column) {
        return this.columns.get(column).getText();
    }
    
    public String getColumnContent(String name) {
        return getColumnContent(this.table.getColumnIndex(name));
    }
    
    public void select() {
        
    }
    
    public boolean isSelected() {
        return CSSHelper.hasCSSClass((WebElement) this.context, CellTable2.SELECTED_ROW_CSS_CLASS);
    }
    
    /**
     * <p>Returns an object which uniquely describes the data entry. The default implementation returns a string, which
     *   is a concatenation of all columns as obtained via getColumnContent(int column).</p>
     * 
     * @return
     *   An object which uniquely describes the data entry.
     */
    public Object getDescriptor() {
        StringBuilder builder = new StringBuilder();
        builder.append("DataEntry[");
        
        for(int i = 0; i < this.table.numberOfColumns(); i++) {
            builder.append(getColumnContent(i));
            builder.append(", ");
        }
        
        builder.delete(builder.length() - 2, builder.length());
        builder.append("]");
        
        return builder.toString();
    }
}
