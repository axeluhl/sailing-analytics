package com.sap.sailing.gwt.ui.shared;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.sap.sse.common.Util;
import com.sap.sse.security.shared.dto.NamedDTO;

public class ScoreCorrectionProviderDTO extends NamedDTO {
    private static final long serialVersionUID = -636159261445539142L;
    private Map<String, Set<Util.Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName;
    
    public ScoreCorrectionProviderDTO() {}

    public ScoreCorrectionProviderDTO(String name, Map<String, Set<Util.Pair<String, Date>>> hasResultsForBoatClassFromDateByEventName2) {
        super(name);
        this.hasResultsForBoatClassFromDateByEventName = hasResultsForBoatClassFromDateByEventName2;
    }
    
    public Map<String, Set<Util.Pair<String, Date>>> getHasResultsForBoatClassFromDateByEventName() {
        return hasResultsForBoatClassFromDateByEventName;
    }

}
