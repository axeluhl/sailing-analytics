package com.sap.sailing.selenium.test;

import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.test.core.Managed;
import com.sap.sailing.selenium.test.core.Selenium;
import com.sap.sailing.selenium.test.core.TestEnvironment;

@RunWith(Selenium.class)
public abstract class AbstractSeleniumTest {
    @Managed
    protected TestEnvironment environment;
    
    protected String getContextRoot() {
        return this.environment.getContextRoot();
    }
    
    protected WebDriver getWebDriver() {
        return this.environment.getWebDriver();
    }
}
