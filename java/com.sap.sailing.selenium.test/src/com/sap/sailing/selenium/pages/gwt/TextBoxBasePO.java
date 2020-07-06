package com.sap.sailing.selenium.pages.gwt;

import java.util.function.BooleanSupplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Objects;
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
     * Wait until text equals to expected one.
     * @param expected expected text.
     */
    public void waitForElementUntil(String expected) {
        waitUntil(new BooleanSupplier() {
            @Override
            public boolean getAsBoolean() {
                try {
                    return Objects.equal(getText(), expected);
                } catch (Exception e) {
                    return false;
                }
            }
        });
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

}
