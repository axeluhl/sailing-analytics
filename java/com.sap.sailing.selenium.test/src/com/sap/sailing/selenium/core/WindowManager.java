package com.sap.sailing.selenium.core;

import java.util.Set;
import java.util.function.BiConsumer;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * <p></p>
 * 
 * @author
 *   Riccardo Nimser (D049941)
 */
public class WindowManager {
    private static final String JAVA_SCRIPT = "window.open('%s', '_blank')";
    
    private WebDriver driver;
    
    /**
     * <p></p>
     * 
     * @param driver
     *   
     */
    public WindowManager(WebDriver driver) {
        this.driver = driver;
        
        setWindowMaximized();
    }
    
    /**
     * <p>Returns a handle to the window witch is currently active.</p>
     * 
     * @return
     *   
     */
    private WebDriverWindow getCurrentWindow() {
        return new WebDriverWindow(this.driver, this.driver.getWindowHandle());
    }
    
    /**
     * <p></p>
     * 
     * @return
     */
    private WebDriverWindow openNewWindow() {
        return openNewWindow(false);
    }
    
    private WebDriverWindow openNewWindow(boolean focus) {
        return openNewWindow("", focus);
    }
    
    private WebDriverWindow openNewWindow(String url, boolean focus) {
        WebDriverWindow window = new WebDriverWindow(this.driver, createWindow(url));
        
        if(focus) {
            window.switchToWindow();
        }
        
        return window;
    }
    
    private String createWindow(String url) {
        // Record old handles
        Set<String> oldHandles = this.driver.getWindowHandles();
        
        executeScript(url != null ? url : "");
        
        return getNewHandle(oldHandles);
    }
    
    private void executeScript(String url) {
        JavascriptExecutor executor = (JavascriptExecutor) this.driver;
        executor.executeScript(String.format(JAVA_SCRIPT, url));
    }
    
    private String getNewHandle(Set<String> oldHandles) {
        Set<String> newHandles = this.driver.getWindowHandles();
        newHandles.removeAll(oldHandles);

        // Find the new window
        for(String handle : newHandles)
            return handle;

        return null;
    }
    
    public void withExtraWindow(BiConsumer<WebDriverWindow, WebDriverWindow> defaultAndExtraWindow) {
        final WebDriverWindow defaultWindow = getCurrentWindow();
        final WebDriverWindow extraWindow = openNewWindow();
        extraWindow.switchToWindow();
        setWindowMaximized();
        defaultWindow.switchToWindow();
        try {
            defaultAndExtraWindow.accept(defaultWindow, extraWindow);
        } finally {
            try {
                extraWindow.close();
            } catch (Exception e) {
                // This call may fail depending on the WebDriver being used
            }
            defaultWindow.switchToWindow();
        }
    }
    
    private void setWindowMaximized() {
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
