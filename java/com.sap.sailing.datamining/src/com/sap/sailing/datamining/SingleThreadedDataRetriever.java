package com.sap.sailing.datamining;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public interface SingleThreadedDataRetriever<DataType> extends ComponentWorker {

    public void setGroup(LeaderboardGroup group);

    public void setReceiver(DataReceiver<DataType> receiver);

}
