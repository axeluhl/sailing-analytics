package com.sap.sse.datamining.shared.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;

import com.sap.sse.datamining.shared.Unit;

/**
 * Methods marked with this annotation will be used as computable statistic for the data mining framework. The method will be called,
 * if the statistic value of a data element is extracted.<br />
 * The marked method has to match the following conditions or the data mining could fail:
 * <ul>
 *      <li>Has no parameters (except if the parameter list is exactly {@link Locale}, {@link ResourceBundleStringMessages})</li>
 *      <li>The return type isn't <code>void</code></li>
 *      <li>Is side effect free</li>
 * </ul>
 * 
 * The return type of the method has to be processable by an aggregator of the data mining framework (e.g. <code>int</code> or <code>double</code>)
 * 
 * @author Lennart Hensler (D054527)
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Statistic {
    
    /**
     * The message key used for internationalization
     */
    public String messageKey();
    
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
     * The {@link Unit} of the methods return value. The default value is {@link Unit#None}.
     */
    public Unit resultUnit() default Unit.None;
        
    /**
     * The number of decimals of the methods return value. The default value is <code>0</code>.
     */
    public int resultDecimals() default 0;

}
