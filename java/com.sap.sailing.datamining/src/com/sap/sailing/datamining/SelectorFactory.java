package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.datamining.impl.SelectorImpl;
import com.sap.sailing.datamining.shared.Dimension;

public class SelectorFactory {

    public static Selector createSelector(Map<Dimension, Collection<?>> selection) {
        return new SelectorImpl(SelectionCriteriaFactory.createSelectionCriteria(selection));
    }

}
