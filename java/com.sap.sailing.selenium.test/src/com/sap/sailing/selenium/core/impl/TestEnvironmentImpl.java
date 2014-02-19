package com.sap.sailing.selenium.core.impl;

import java.net.URL;

import org.openqa.selenium.WebDriver;

import com.sap.sailing.selenium.core.TestEnvironment;
import com.sap.sailing.selenium.core.WindowManager;

/**
 * <p>Default implementation of the test environment.</p>
 * 
 * @author
 *   D049941
 */
public class TestEnvironmentImpl implements TestEnvironment {
    private WebDriver driver;
    private WindowManager manager;
    private String root;
    private URL screenshots;
    
    /**
     * <p>Creates a new description of the test environment in which a test is executed.</p>
     * 
     * @param driver
     *   The web driver used for the execution of the test.
     * @param root
     *   The context root (base URL) against the tests should be executed.
     * @param screenshots
     *   The folder where screenshots should be stored.
     */
    public TestEnvironmentImpl(WebDriver driver, String root, URL screenshots) {
        this.driver = driver;
        this.root = root;
        this.screenshots = screenshots;
        
        this.manager = new WindowManager(driver);
    }
    
    @Override
    public WebDriver getWebDriver() {
        return this.driver;
    }

    @Override
    public WindowManager getWindowManager() {
        return this.manager;
    }

    @Override
    public String getContextRoot() {
        return this.root;
    }
    
    @Override
    public URL getScreenshotFolder() {
        return this.screenshots;
    }

    /**
     * <p>Closes the test environment. This quits the web driver, closing every associated window.</p>
     */
    public void close() {
        this.driver.quit();
        this.driver = null;
        this.root = null;
        this.screenshots = null;
    }
}
