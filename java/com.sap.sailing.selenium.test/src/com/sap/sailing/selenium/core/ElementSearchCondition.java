package com.sap.sailing.selenium.core;

import org.openqa.selenium.SearchContext;

import com.google.common.base.Function;

public interface ElementSearchCondition<T> extends Function<SearchContext, T> {
}
