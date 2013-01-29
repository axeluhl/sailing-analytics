package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderDTO extends NamedDTO {
    private Map<String, Set<Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName;
    
    public ScoreCorrectionProviderDTO() {}

    public ScoreCorrectionProviderDTO(String name, Map<String, Set<Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName2) {
        super(name);
        this.hasResultsForBoatClassFromDateByEventName = hasResultsForBoatClassFromDateByEventName2;
    }
    
    public Map<String, Set<Pair<String, Date>>> getHasResultsForBoatClassFromDateByEventName() {
        return hasResultsForBoatClassFromDateByEventName;
    }

}
