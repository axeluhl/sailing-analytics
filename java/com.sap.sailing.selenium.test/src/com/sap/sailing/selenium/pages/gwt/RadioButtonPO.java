package com.sap.sailing.selenium.pages.gwt;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class RadioButtonPO extends CheckBoxPO {
    private static final String RADIO_BUTTON_CSS_CLASS = "gwt-RadioButton";
    
    public RadioButtonPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    protected String getCssClassName() {
        return RADIO_BUTTON_CSS_CLASS;
    }
}
