package com.sap.sailing.selenium.core;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class AjaxCallsComplete implements ExpectedCondition<Boolean> {
    @Override
    public Boolean apply(WebDriver driver) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        
        return (Boolean) executor.executeScript("return (window.PENDING_AJAX_CALLS.numberOfPendingCalls() === 0)");
    }
}
