package com.sap.sailing.ess40.resultimport.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sse.common.Util;

public class RegattaScoreCorrectionsImpl implements RegattaScoreCorrections {
    private final ScoreCorrectionProviderImpl provider;
    private final Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces;
    
    public RegattaScoreCorrectionsImpl(ScoreCorrectionProviderImpl scoreCorrectionProviderImpl,
            Map<String, List<Util.Pair<String, Double>>> actResults) {
        this.provider = scoreCorrectionProviderImpl;
        List<ScoreCorrectionsForRace> scfr = new ArrayList<ScoreCorrectionsForRace>();
        int maxSize = getMaxListSize(actResults);
        for (int i=0; i<maxSize; i++) {
            scfr.add(new ScoreCorrectionForRaceImpl(actResults, i));
        }
        this.scoreCorrectionsForRaces = scfr;
    }

    private int getMaxListSize(Map<String, List<Util.Pair<String, Double>>> actResults) {
        int result = 0;
        for (Map.Entry<String, List<Util.Pair<String, Double>>> e : actResults.entrySet()) {
            result = Math.max(result, e.getValue().size());
        }
        return result;
    }

    @Override
    public ScoreCorrectionProvider getProvider() {
        return provider;
    }

    @Override
    public String getRegattaName() {
        return "Extreme Sailing Series";
    }

    @Override
    public Iterable<ScoreCorrectionsForRace> getScoreCorrectionsForRaces() {
        return scoreCorrectionsForRaces;
    }

}
