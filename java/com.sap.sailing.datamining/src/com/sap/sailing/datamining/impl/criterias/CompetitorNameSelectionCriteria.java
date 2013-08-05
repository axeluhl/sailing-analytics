package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;

public class CompetitorNameSelectionCriteria extends AbstractSelectionCriteria<String> {

    public CompetitorNameSelectionCriteria(Collection<String> competitorNames) {
        super(competitorNames);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getCompetitor() == null) {
            return false;
        }
        
        for (String competitorName : getSelection()) {
            if (competitorName.equals(context.getCompetitor().getName())) {
                return true;
            }
        }
        return false;
    }

}
