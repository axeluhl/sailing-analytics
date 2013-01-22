package com.sap.sailing.selenium.test;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * <p>Base class for page objects representing an area of a page.</p>
 * 
 * @author
 *   D049941
 */
public class PageArea extends PageObject {
    /**
     * <p>Creates a new page object with the given driver representing an area which is the specified by the element
     *   and its children.</p>
     * 
     * @param driver
     *   The web driver to use.
     * @param element
     *   The element representing the area of the page.
     */
    public PageArea(WebDriver driver, WebElement element) {
        super(driver, element);
    }
}
