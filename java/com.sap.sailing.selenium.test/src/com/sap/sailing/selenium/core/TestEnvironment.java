package com.sap.sailing.selenium.core;

import org.openqa.selenium.WebDriver;

public interface TestEnvironment {
    public WebDriver getWebDriver();
    
    public String getContextRoot();
}
