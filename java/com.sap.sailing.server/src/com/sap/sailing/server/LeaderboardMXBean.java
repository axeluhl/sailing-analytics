package com.sap.sailing.server;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sse.common.Named;

public interface LeaderboardMXBean extends Named {
    public static interface ComputationTimeAverage {
        int getNumberOfComputations();
        long getAverageComputeDurationInMillis();
        long getAverageRangeInMillis();
    }
    
    ObjectName getObjectName() throws MalformedObjectNameException;
    int getNumberOfCompetitors();
    int getNumberOfAllCompetitors();
    String getDisplayName();
    String getType();
    long getDelayToLiveInMillis();
    String getBoatClass();
    ComputationTimeAverage[] getComputationTimeAverages();
}
