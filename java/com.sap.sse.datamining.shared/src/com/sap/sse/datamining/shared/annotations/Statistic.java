package com.sap.sse.datamining.shared.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sap.sse.datamining.shared.Unit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Statistic {
    
    public String messageKey();
    
    public Unit resultUnit() default Unit.None;
        
    public int resultDecimals() default 0;
    
    public int ordinal() default Integer.MAX_VALUE;

}
