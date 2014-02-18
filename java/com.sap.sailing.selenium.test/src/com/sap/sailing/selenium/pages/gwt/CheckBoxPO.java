package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.CSSHelper;

public class CheckBoxPO extends PageArea {
    protected static final String CHECKBOX_TAG_NAME = "span"; //$NON-NLS-1$
    
    private static final String CHECKBOX_CSS_CLASS = "gwt-CheckBox";
    
    @FindBy(how = ByTagName.class, using = "input")
    private WebElement checkbox;

    @FindBy(how = ByTagName.class, using = "label")
    private WebElement label;
    
    public CheckBoxPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @Override
    protected void verify() {
        WebElement element = (WebElement) this.context;
        String tagName = element.getTagName();
        
        if(!CHECKBOX_TAG_NAME.equalsIgnoreCase(tagName) || !CSSHelper.hasCSSClass(element, CHECKBOX_CSS_CLASS))
            throw new IllegalArgumentException("WebElement does not represent a CheckBox");
    }
    
    public boolean isEnabled() {
        return this.checkbox.isEnabled();
    }
    
    public String getLabel() {
        return this.label.getText();
    }
    
    public boolean isSelected() {
        return this.checkbox.isSelected();
    }
    
    public void setSelected(boolean selected) {
        if(selected != isSelected())
            this.checkbox.click();
    }
}
