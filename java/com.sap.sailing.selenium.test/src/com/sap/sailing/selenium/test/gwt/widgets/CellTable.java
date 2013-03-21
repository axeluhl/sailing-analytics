package com.sap.sailing.selenium.test.gwt.widgets;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.test.PageObject;

/**
 * <p></p>
 * 
 * @author
 *   D049941
 */
public class CellTable extends PageObject {
    /**
     * <p></p>
     * 
     * @param driver
     *   
     * @param element
     *   
     */
    public CellTable(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    @Override
    protected void verify() {
        // TODO: Verify that the context represents a GWT CellTable
    }
}
