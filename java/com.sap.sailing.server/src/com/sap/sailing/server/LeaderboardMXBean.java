package com.sap.sailing.server;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public interface LeaderboardMXBean {
    ObjectName getObjectName() throws MalformedObjectNameException;
}
