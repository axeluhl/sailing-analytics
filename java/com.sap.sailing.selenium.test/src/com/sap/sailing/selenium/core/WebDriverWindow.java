package com.sap.sailing.selenium.core;

import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebDriverException;

public class WebDriverWindow {

    private static int instanceCount = 0;
    
    private static final String JAVA_SCRIPT = "var anchorTag = document.createElement('a');" +
                                              "anchorTag.setAttribute('id', '%s');" +
                                              "anchorTag.setAttribute('href', '%s');" +
                                              "anchorTag.setAttribute('target', '_blank');" +
                                              "anchorTag.setAttribute('style', 'display:block;');" +
                                              "anchorTag.appendChild(document.createTextNode('nwh'));" +
                                              "document.getElementsByTagName('body')[0].appendChild(anchorTag);";
    
    private static String createUniqueName() {
        return "WebDriverWindow_" + instanceCount++;
    }
    
    private WebDriver driver;
    
    private String name;
    
    private String handle;
    
    private String parent;

    /**
     * Creates a new window for given web driver
     * 
     * @param driver
     *   WebDriver instance
     * @param url
     *   Initial URL to load
     */
    public WebDriverWindow(WebDriver driver, String url) {
        this.driver = driver;
        this.name = createUniqueName();
        
        createWindow(url);
        // Switch to that window and load the url to wait
        switchToWindow();//.get(url);
    }

    public String getWindowHandle() {
        return this.handle;
    }

    public String getParentHandle() {
        return this.parent;
    }

    public void close() {
        switchToWindow().close();
        this.handle = "";
        // Switch back to the parent window
        this.driver.switchTo().window(this.parent);
    }

    public WebDriver switchToWindow() {
        checkForClosed();
        
        return switchTo(this.handle);
    }

    public WebDriver switchToParent() {
        checkForClosed();
        
        return switchTo(this.parent);
    }

    private void createWindow(String url) {
        // Record old handles
        Set<String> oldHandles = this.driver.getWindowHandles();
        this.parent = this.driver.getWindowHandle();

        // Inject an anchor element
        injectAnchorTag(this.name, url);

        // Click on the anchor element
        this.driver.findElement(By.id(this.name)).click();

        this.handle = getNewHandle(oldHandles);
    }

    private String getNewHandle(Set<String> oldHandles) {
        Set<String> newHandles = this.driver.getWindowHandles();
        newHandles.removeAll(oldHandles);

        // Find the new window
        for(String handle : newHandles)
            return handle;

        return null;
    }

    private void checkForClosed() {
        if(this.handle == null || this.handle.equals(""))
            throw new WebDriverException("Web Window closed or not initialized");
    }

    private void injectAnchorTag(String id, String url) {
        JavascriptExecutor executor = (JavascriptExecutor) this.driver;
        executor.executeScript(String.format(JAVA_SCRIPT, id, url));
    }
    
    private WebDriver switchTo(String handle) {
        TargetLocator locator = this.driver.switchTo();
        
        return locator.window(handle);
    }
}
