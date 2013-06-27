package com.sap.sailing.domain.masterdataimport;

import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.impl.RegattaLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;


public class RegattaLeaderboardMasterData extends LeaderboardMasterData{
    
    private String regattaName;
    private Regatta regatta;

    public RegattaLeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Set<Competitor> competitors, ScoreCorrectionMasterData scoreCorrection, String regattaName) {
        super(name, displayName, resultDiscardingRule, competitors, scoreCorrection);
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
        return new RegattaLeaderboardImpl(regatta, new ScoreCorrectionImpl(), getResultDiscardingRule());
    }   

}
