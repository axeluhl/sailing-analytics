package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.EventSelector;
import com.sap.sailing.datamining.shared.SelectorType;

public class SelectorFactory {
    
    public static Selector createEventSelector(String... eventNames) {
        return new EventSelector(eventNames);
    }
    
    public static Selector createSelector(SelectorType selectorType, String... selectionIdentifiers) {
        switch (selectorType) {
        case Events:
            return createEventSelector(selectionIdentifiers);
        default:
            throw new IllegalArgumentException("Case for the selector type '" + selectorType + "' isn't implemented.");
        }
    }

}
