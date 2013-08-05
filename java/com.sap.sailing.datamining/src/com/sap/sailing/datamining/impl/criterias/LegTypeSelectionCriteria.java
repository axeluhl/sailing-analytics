package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.common.LegType;

public class LegTypeSelectionCriteria extends AbstractSelectionCriteria<LegType> {

    public LegTypeSelectionCriteria(Collection<LegType> legTypes) {
        super(legTypes);
    }

    @Override
    public boolean matches(SelectionContext context) {
        return true;
    }

}
