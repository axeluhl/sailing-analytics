package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * Abstract base {@link PageObject} implementation for GWT text inputs, providing access to the contained text.
 */
public abstract class TextBoxBasePO extends AbstractInputPO {

    /**
     * @see AbstractInputPO#AbstractInputPO(WebDriver, WebElement)
     */
    protected TextBoxBasePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    /**
     * Appends the given text to the underlying {@link WebElement}, while keeping possibly existing text.
     * 
     * @param text the text to append
     * 
     * @see WebElement#sendKeys(CharSequence...)
     */
    public void appendText(String text) {
        if (text != null) {
            getWebElement().sendKeys(text);
        }
    }
    
    /**
     * Sets the given text to the underlying {@link WebElement}, while clearing possibly existing text.
     * 
     * @param text the text to set
     * 
     * @see WebElement#clear()
     * @see WebElement#sendKeys(CharSequence...)
     */
    public void setText(String text) {
        getWebElement().clear();
        this.appendText(text);
    }
    
    /**
     * Gets the text contained in the underlying {@link WebElement}.
     * 
     * @return the contained text
     * 
     * @see WebElement#getText()
     */
    public String getText() {
        return getWebElement().getText();
    }
    
    /**
     * Gets the text contained in the underlying {@link WebElement}s "Value" attribute.
     * This might be necessary for input elements that do not maintain their contained text in the "innerText" attribute, that is returned by #WebElement#getText()
     * @return the contained text
     * 
     */
    public String getValue() {
        return getWebElement().getAttribute("Value");
    }

}
