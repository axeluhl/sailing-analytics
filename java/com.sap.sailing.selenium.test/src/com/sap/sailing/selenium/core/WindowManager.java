package com.sap.sailing.selenium.core;

import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;

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
    }
    
    /**
     * <p></p>
     * 
     * @param url
     */
    public void switchTo(String url) {
        WebDriverWindow window = findWindow(url);
        
        if(window == null)
            throw new RuntimeException("Window not found");
        
        window.switchToWindow();
    }
    
    /**
     * <p>Returns a handle to the window witch is currently active.</p>
     * 
     * @return
     *   
     */
    public WebDriverWindow getCurrentWindow() {
        return new WebDriverWindow(this.driver, this.driver.getWindowHandle());
    }
    
    /**
     * <p></p>
     * 
     * <p>Note: If no window is found with the specified URL, </p>
     * 
     * @param url
     *   
     * @return
     *   
     */
    public WebDriverWindow findWindow(String url) {
        if(url == null)
            throw new IllegalArgumentException();
        
        TargetLocator locator = this.driver.switchTo();
        
        for(String handle : this.driver.getWindowHandles()) {
            locator.window(handle);
            
            if(url.equals(this.driver.getCurrentUrl()))
                return new WebDriverWindow(this.driver, handle);
        }
        
        return null;
    }
    
    /**
     * <p></p>
     * 
     * @return
     */
    public WebDriverWindow openNewWindow() {
        return openNewWindow(false);
    }
    
    public WebDriverWindow openNewWindow(boolean focus) {
        return openNewWindow("", focus);
    }
    
    /**
     * <p></p>
     * 
     * @param url
     * @return
     */
    public WebDriverWindow openNewWindow(String url) {
        return openNewWindow(url, false);
    }
    
    public WebDriverWindow openNewWindow(String url, boolean focus) {
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
}
