package com.sap.sailing.datamining;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public interface SingleThreadedDataRetriever<DataType> extends Cloneable, Runnable {

    public SingleThreadedDataRetriever<DataType> clone();

    public boolean isDone();

    public void setGroup(LeaderboardGroup group);

    public void setReceiver(DataReceiver<DataType> receiver);

}
