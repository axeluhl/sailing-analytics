package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for better date time boxes.
 */
public class BetterDateTimeBoxPO extends TextBoxPO {
    
    /**
     * Factory method to create a {@link BetterDateTimeBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the date time box on the page
     * @return a new {@link BetterDateTimeBoxPO} instance
     */
    public static BetterDateTimeBoxPO create(WebDriver driver, WebElement element) {
        return new BetterDateTimeBoxPO(driver, element);
    }
    
    /**
     * @see TextBoxPO#TextBoxPO(WebDriver, WebElement)
     */
    protected BetterDateTimeBoxPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    @Override
    public void appendText(String text) {
        super.appendText(text);
        super.appendText("\t");
    }
    
}
