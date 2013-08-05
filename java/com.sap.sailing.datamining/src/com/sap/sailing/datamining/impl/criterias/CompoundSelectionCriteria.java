package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;
import java.util.HashSet;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;

public class CompoundSelectionCriteria implements SelectionCriteria {
    
    private Collection<SelectionCriteria> criterias;

    public CompoundSelectionCriteria() {
        criterias = new HashSet<SelectionCriteria>();
    }
    
    public void addCriteria(SelectionCriteria criteria) {
        criterias.add(criteria);
    }

    @Override
    public boolean matches(SelectionContext context) {
        for (SelectionCriteria criteria : criterias) {
            if (!criteria.matches(context)) {
                return false;
            }
        }
        return true;
    }

}
