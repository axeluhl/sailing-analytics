package com.sap.sailing.selenium.pages.highcharts;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.CSSHelper;

public class Chart extends PageArea {
    private static final String TAG_NAME = "div";
    private static final String CSS_CLASS = "highcharts-container";
    
    public Chart(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    @Override
    protected void verify() {
        WebElement element = getWebElement();
        
        if(!TAG_NAME.equals(element.getTagName()) || !CSSHelper.hasCSSClass(element, CSS_CLASS)) {
            throw new IllegalArgumentException("The element " + element + " does not represent a Highchart");
        }
    }
}
