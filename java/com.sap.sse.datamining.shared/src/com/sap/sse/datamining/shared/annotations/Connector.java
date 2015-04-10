package com.sap.sse.datamining.shared.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation indicate, that the result type contains marked methods.<br />
 * The marked method has to match the following conditions or the data mining could fail:
 * <ul>
 *      <li>Has no parameters</li>
 *      <li>The return type isn't <code>void</code></li>
 *      <li>Is side effect free</li>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Connector {
    
    /**
     * The message key used for internationalization.<br />
     * If there are more than one ordinal in a function (e.g. if the function is an instance of ConcatenatingCompoundFunction),
     * then the messages will be concatenated (separated by a space).
     */
    public String messageKey() default "";

    /**
     * The ordinal used for the sorting of functions. The default value is {@link Integer#MAX_VALUE}.<br />
     * <br />
     * The standard sorting with this ordinal is ascending (The function with ordinal 0 is the first and the
     * function with <code>Integer.MAX_VALUE</code> is the last in the list).<br />
     * If there are more than one ordinal in a function (e.g. if the function is an instance of ConcatenatingCompoundFunction),
     * then the smallest ordinal is the used ordinal.
     */
    public int ordinal() default Integer.MAX_VALUE;
    
    /**
     * If <code>false</code>, the connection won't be used to find {@link Statistic Statistics}.
     */
    public boolean scanForStatistics() default true;

}
