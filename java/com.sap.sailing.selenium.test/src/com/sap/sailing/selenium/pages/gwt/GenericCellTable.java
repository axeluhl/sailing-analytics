package com.sap.sailing.selenium.pages.gwt;

import java.lang.reflect.Constructor;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class GenericCellTable<T extends DataEntry> extends CellTable2<T> {
    public static interface DataEntryFactory<T extends DataEntry> {
        public <S extends CellTable2<T>> T createEntry(S table, WebElement element);
    }
    
    /**
     * <p></p>
     * 
     * @param <T>
     *   The type of the data entries the factory creates.
     */
    public static class DefaultDataEntryFactory<T extends DataEntry> implements DataEntryFactory<T> {
        private Class<T> type;
        
        public DefaultDataEntryFactory(Class<T> type) {
            this.type = type;
        }
        
        @Override
        public <S extends CellTable2<T>> T createEntry(S table, WebElement element) {
            try {
                Constructor<T> constructor = this.type.getConstructor(this.getClass(), WebElement.class);
                
                return constructor.newInstance(this, element);
            } catch (Exception exception) {
                throw new RuntimeException("Can't create DataEntry of type " + this.type, exception);
            }
        }
        
    }
    private DataEntryFactory<T> factory;
    
    public GenericCellTable(WebDriver driver, WebElement element, Class<T> entryType) {
        this(driver, element, new DefaultDataEntryFactory<>(entryType));
    }
    
    public GenericCellTable(WebDriver driver, WebElement element, DataEntryFactory<T> factory) {
        super(driver, element);
        
        this.factory = factory;
    }
    
    @Override
    protected T createDataEntry(WebElement element) {
        return this.factory.createEntry(this, element);
    }
}
