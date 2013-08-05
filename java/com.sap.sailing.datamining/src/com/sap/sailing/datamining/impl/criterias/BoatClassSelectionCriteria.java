package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;

public class BoatClassSelectionCriteria extends AbstractSelectionCriteria<String> {

    public BoatClassSelectionCriteria(Collection<String> boatClassNames) {
        super(boatClassNames);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRegatta() == null) {
            return false;
        }
        
        String boatClassName = context.getTrackedRegatta().getRegatta().getBoatClass().getName();
        for (String boatClassNameToCheck : getSelection()) {
            if (boatClassNameToCheck.equals(boatClassName)) {
                return true;
            }
        }
        return false;
    }

}
