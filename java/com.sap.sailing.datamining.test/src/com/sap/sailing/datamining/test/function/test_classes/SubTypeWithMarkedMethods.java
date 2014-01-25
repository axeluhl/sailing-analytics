package com.sap.sailing.datamining.test.function.test_classes;

import com.sap.sailing.datamining.annotations.SideEffectFreeValue;

/*
 * DON'T CHANGE THE METHOD/CLASS NAMES!
 * The tests will fail, because they are reflected via constant strings.
 */

public class SubTypeWithMarkedMethods extends SuperTypeWithMarkedMethods {
    
    @SideEffectFreeValue("decrement")
    public int decrement(int i) {
        return i - 1;
    }

}
