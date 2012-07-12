package com.sap.sailing.freg.resultimport.impl;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
    private static final long serialVersionUID = 5853404150107387702L;

    @Override
    public String getName() {
        return "FREG HTML Score Importer";
    }
    
    @Override
    public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName() throws Exception {
        // TODO 
        return null;
    }

    @Override
    public RegattaScoreCorrections getScoreCorrections(String actName, String boatClassName,
            TimePoint millisecondsTimePoint) throws Exception {
        // TODO
        return null;
    }

}
