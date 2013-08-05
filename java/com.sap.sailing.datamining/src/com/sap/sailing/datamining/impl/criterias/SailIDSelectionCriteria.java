package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;

public class SailIDSelectionCriteria extends AbstractSelectionCriteria<String> {

    public SailIDSelectionCriteria(Collection<String> sailIDs) {
        super(sailIDs);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getCompetitor() == null) {
            return false;
        }
        
        for (String sailID : getSelection()) {
            if (sailID.equals(context.getCompetitor().getBoat().getSailID())) {
                return true;
            }
        }
        return false;
    }

}
