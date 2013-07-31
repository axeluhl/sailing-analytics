package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.impl.retrievers.LegTypeDataRetriever;
import com.sap.sailing.domain.common.LegType;

public class LegTypeSelectionCriteria extends AbstractSelectionCriteria<LegType> {

    public LegTypeSelectionCriteria(Collection<LegType> legTypes) {
        super(legTypes);
    }

    @Override
    public boolean matches(SelectionContext context) {
        //Returns always true, if there's any data. There are only 3 leg types and nearly every race contains at least two of them.
        //It saves time, because the data retriever has to check each leg for it's type anyway, which would be done here.
        return context.getTrackedRegatta() != null;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        return new LegTypeDataRetriever(context.getTrackedRace(), getSelection());
    }

}
