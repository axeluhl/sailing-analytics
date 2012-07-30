package com.sap.sailing.freg.resultimport.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.resultimport.CompetitorRow;
import com.sap.sailing.resultimport.RegattaResults;

public class RegattaScoreCorrectionsImpl implements RegattaScoreCorrections {
    private final ScoreCorrectionProviderImpl provider;
    private final Iterable<ScoreCorrectionsForRace> scoreCorrectionsForRaces;
    
    public RegattaScoreCorrectionsImpl(ScoreCorrectionProviderImpl scoreCorrectionProviderImpl,
            RegattaResults regattaResult) {
        this.provider = scoreCorrectionProviderImpl;
        List<ScoreCorrectionsForRace> scfr = new ArrayList<ScoreCorrectionsForRace>();
        int maxSize = getMaxListSize(regattaResult);
        for (int i=0; i<maxSize; i++) {
            scfr.add(new ScoreCorrectionForRaceImpl(regattaResult, i));
        }
        this.scoreCorrectionsForRaces = scfr;
    }

    private int getMaxListSize(RegattaResults regattaResult) {
        int result = 0;
        for (CompetitorRow competitorRow : regattaResult.getCompetitorResults()) {
            result = Math.max(result, Util.size(competitorRow.getRankAndMaxPointsReasonAndPointsAndDiscarded()));
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
