package com.sap.sailing.selenium.pages.common;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.sap.sailing.selenium.pages.PageArea;

public class LabelTypePO extends PageArea {
    
    private static final String LABEL_TYPE_LIVE = "live";
    private static final String LABEL_TYPE_FINISHED = "finished";
    private static final String LABEL_TYPE_INPROGRESS = "proress";
    private static final String LABEL_TYPE_UPCOMING = "upcoming";
    
    public LabelTypePO(WebDriver driver, WebElement element) {
        super(driver, element);
    }
    
    public boolean isLive() {
        return hasType(LABEL_TYPE_LIVE);
    }
    
    public boolean isFinished() {
        return hasType(LABEL_TYPE_FINISHED);
    }
    
    public boolean isInProgress() {
        return hasType(LABEL_TYPE_INPROGRESS);
    }
    
    public boolean isUpcoming() {
        return hasType(LABEL_TYPE_UPCOMING);
    }
    
    private boolean hasType(String expectedType) {
        String actualType = getWebElement().getAttribute("data-labeltype");
        return expectedType.equalsIgnoreCase(actualType);
    }
    

}
