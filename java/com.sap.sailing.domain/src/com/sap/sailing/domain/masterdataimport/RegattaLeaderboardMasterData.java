package com.sap.sailing.domain.masterdataimport;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public class RegattaLeaderboardMasterData extends LeaderboardMasterData {

    private String regattaName;
    private Regatta regatta;

    public RegattaLeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<Serializable, Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection,
            String regattaName, Map<Serializable, Double> carriedPoints, List<Serializable> suppressedCompetitors,
            Map<Serializable, String> displayNamesByCompetitorId,
            Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents) {
        super(name, displayName, resultDiscardingRule, competitorsById, scoreCorrection, carriedPoints,
                suppressedCompetitors, displayNamesByCompetitorId, raceLogEvents);
        this.regattaName = regattaName;
    }

    public RegattaIdentifier getRegattaName() {
        return new RegattaName(regattaName);
    }

    public void setRegatta(Regatta regatta) {
        this.regatta = regatta;
    }

    @Override
    public Leaderboard getLeaderboard() {
        if (regatta == null) {
            return null;
        }
        return new RegattaLeaderboardImpl(regatta, getResultDiscardingRule());
    }

}
