package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.SideEffectFreeValue;

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
    
    @SideEffectFreeValue(messageKey="value")
    public int sideEffectFreeValue() {
        return 1;
    }
    
    public void unmarkedMethod() {
        
    }
    
    public int increment(int i) {
        return i + 1;
    }

}
