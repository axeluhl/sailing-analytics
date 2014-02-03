package com.sap.sailing.selenium.pages.gwt;

import java.lang.reflect.Constructor;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * <p></p>
 * 
 * @author
 *   D049941
 * @param <T>
 *   The type of the data entries the table contains.
 */
public class GenericCellTablePO<T extends DataEntryPO> extends CellTablePO<T> {
    /**
     * <p></p>
     * 
     * @author
     *   D049941
     * @param <T>
     *   The type of the data entries the factory creates.
     */
    public static interface DataEntryFactory<T extends DataEntryPO> {
        /**
         * <p>Creates and returns a new entry which represents the specified element (a row) of the given table.</p>
         * 
         * @param table
         *   The table containing the entry.
         * @param element
         *   The underlying element represented by the entry to create.
         * @return
         *   The data entry representing the element.
         */
        public <S extends CellTablePO<T>> T createEntry(S table, WebElement element);
    }
    
    /**
     * <p>Default implementation of the interface DataEntryFactory which use reflection to create the data entries.</p>
     * 
     * @author
     *   D049941
     * @param <T>
     *   The type of the data entries the factory creates.
     */
    public static class DefaultDataEntryFactory<T extends DataEntryPO> implements DataEntryFactory<T> {
        private Class<T> type;
        
        /**
         * <p>Creates a new factory for the specified type of data entries.</p>
         * 
         * @param type
         *   The class object of the entries to create.
         */
        public DefaultDataEntryFactory(Class<T> type) {
            this.type = type;
        }
        
        /**
         * TODO [D049941]
         * 
         * To be able to create entries of a specified type, the type must have a public constructor with two arguments
         * with the formal parameter types CellTable and WebElement.</p>
         */
        @Override
        public <S extends CellTablePO<T>> T createEntry(S table, WebElement element) {
        	Class<?> clazz = table.getClass();
        	
        	while(clazz != null) {
        		try {
        			Constructor<T> constructor = this.type.getConstructor(clazz, WebElement.class);
        			
                    return constructor.newInstance(table, element);
        		} catch (Exception exception) {
        			clazz = clazz.getSuperclass();
        		}
        	}
        	
        	throw new RuntimeException("Can't create DataEntry of type " + this.type);
        }
    }
    
    private DataEntryFactory<T> factory;
    
    public GenericCellTablePO(WebDriver driver, WebElement element, Class<T> entryType) {
        this(driver, element, new DefaultDataEntryFactory<>(entryType));
    }
    
    public GenericCellTablePO(WebDriver driver, WebElement element, DataEntryFactory<T> factory) {
        super(driver, element);
        
        this.factory = factory;
    }
    
    @Override
    protected T createDataEntry(WebElement element) {
        return this.factory.createEntry(this, element);
    }
}
