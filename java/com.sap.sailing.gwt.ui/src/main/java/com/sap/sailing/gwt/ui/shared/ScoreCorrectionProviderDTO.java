package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderDTO extends NamedDTO {
    private Map<String, Pair<String, Date>> hasResultsForBoatClassFromDateByEventName;
    
    public ScoreCorrectionProviderDTO() {}

    public ScoreCorrectionProviderDTO(String name, Map<String, Pair<String, Date>> hasResultsForBoatClassFromDateByEventName) {
        super(name);
        this.hasResultsForBoatClassFromDateByEventName = hasResultsForBoatClassFromDateByEventName;
    }
    
    public Map<String, Pair<String, Date>> getHasResultsForBoatClassFromDateByEventName() {
        return hasResultsForBoatClassFromDateByEventName;
    }

}
