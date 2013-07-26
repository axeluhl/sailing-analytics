package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.impl.criterias.RegattaSelectionCriteria;
import com.sap.sailing.datamining.shared.SelectionType;

public class SelectionCriteriaFactory {
    
    @SuppressWarnings("unchecked")
    public static <T> SelectionCriteria createSelectionCriteria(SelectionType type, Collection<T> selection) {
        switch (type) {
        case Regatta:
            return new RegattaSelectionCriteria((Collection<String>) selection);
        default:
            throw new IllegalArgumentException("Not yet implemented for the given selection type.");
        }
    }

}
