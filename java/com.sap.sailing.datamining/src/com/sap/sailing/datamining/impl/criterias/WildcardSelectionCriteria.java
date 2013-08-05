package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;

public class WildcardSelectionCriteria implements SelectionCriteria {

    @Override
    public boolean matches(SelectionContext context) {
        return true;
    }

}
