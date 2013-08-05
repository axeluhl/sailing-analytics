package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.impl.SelectorImpl;
import com.sap.sailing.datamining.shared.SelectionType;

public class SelectorFactory {

    public static Selector createSelector(Map<SelectionType, Collection<?>> selection) {
        return new SelectorImpl(SelectionCriteriaFactory.createSelectionCriteria(selection));
    }

}
