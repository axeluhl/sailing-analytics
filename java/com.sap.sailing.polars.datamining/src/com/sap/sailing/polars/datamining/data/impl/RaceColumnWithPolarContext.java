package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;
import com.sap.sailing.polars.datamining.data.HasRaceColumnPolarContext;

public class RaceColumnWithPolarContext implements HasRaceColumnPolarContext {
    
    private final RaceColumn raceColumn;
    private final HasLeaderboardPolarContext leaderboardPolarContext;

    public RaceColumnWithPolarContext(RaceColumn raceColumn, HasLeaderboardPolarContext leaderboardPolarContext) {
        this.raceColumn = raceColumn;
        this.leaderboardPolarContext = leaderboardPolarContext;
    }

    @Override
    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

    @Override
    public HasLeaderboardPolarContext getLeaderboardPolarContext() {
        return leaderboardPolarContext;
    }

}
