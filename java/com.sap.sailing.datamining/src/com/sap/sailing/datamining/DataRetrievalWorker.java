package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public interface DataRetrievalWorker<DataType> extends ComponentWorker<Collection<DataType>> {

    public void setGroup(LeaderboardGroup group);

}
