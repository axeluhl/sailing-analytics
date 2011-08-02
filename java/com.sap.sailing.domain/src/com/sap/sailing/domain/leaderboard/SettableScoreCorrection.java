package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface SettableScoreCorrection extends ScoreCorrection {

    void setMaxPointsReason(Competitor competitor, TrackedRace race, MaxPointsReason reason);

    void correctScore(Competitor competitor, TrackedRace race, int points);

}
