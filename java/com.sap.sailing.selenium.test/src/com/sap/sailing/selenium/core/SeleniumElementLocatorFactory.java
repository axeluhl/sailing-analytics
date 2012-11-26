package com.sap.sailing.selenium.core;

import java.lang.reflect.Field;

import org.openqa.selenium.SearchContext;

import org.openqa.selenium.support.pagefactory.ElementLocator;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;

public class SeleniumElementLocatorFactory implements ElementLocatorFactory {
    private SearchContext context;
    private final int timeOut;
    private final int intervall;
    
    public SeleniumElementLocatorFactory(SearchContext context, int timeOut) {
        this(context, timeOut, 250);
    }
    
    public SeleniumElementLocatorFactory(SearchContext context, int timeOut, int intervall) {
        this.context = context;
        this.timeOut = timeOut;
        this.intervall = intervall;
    }

    @Override
    public ElementLocator createLocator(Field field) {
        return new SeleniumElementLocator(this.context, field, this.timeOut, this.intervall);
    }
}
