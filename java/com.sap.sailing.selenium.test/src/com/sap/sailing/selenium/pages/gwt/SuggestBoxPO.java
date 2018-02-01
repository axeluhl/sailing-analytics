package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for GWT text boxes.
 */
public class SuggestBoxPO extends TextBoxBasePO {

    private static final String TAG_NAME = "input";
    private static final String CSS_CLASS = "gwt-SuggestBox";
    
    /**
     * Factory method to create a {@link SuggestBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the suggest box on the page
     * @return a new {@link SuggestBoxPO} instance
     */
    public static SuggestBoxPO create(WebDriver driver, WebElement element) {
        return new SuggestBoxPO(driver, element);
    }
    
    /**
     * @see TextBoxBasePO#TextBoxBasePO(WebDriver, WebElement)
     */
    protected SuggestBoxPO(WebDriver driver, WebElement element) {
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
