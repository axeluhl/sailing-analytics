package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.SelectionCriteria;
import com.sap.sailing.datamining.impl.retrievers.TrackedRegattaDataRetriever;

public class WildcardSelectionCriteria implements SelectionCriteria {

    @Override
    public boolean matches(SelectionContext context) {
        return context.getTrackedRegatta() != null;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        return new TrackedRegattaDataRetriever(context.getTrackedRegatta());
    }

}
