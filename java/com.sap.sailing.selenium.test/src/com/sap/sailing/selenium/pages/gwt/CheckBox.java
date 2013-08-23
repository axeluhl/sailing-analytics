package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.By.ByTagName;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.core.FindBy;

import com.sap.sailing.selenium.pages.PageArea;

public class CheckBox extends PageArea {
    @FindBy(how = ByTagName.class, using = "input")
    private WebElement checkbox;

    @FindBy(how = ByTagName.class, using = "label")
    private WebElement label;
    
    public CheckBox(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @Override
    protected void verify() {
        WebElement element = (WebElement) this.context;
        
        if(!element.getTagName().equals("span") || !element.getAttribute("class").equals("gwt-CheckBox"))
            throw new IllegalArgumentException("WebElement does not represent a CheckBox");
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
