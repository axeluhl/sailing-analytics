package com.sap.sailing.selenium.core;

import java.lang.reflect.Field;

import java.util.List;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.pagefactory.Annotations;
import org.openqa.selenium.support.pagefactory.ElementLocator;

import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Function;

public class SeleniumElementLocator implements ElementLocator {
    private final SearchContext context;
    private final FluentWait<SearchContext> wait;
    private final boolean useCache;
    
    private final By by;
    
    private WebElement element;
    private List<WebElement> elements;
    
    public SeleniumElementLocator(SearchContext context, Field field, int timeOut) {
        this(context, field, timeOut, 250);
    }

    public SeleniumElementLocator(SearchContext context, Field field, int timeOut, int intervall) {
        this.context = context;
        
        this.wait = new FluentWait<>(this.context);
        this.wait.withTimeout(timeOut, TimeUnit.SECONDS);
        this.wait.pollingEvery(intervall, TimeUnit.MILLISECONDS);
        this.wait.ignoring(NoSuchElementException.class);
        
        Annotations annotations = new Annotations(field);
        this.useCache = annotations.isLookupCached();
        this.by = annotations.buildBy();
    }
    
    @Override
    public WebElement findElement() {
        if(this.useCache && this.element != null) {
            return this.element;
        }
        
        this.element = this.wait.until(new Function<SearchContext, WebElement>() {
            @Override
            public WebElement apply(SearchContext context) {
                WebElement element = context.findElement(SeleniumElementLocator.this.getBy());
                
                return (isElementUsable(element) ? element : null);
            }
        });
        
        return this.element;
    }

    @Override
    public List<WebElement> findElements() {
        if(this.useCache && this.elements != null) {
            return this.elements;
        }
        
        this.elements = this.wait.until(new Function<SearchContext, List<WebElement>>() {
            @Override
            public List<WebElement> apply(SearchContext context) {
                return context.findElements(SeleniumElementLocator.this.getBy());
            }
        });
        
        return this.elements;
    }
    
    public By getBy() {
        return this.by;
    }
    
    protected boolean isElementUsable(WebElement element) {
        return element.isDisplayed();
    }
}
