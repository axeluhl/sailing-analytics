package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;

public class LegNumberSelectionCriteria extends AbstractSelectionCriteria<Integer> {

    public LegNumberSelectionCriteria(Collection<Integer> legNumbers) {
        super(legNumbers);
    }

    @Override
    public boolean matches(SelectionContext context) {
        for (Integer legNumberToCheck : getSelection()) {
            if (legNumberToCheck.equals(context.getLegNumber())) {
                return true;
            }
        }
        return false;
    }

}
