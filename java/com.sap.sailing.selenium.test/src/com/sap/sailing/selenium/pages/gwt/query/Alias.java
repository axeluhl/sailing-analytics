package com.sap.sailing.selenium.pages.gwt.query;

import com.sap.sailing.selenium.pages.gwt.query.path.BooleanPath;
import com.sap.sailing.selenium.pages.gwt.query.path.StringPath;

public class Alias {
    private static final AliasFactory factory = new AliasFactory();
    
    public static <A> A alias(Class<A> clazz) {
        return factory.createAlias(clazz);
    }
    
    public static <D extends Expression<?>> D $() {
        return factory.getCurrentExpressionAndReset();
    }
    
    public static StringPath $(String value) {
        return $();
    }
    
    public static BooleanPath $(Boolean value) {
        return $();
    }
    
    public static BooleanPath $(boolean value) {
        return $();
    }
    
//    public static NumberPath $(Byte value) {
//        return $();
//    }
    
//  public static NumberPath $(byte value) {
//      return $();
//  }
//    
//    public static NumberPath $(Short value) {
//        return $();
//    }
//  
//  public static NumberPath $(short value) {
//      return $();
//  }
//    
//    public static NumberPath $(Integer value) {
//        return $();
//    }
//  
//  public static NumberPath $(int value) {
//      return $();
//  }
//    
//    public static NumberPath $(Long value) {
//        return $();
//    }
//  
//  public static NumberPath $(long value) {
//      return $();
//  }
//    
//    public static NumberPath $(Float value) {
//        return $();
//    }
//  
//  public static NumberPath $(float value) {
//      return $();
//  }
//    
//    public static NumberPath $(Double value) {
//        return $();
//    }
//  
//  public static NumberPath $(double value) {
//      return $();
//  }
    
    public static void resetAlias() {
        factory.reset();
    }
}
