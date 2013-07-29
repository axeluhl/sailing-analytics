package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.SelectionContext;
import com.sap.sailing.domain.base.Competitor;

public class NationalitySelectionCriteria extends AbstractSelectionCriteria<String> {

    public NationalitySelectionCriteria(Collection<String> nationalities) {
        super(nationalities);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getTrackedRace() == null) {
            return false;
        }
        
        for (Competitor competitor : context.getTrackedRace().getRace().getCompetitors()) {
            for (String nationality : getSelection()) {
                if (nationality.equals(competitor.getTeam().getNationality().getThreeLetterIOCAcronym())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public DataRetriever getDataRetriever(SelectionContext context) {
        // TODO Auto-generated method stub
        return null;
    }

}
