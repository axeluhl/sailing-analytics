package com.sap.sailing.selenium.test;

import java.util.List;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.PageFactory;

import org.openqa.selenium.support.ui.FluentWait;

import com.sap.sailing.selenium.core.AjaxCallsComplete;
import com.sap.sailing.selenium.core.BySeleniumId;
import com.sap.sailing.selenium.core.SeleniumElementLocatorFactory;

public class PageObject {
    protected static final int DEFAULT_TIMEOUT = 60;
    
    protected final WebDriver driver;
    protected final SearchContext context;
    
    public PageObject(WebDriver driver) {
        this(driver, driver);
    }
    
    public PageObject(WebDriver driver, SearchContext context) {
        this.driver = driver;
        this.context = context;
        
        initElements();
    }
    
    protected void initElements() {
        PageFactory.initElements(new SeleniumElementLocatorFactory(this.context, getTimeOut()), this);
    }
    
    protected int getTimeOut() {
        return DEFAULT_TIMEOUT;
    }
    
    protected WebElement findElementBySeleniumId(String id) {
        return findElementBySeleniumId(this.context, id);
    }
    
    protected WebElement findElementBySeleniumId(SearchContext context, String id) {
        return context.findElement(new BySeleniumId(id));
    }
    
    protected List<WebElement> findElementsBySeleniumId(String id) {
        return findElementsBySeleniumId(this.context, id);
    }
    
    protected List<WebElement> findElementsBySeleniumId(SearchContext context, String id) {
        return context.findElements(new BySeleniumId(id));
    }
    
    protected void waitForAjaxRequests() {
        waitForAjaxRequests(30, 5);
    }
    
    protected void waitForAjaxRequests(int timeout, int polling) {
        FluentWait<WebDriver> wait = new FluentWait<>(this.driver);
        wait.withTimeout(timeout, TimeUnit.SECONDS);
        wait.pollingEvery(polling, TimeUnit.SECONDS);
        
        wait.until(new AjaxCallsComplete());
    }
}
