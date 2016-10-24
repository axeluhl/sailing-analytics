package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.regattalike.IsRegattaLike;

public interface FlexibleRaceColumn extends RaceColumn {
    void setName(String newName);
    void setIsMedalRace(boolean isMedalRace);
    void setRegattaLikeHelper(IsRegattaLike regattaLikeHelper);
}
