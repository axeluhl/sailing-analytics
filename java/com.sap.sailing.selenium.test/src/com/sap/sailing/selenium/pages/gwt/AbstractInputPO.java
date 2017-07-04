package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.PageObject;
import com.sap.sailing.selenium.pages.common.CSSHelper;

/**
 * Abstract super class for {@link PageObject}s representing a (GWT) UI element, which verifies the provided
 * {@link WebElement} by the tag name and CSS class name defined in implementing subclasses.
 */
public abstract class AbstractInputPO extends PageArea {

    /**
     * @see PageArea#PageArea(WebDriver, WebElement)
     */
    protected AbstractInputPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    /**
     * Verifies that the underlying {@link WebElement} is represented by the defined {@link #getTagName() tag name} and
     * has (at least) the defined {@link #getCssClassName() CSS class name}.
     * 
     * @throws IllegalArgumentException
     *             if the {@link WebElement}s tag name does not match the defined tag name or if the it doesn't have the
     *             defined CSS class name
     * 
     * @see #getTagName()
     * @see #getClass()
     */
    @Override
    protected void verify() {
        WebElement element = getWebElement();
        String tagName = element.getTagName();
        if(!getTagName().equalsIgnoreCase(tagName) || !CSSHelper.hasCSSClass(element, getCssClassName())) {
            throw new IllegalArgumentException("WebElement does not represent a " + getCssClassName());
        }
    }
    
    /**
     * Determines whether or not the underlying {@link WebElement} is enabled.
     * 
     * @return <code>true</code> if the {@link WebElement} is enabled, <code>false</code> otherwise
     * 
     * @see WebElement#isEnabled()
     */
    public boolean isEnabled() {
        return getWebElement().isEnabled();
    }
    
    /**
     * Provide a tag name used for verification.
     * 
     * @return the elements tag name
     * 
     * @see #verify()
     */
    protected abstract String getTagName();
    
    /**
     * Provide a CSS class name used for verification.
     * 
     * @return the elements (primary) CSS class name
     * 
     * @see #verify()
     */
    protected abstract String getCssClassName();

}
