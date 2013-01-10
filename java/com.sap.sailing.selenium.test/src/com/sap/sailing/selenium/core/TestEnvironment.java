package com.sap.sailing.selenium.core;

import java.net.URL;

import org.openqa.selenium.WebDriver;

public interface TestEnvironment {
    public WebDriver getWebDriver();
    
    public String getContextRoot();
    
    public URL getScreenshotFolder();
}
