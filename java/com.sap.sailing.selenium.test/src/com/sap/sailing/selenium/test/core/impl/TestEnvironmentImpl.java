package com.sap.sailing.selenium.test.core.impl;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.test.core.TestEnvironment;

public class TestEnvironmentImpl implements TestEnvironment {
    private WebDriver driver;
    private String root;
    
    public TestEnvironmentImpl(WebDriver driver, String root) {
        this.driver = driver;
        this.root = root;
    }
    
    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @Override
    public String getContextRoot() {
        return this.root;
    }

    public void close() {
        this.driver.quit();
        this.driver = null;
        this.root = null;
    }
}
