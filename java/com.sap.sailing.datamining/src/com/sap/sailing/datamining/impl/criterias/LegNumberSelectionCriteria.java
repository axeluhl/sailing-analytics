package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;

public class LegNumberSelectionCriteria extends AbstractSelectionCriteria<Integer> {

    public LegNumberSelectionCriteria(Collection<Integer> selection) {
        super(selection);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRace() == null) {
            return false;
        }
        
        for (Integer selection : getSelection()) {
            if (selection < context.getTrackedRace().getRace().getCourse().getLegs().size()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        // TODO Implement data retriever
        return null;
    }

}
