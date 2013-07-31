package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.impl.retrievers.LegNumberDataRetriever;

public class LegNumberSelectionCriteria extends AbstractSelectionCriteria<Integer> {

    public LegNumberSelectionCriteria(Collection<Integer> legNumbers) {
        super(legNumbers);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRace() == null) {
            return false;
        }

        int legNumberOfRace = context.getTrackedRace().getRace().getCourse().getLegs().size();
        for (Integer legNumber : getSelection()) {
            if (legNumber < legNumberOfRace) {
                return true;
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        return new LegNumberDataRetriever(context.getTrackedRace(), getSelection());
    }

}
