package com.sap.sailing.ess40.resultimport.impl;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class RegattaScoreCorrectionsImpl implements RegattaScoreCorrections {
    private final ScoreCorrectionProviderImpl provider;
    
    public RegattaScoreCorrectionsImpl(ScoreCorrectionProviderImpl scoreCorrectionProviderImpl,
            Pair<TimePoint, Map<String, List<Pair<String, Integer>>>> actResults) {
        this.provider = scoreCorrectionProviderImpl;
    }

    @Override
    public ScoreCorrectionProvider getProvider() {
        return provider;
    }

    @Override
    public String getRegattaName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces() {
        // TODO Auto-generated method stub
        return null;
    }

}
