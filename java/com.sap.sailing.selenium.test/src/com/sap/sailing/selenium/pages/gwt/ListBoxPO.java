package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for GWT list boxes.
 */
public class ListBoxPO extends AbstractInputPO {
    
    private static final String TAG_NAME = "select";
    private static final String CSS_CLASS = "gwt-ListBox";
    
    private final Select selectBox;
    
    /**
     * Factory method to create a {@link ListBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the list box on the page
     * @return a new {@link ListBoxPO} instance
     */
    public static ListBoxPO create(WebDriver driver, WebElement element) {
        return new ListBoxPO(driver, element);
    }
    
    /**
     * @see AbstractInputPO#AbstractInputPO(WebDriver, WebElement)
     */
    public ListBoxPO(WebDriver driver, WebElement element) {
        super(driver, element);
        selectBox = new Select(element);
    }
    
    public String getSelectedOptionLabel() {
        return this.selectBox.getFirstSelectedOption().getText();
    }
    
    public String getSelectedOptionValue() {
        return this.selectBox.getFirstSelectedOption().getAttribute("value");
    }
    
    public void selectOptionByLabel(String optionLabel) {
        this.selectBox.selectByVisibleText(optionLabel);
    }
    
    public void selectOptionByValue(String optionValue) {
        this.selectBox.selectByValue(optionValue);
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
