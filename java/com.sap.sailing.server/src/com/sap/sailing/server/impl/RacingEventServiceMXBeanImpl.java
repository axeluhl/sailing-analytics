package com.sap.sailing.server.impl;

import java.util.Map.Entry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.RacingEventServiceMXBean;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RacingEventServiceMXBeanImpl implements RacingEventServiceMXBean {
    private final RacingEventService racingEventService;

    protected RacingEventServiceMXBeanImpl(RacingEventService racingEventService) {
        super();
        this.racingEventService = racingEventService;
    }
    
    private RacingEventService getRacingEventService() {
        return racingEventService;
    }

    @Override
    public int getNumberOfLeaderboards() {
        return getRacingEventService().getLeaderboards().size();
    }

    @Override
    public long getNumberOfTrackedRacesToRestore() {
        return getRacingEventService().getNumberOfTrackedRacesToRestore();
    }

    @Override
    public int getNumberOfTrackedRacesRestored() {
        return getRacingEventService().getNumberOfTrackedRacesRestored();
    }
    
    @Override
    public ObjectName[] getLeaderboards() throws MalformedObjectNameException {
        final ObjectName[] result = new ObjectName[getRacingEventService().getLeaderboards().size()];
        int i=0;
        for (final Entry<String, Leaderboard> entry : getRacingEventService().getLeaderboards().entrySet()) {
            result[i++] = new LeaderboardMXBeanImpl(entry.getValue()).getObjectName();
        }
        return result;
    }
}
