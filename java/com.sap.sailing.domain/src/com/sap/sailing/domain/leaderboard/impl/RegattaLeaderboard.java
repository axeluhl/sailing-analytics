package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.leaderboard.RaceColumn;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;

/**
 * A leaderboard that is based on the definition of a {@link Regatta} with its {@link Series} and {@link Fleet}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaLeaderboard extends AbstractLeaderboardImpl {
    private static final long serialVersionUID = -8243802111008628779L;
    private final Regatta regatta;
    
    public RegattaLeaderboard(Regatta regatta, SettableScoreCorrection scoreCorrection,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(regatta.getName(), scoreCorrection, resultDiscardingRule);
        this.regatta = regatta;
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>();
        for (Series series : regatta.getSeries()) {
            for (RaceColumn raceColumnInSeries : series.getRaceColumns()) {
                result.add(raceColumnInSeries);
            }
        }
        return result;
    }
}
