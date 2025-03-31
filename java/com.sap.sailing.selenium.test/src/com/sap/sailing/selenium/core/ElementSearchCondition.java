package com.sap.sailing.selenium.core;

import java.util.function.Function;

import org.openqa.selenium.SearchContext;

/**
 * <p>Models a condition that might reasonably be expected to eventually evaluate to something that is neither
 *   {@code null} nor {@code false} using a search context. An example would be that an element is visible.</p>
 * 
 * <p>Note that it is expected that conditions are idempotent. They will be called in a loop by the WebDriverWait
 *   and any modification of the state of the application under test may have unexpected side-effects.</p>
 * 
 * @param <T>
 *   The return type 
 * @author
 *   D049941
 */
public interface ElementSearchCondition<T> extends Function<SearchContext, T> {
}
