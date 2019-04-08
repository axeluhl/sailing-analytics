package com.sap.sailing.selenium.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Used to mark a field in test cases that should be injected with the {@link TestEnvironment} by the
 *   {@link SeleniumRunner} runner. The environment provides the web driver to use for the tests as well as other information
 *   as configured.</p>
 * 
 * @author
 *   D049941
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Managed {
}
