package com.sap.sailing.kiworesultimport.impl;

import java.util.Map;

import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = -4596215011753860781L;

    private static final String name = "Kieler Woche Official Results by b+m";
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Pair<String, TimePoint>> getHasResultsForBoatClassFromDateByEventName() {
        // TODO Auto-generated method stub
        return null;
    }

}
