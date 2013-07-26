package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.common.LegType;

public class LegTypeSelectionCriteria extends AbstractSelectionCriteria<LegType> {

    public LegTypeSelectionCriteria(Collection<LegType> selection) {
        super(selection);
    }

    @Override
    public boolean matches(SelectionContext context) {
        return context.getTrackedRace() != null;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
