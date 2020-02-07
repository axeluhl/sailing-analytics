package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionForCompetitorInRace;
import com.sap.sailing.domain.common.RegattaScoreCorrections.ScoreCorrectionsForRace;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionMapping;
import com.sap.sse.common.Util.Pair;

public class ScoreCorrectionMappingImpl implements ScoreCorrectionMapping {
    private final Map<String, RaceColumn> raceMappings;
    private final Map<String, Competitor> competitorMappings;
    private final RegattaScoreCorrections regattaScoreCorrections;
    
    public ScoreCorrectionMappingImpl(Map<String, RaceColumn> raceMappings, Map<String, Competitor> competitorMappings,
            RegattaScoreCorrections regattaScoreCorrections) {
        super();
        this.raceMappings = raceMappings;
        this.competitorMappings = competitorMappings;
        this.regattaScoreCorrections = regattaScoreCorrections;
    }

    @Override
    public Map<String, RaceColumn> getRaceMappings() {
        return raceMappings;
    }

    @Override
    public Map<String, Competitor> getCompetitorMappings() {
        return competitorMappings;
    }

    @Override
    public Map<RaceColumn, Map<Competitor, Pair<Double, MaxPointsReason>>> getScoreCorrections() {
        final Map<RaceColumn, Map<Competitor, Pair<Double, MaxPointsReason>>> result = new HashMap<>();
        for (final ScoreCorrectionsForRace raceCorrection : regattaScoreCorrections.getScoreCorrectionsForRaces()) {
            final RaceColumn raceColumn = getRaceMappings().get(raceCorrection.getRaceNameOrNumber());
            if (raceColumn != null) {
                final Map<Competitor, Pair<Double, MaxPointsReason>> competitorMapForRace = result.computeIfAbsent(raceColumn, k->new HashMap<>());
                for (final String sailIdOrShortName : raceCorrection.getSailIDs()) {
                    final Competitor competitor = getCompetitorMappings().get(sailIdOrShortName);
                    final ScoreCorrectionForCompetitorInRace competitorCorrection = raceCorrection.getScoreCorrectionForCompetitor(sailIdOrShortName);
                    if (competitor != null && competitorCorrection != null) {
                        competitorMapForRace.put(competitor, new Pair<>(competitorCorrection.getPoints(), competitorCorrection.getMaxPointsReason()));
                    }
                }
            }
        }
        return result;
    }

}
