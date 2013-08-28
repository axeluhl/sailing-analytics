package com.sap.sailing.selenium.core;

import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;

public class WindowManager {
    private static final String JAVA_SCRIPT = "window.open('%s', '_blank')";
    
    private WebDriver driver;
    
    public WindowManager(WebDriver driver) {
        this.driver = driver;
    }
    
    public void switchTo(String url) {
        WebDriverWindow window = findWindow(url);
        
        if(window == null)
            throw new RuntimeException("Window not found");
        
        window.switchToWindow();
    }
    
    public WebDriverWindow getCurrentWindow() {
        return new WebDriverWindow(this.driver, this.driver.getWindowHandle());
    }
    
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
    
    public WebDriverWindow openNewWindow() {
        return openNewWindow("");
    }
    
    public WebDriverWindow openNewWindow(String url) {
        return new WebDriverWindow(this.driver, createWindow(url));
    }
    
    private String createWindow(String url) {
        // Record old handles
        Set<String> oldHandles = this.driver.getWindowHandles();
        
        executeScript(url != null ? url : "");
        // Inject an anchor element
        //injectAnchorTag(this.name, url);
        // Click on the anchor element
        //this.driver.findElement(By.id(this.name)).click();

        return getNewHandle(oldHandles);
    }
    
    private void executeScript(String url) {
        JavascriptExecutor executor = (JavascriptExecutor) this.driver;
        //executor.executeScript(String.format(JAVA_SCRIPT, id, url));
        String script = String.format(JAVA_SCRIPT, url);
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
