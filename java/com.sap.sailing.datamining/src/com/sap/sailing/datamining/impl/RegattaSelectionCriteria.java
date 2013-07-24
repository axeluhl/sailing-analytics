package com.sap.sailing.datamining.impl;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;

public class RegattaSelectionCriteria extends AbstractSelectionCriteria<String> {

    public RegattaSelectionCriteria(Collection<String> selection) {
        super(selection);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRegatta() == null) {
            return false;
        }
        
        for (String regattaName : getSelection()) {
            if (regattaName.equals(context.getTrackedRegatta().getRegatta().getName())) {
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
