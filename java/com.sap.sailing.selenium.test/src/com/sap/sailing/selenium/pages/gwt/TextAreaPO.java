package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for GWT text areas.
 */
public class TextAreaPO extends TextBoxBasePO {

    private static final String TAG_NAME = "textarea";
    private static final String CSS_CLASS = "gwt-TextArea";
    
    /**
     * Factory method to create a {@link TextAreaPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the text area on the page
     * @return a new {@link TextAreaPO} instance
     */
    public static TextAreaPO create(WebDriver driver, WebElement element) {
        return new TextAreaPO(driver, element);
    }
    
    /**
     * @see TextBoxBasePO#TextBoxBasePO(WebDriver, WebElement)
     */
    protected TextAreaPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    @Override
    protected String getTagName() {
        return TAG_NAME;
    }
    
    @Override
    protected String getCssClassName() {
        return CSS_CLASS;
    }

}
