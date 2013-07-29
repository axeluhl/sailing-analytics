package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.impl.criterias.BoatClassSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.CompetitorNameSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.LegNumberSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.LegTypeSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.NationalitySelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.RaceSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.RegattaSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.SailIDSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.WindStrengthSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.YearSelectionCriteria;
import com.sap.sailing.datamining.shared.SelectionType;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.common.LegType;

public class SelectionCriteriaFactory {
    
    @SuppressWarnings("unchecked")
    public static <T> SelectionCriteria createSelectionCriteria(SelectionType type, Collection<T> selection) {
        switch (type) {
        case Regatta:
            return new RegattaSelectionCriteria((Collection<String>) selection);
        case BoatClass:
            return new BoatClassSelectionCriteria((Collection<String>) selection);
        case Competitor:
            return new CompetitorNameSelectionCriteria((Collection<String>) selection);
        case Leg:
            return new LegNumberSelectionCriteria((Collection<Integer>) selection);
        case LegType:
            return new LegTypeSelectionCriteria((Collection<LegType>) selection);
        case Nationality:
            return new NationalitySelectionCriteria((Collection<String>) selection);
        case Race:
            return new RaceSelectionCriteria((Collection<String>) selection);
        case SailID:
            return new SailIDSelectionCriteria((Collection<String>) selection);
        case WindStrength:
            return new WindStrengthSelectionCriteria((Collection<WindStrength>) selection);
        case Year:
            return new YearSelectionCriteria((Collection<Integer>) selection);
        default:
            throw new IllegalArgumentException("Not yet implemented for the given selection type.");
        }
    }

}
