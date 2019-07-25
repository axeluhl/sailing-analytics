package com.sap.sailing.server;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

import com.sap.sse.common.Named;

public interface LeaderboardMXBean extends Named {
    ObjectName getObjectName() throws MalformedObjectNameException;
    int getNumberOfCompetitors();
    int getNumberOfAllCompetitors();
    String getDisplayName();
    String getType();
    long getDelayToLiveInMillis();
    String getBoatClass();
    TabularData getComputationTimeAverages();
}
