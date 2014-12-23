package com.sap.sse.datamining.test.functions.test_classes;

import java.util.Locale;

import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;

/*
 * DON'T CHANGE THE METHOD/CLASS NAMES!
 * The tests will fail, because they are reflected via constant strings.
 */

public class SimpleClassWithMarkedMethods {
    
    @Dimension(messageKey="dimension")
    public String dimension() {
        return "Method marked as dimension";
    }
    
    //Methods without a return value can't be dimensions
    @Dimension(messageKey="illegalDimension")
    public void illegalDimension() {
        
    }
    
    @Statistic(messageKey="value")
    public int sideEffectFreeValue() {
        return 1;
    }
    
    public void unmarkedMethod() {
        
    }
    
    //Methods with parameters
    
    public int increment(int i) {
        return i + 1;
    }
    
    public String getLocalizedName(Locale locale, DataMiningStringMessages stringMessages) {
        return "";
    }

}
