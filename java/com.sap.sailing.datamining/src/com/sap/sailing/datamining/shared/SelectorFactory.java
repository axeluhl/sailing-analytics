package com.sap.sailing.datamining.shared;

import com.sap.sailing.datamining.impl.EventSelector;

public class SelectorFactory {
    
    public static Selector createEventSelector(String... eventNames) {
        return new EventSelector(eventNames);
    }

}
