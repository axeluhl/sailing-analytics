package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.Renamable;
import com.sap.sailing.domain.regattalike.IsRegattaLike;

public interface FlexibleRaceColumn extends RaceColumn, Renamable {
    void setIsMedalRace(boolean isMedalRace);
    void setRegattaLikeHelper(IsRegattaLike regattaLikeHelper);
}
