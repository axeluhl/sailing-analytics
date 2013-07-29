package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.datamining.shared.WindStrength;

public class WindStrengthSelectionCriteria extends AbstractSelectionCriteria<WindStrength> {

    public WindStrengthSelectionCriteria(Collection<WindStrength> windStrengthes) {
        super(windStrengthes);
    }

    @Override
    public boolean matches(SelectionContext context) {
        //Returns allways true if there's any data, because looping over all fixes has to be done by the data retriever too.
        //This saves some time.
        return context.getTrackedRegatta() != null;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
