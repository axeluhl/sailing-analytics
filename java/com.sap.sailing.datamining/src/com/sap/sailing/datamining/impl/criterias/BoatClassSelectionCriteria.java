package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.impl.retrievers.TrackedRegattaDataRetriever;

public class BoatClassSelectionCriteria extends AbstractSelectionCriteria<String> {

    public BoatClassSelectionCriteria(Collection<String> boatClassNames) {
        super(boatClassNames);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRegatta() == null) {
            return false;
        }
        
        for (String boatClassName : getSelection()) {
            if (boatClassName.equals(context.getTrackedRegatta().getRegatta().getBoatClass().getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        return new TrackedRegattaDataRetriever(context.getTrackedRegatta());
    }

}
