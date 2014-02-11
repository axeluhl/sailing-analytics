package com.sap.sailing.datamining.builders;

import com.sap.sailing.datamining.impl.gps_fix.GPSFixLeaderboardGroupDataRetrievalWorker;
import com.sap.sailing.datamining.impl.tracked_leg_of_competitor.TrackedLegOfCompetitorLeaderboardGroupDataRetrievalWorker;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.datamining.workers.DataRetrievalWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public class DataRetrieverWorkerBuilder<DataType> implements WorkerBuilder<DataRetrievalWorker<LeaderboardGroup, DataType>> {

    private final DataTypes dataType;

    public DataRetrieverWorkerBuilder(DataTypes dataType) {
        this.dataType = dataType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public DataRetrievalWorker<LeaderboardGroup, DataType> build() {
        switch (dataType) {
        case GPSFix:
            return (DataRetrievalWorker<LeaderboardGroup, DataType>) new GPSFixLeaderboardGroupDataRetrievalWorker();
        case TrackedLegOfCompetitor:
            return (DataRetrievalWorker<LeaderboardGroup, DataType>) new TrackedLegOfCompetitorLeaderboardGroupDataRetrievalWorker();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: " + dataType.toString());
    }

}
