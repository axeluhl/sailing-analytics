package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.FindBy;
import com.sap.sailing.selenium.pages.PageObject;

/**
 * {@link PageObject} implementation for GWT check boxes.
 */
public class CheckBoxPO extends AbstractInputPO {
    
    private static final String TAG_NAME = "span";
    public static final String CSS_CLASS = "gwt-CheckBox";
    
    @FindBy(how = ByTagName.class, using = "input")
    private WebElement input;

    @FindBy(how = ByTagName.class, using = "label")
    private WebElement label;
    
    /**
     * Factory method to create a {@link CheckBoxPO}.
     * 
     * @param driver the web driver to use
     * @param element the element representing the check box on the page
     * @return a new {@link CheckBoxPO} instance
     */
    public static CheckBoxPO create(WebDriver driver, WebElement element) {
        return new CheckBoxPO(driver, element);
    }
    
    /**
     * @see AbstractInputPO#AbstractInputPO(WebDriver, WebElement)
     */
    public CheckBoxPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    /**
     * Get the text of the check boxes label.
     * 
     * @return the label text
     */
    public String getLabel() {
        return this.label.getText();
    }
    
    /**
     * Determines whether or not the underlying {@link WebElement} is selected.
     * 
     * @return <code>true</code> if the {@link WebElement} is enabled, <code>false</code> otherwise
     * 
     * @see WebElement#isSelected()
     */
    public boolean isSelected() {
        return this.input.isSelected();
    }
    
    /**
     * Changes the {@link WebElement}s selection by clicking it, if desired and current state don't match. 
     * 
     * @param selected the desired selection state
     */
    public void setSelected(boolean selected) {
        if(selected != isSelected()) {
            this.input.click();
        }
    }
    
    @Override
    public boolean isEnabled() {
        return this.input.isEnabled();
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
