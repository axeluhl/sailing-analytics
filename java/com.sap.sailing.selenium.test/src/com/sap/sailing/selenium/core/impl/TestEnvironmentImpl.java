package com.sap.sailing.selenium.core.impl;

import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.core.TestEnvironment;

public class TestEnvironmentImpl implements TestEnvironment {
    private WebDriver driver;
    private String root;
    private URL screenshots;
    
    public TestEnvironmentImpl(WebDriver driver, String root, URL screenshots) {
        this.driver = driver;
        this.root = root;
        this.screenshots = screenshots;
    }
    
    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @Override
    public String getContextRoot() {
        return this.root;
    }
    
    @Override
    public URL getScreenshotFolder() {
        return this.screenshots;
    }

    public void close() {
        this.driver.quit();
        this.driver = null;
        this.root = null;
        this.screenshots = null;
    }
}
