package com.sap.sailing.selenium.core;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;

/**
 * <p></p>
 * 
 * @author
 *   Riccardo Nimser (D049941)
 */
public class WindowManager {
    private final WebDriver driver;

    private final Supplier<WebDriver> webDriverFactory;
    
    /**
     * <p></p>
     * 
     * @param driver
     *   
     */
    public WindowManager(WebDriver driver, Supplier<WebDriver> webDriverFactory) {
        this.driver = driver;
        this.webDriverFactory = webDriverFactory;
        
        setWindowMaximized(this.driver);
    }
    
    public void withExtraWindow(BiConsumer<WebDriverWindow, WebDriverWindow> defaultAndExtraWindow) {
        final WebDriverWindow defaultWindow = new WebDriverWindow(this.driver, this.driver.getWindowHandle());
        final WebDriver extraDriver = webDriverFactory.get();
        final WebDriverWindow extraWindow = new WebDriverWindow(extraDriver, extraDriver.getWindowHandle());
        
        extraWindow.switchToWindow();
        setWindowMaximized(extraDriver);
        defaultWindow.switchToWindow();
        try {
            defaultAndExtraWindow.accept(defaultWindow, extraWindow);
        } finally {
            try {
                extraDriver.quit();
            } catch (Exception e) {
                // This call may fail depending on the WebDriver being used
            }
            defaultWindow.switchToWindow();
        }
    }
    
    private void setWindowMaximized(WebDriver driver) {
        try {
            driver.manage().window().maximize();
        } catch (Exception e) {
            // Depending on the combination of OS and WebDriver implementation this may fail
            // e.g. chrome with xvfb can't do this successfully.
            try {
                // Trying to set a proper screen size as fallback that should usable with all modern screens
                driver.manage().window().setSize(new Dimension(1440, 900));
            } catch (Exception exc) {
                // In this case we just can't change the window
            }
        }
    }
}
