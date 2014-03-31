package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.LeaderboardGroupWithContext;

public class LeaderboardGroupWithContextImpl implements LeaderboardGroupWithContext {
    
    private HasLeaderboardGroupContext context;

    public LeaderboardGroupWithContextImpl(HasLeaderboardGroupContext context) {
        this.context = context;
    }

    @Override
    public String getLeaderboardGroupName() {
        return context.getLeaderboardGroup().getName();
    }

}
