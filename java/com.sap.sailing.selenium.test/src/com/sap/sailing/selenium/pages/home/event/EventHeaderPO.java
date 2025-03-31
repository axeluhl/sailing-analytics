package com.sap.sailing.selenium.pages.home.event;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;
import com.sap.sailing.selenium.pages.common.LabelTypePO;

public class EventHeaderPO extends PageArea {
    
    public EventHeaderPO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public LabelTypePO getEventStateLabel() {
        return getChildPO(LabelTypePO::new, "EventStateLabelDiv");
    }
    
}
