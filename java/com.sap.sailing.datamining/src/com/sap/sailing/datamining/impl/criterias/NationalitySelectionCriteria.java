package com.sap.sailing.datamining.impl.criterias;

import java.util.Collection;

import com.sap.sailing.datamining.SelectionContext;

public class NationalitySelectionCriteria extends AbstractSelectionCriteria<String> {

    public NationalitySelectionCriteria(Collection<String> nationalities) {
        super(nationalities);
    }

    @Override
    public boolean matches(SelectionContext context) {
        if (context.getCompetitor() == null) {
            return false;
        }
        
        for (String nationality : getSelection()) {
            if (nationality.equals(context.getCompetitor().getTeam().getNationality().getThreeLetterIOCAcronym())) {
                return true;
            }
        }
        return false;
    }

}
