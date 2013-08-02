package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.impl.EventSelector;
import com.sap.sailing.datamining.impl.RegattaSelector;
import com.sap.sailing.datamining.impl.SelectorImpl;
import com.sap.sailing.datamining.shared.SelectionType;
import com.sap.sailing.datamining.shared.SelectorType;

public class SelectorFactory {

    public static Selector createSelector(Map<SelectionType, Collection<?>> selection) {
        return new SelectorImpl(SelectionCriteriaFactory.createSelectionCriteria(selection));
    }
    
    public static <T> Selector createSelector(SelectionType type, Collection<T> selection) {
        return new SelectorImpl(SelectionCriteriaFactory.createSelectionCriteria(type, selection));
    }
    
    public static Selector createSelector(SelectorType selectorType, String... selectionIdentifiers) {
        switch (selectorType) {
        case Events:
            return createEventSelector(selectionIdentifiers);
        case Regattas:
            return createRegattaSelector(selectionIdentifiers);
        default:
            throw new IllegalArgumentException("Case for the selector type '" + selectorType + "' isn't implemented.");
        }
    }
    
    public static Selector createEventSelector(String... eventNames) {
        return new EventSelector(eventNames);
    }

    private static Selector createRegattaSelector(String[] selectionIdentifiers) {
        return new RegattaSelector(selectionIdentifiers);
    }

}
