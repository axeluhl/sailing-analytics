package com.sap.sailing.racecommittee.app.data.handlers;

import com.sap.sailing.racecommittee.app.data.OnlineDataManager;
import com.sap.sailing.racecommittee.app.domain.impl.LeaderboardResult;

public class LeaderboardResultDataHandler extends DataHandler<LeaderboardResult> {

    public LeaderboardResultDataHandler(OnlineDataManager manager) {
        super(manager);
    }

    @Override
    public void onResult(LeaderboardResult data, boolean isCached) {

    }
}
