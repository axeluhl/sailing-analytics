package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.shared.WindStrength;

public class WindStrengthSelectionCriteria extends AbstractSelectionCriteria<WindStrength> {

    public WindStrengthSelectionCriteria(Collection<WindStrength> windStrengthes) {
        super(windStrengthes);
    }

    @Override
    public boolean matches(SelectionContext context) {
        return true;
    }

}
