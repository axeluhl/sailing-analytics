package com.sap.sse.datamining.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Methods marked with this annotation will be used as function for the data mining framework. Functions can be used by the user to
 * create new functions and dimensions.<br />
 * The marked method has to be <b>side effect free</b>.<br />
 * 
 * @author Lennart Hensler (D054527)
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SideEffectFreeValue {
    
    public String value();

}
