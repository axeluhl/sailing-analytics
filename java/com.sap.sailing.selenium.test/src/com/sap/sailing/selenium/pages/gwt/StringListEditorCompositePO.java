package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageArea;

public class StringListEditorCompositePO extends PageArea {
    
    public static class ValueEntryPO extends DataEntryPO {

        @FindBy(how = BySeleniumId.class, using = "RemoveButton")
        private WebElement deleteButton;
        @FindBy(how = BySeleniumId.class, using = "ValueTextBox")
        private WebElement valueTextBox;
        

        public ValueEntryPO(CellTablePO<?> table, WebElement element) {
            super(table, element);
        }
        
        public void deleteValueEntry() {
            deleteButton.click();
        }
        
        public String getValueEntryName(){
            TextBoxPO textBoxPO = new TextBoxPO(this.driver, valueTextBox);
            String text = textBoxPO.getValue();
            return text;
        }
    }
    
    @FindBy(how = BySeleniumId.class, using = "AddButton")
    private WebElement addValueButton;
    @FindBy(how = BySeleniumId.class, using = "InputSuggestBox")
    private WebElement valueInput;
    @FindBy(how = BySeleniumId.class, using = "ExpandedValuesGrid")
    private WebElement valueGrid;
    
    private StringListEditorCompositePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    private CellTablePO<ValueEntryPO> getExpandedValuesGrid() {
        return new GenericCellTablePO<>(this.driver, this.valueGrid, ValueEntryPO.class);
    }

    public ValueEntryPO findValue(final String valueToFind) {
        final CellTablePO<ValueEntryPO> table = getExpandedValuesGrid();
        for (ValueEntryPO entry : table.getEntries()) {
            final String name = entry.getValueEntryName();
            if (name != null && name.equals(valueToFind)) {
                return entry;
            }
        }
        return null;
    }
    
    public static StringListEditorCompositePO create(WebDriver driver, WebElement permissionsInput) {
        return new StringListEditorCompositePO(driver, permissionsInput);
    }
    
    public void addNewValue(String value) {
        SuggestBoxPO.create(driver, valueInput).appendText(value);
        addValueButton.click();
    }
    
    public void removeValueByName(String name) {
        ValueEntryPO findValue = findValue(name);
        if (findValue != null) {
            findValue.deleteValueEntry();
        } else {
            throw new IllegalStateException("Expected value '" + name + "' not found");
        }
    }
}
