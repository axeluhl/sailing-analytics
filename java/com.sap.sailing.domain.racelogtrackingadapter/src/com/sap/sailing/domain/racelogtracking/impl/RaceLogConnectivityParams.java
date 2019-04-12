package com.sap.sailing.domain.racelogtracking.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLog;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceInformationFinder;
import com.sap.sailing.domain.abstractlog.race.tracking.analyzing.impl.RaceLogTrackingStateAnalyzer;
import com.sap.sailing.domain.abstractlog.regatta.RegattaLog;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.racelog.tracking.DoesNotHaveRegattaLogException;
import com.sap.sailing.domain.common.racelog.tracking.RaceNotCreatedException;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroupResolver;
import com.sap.sailing.domain.regattalike.HasRegattaLike;
import com.sap.sailing.domain.tracking.DynamicTrackedRegatta;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.impl.AbstractRaceTrackingConnectivityParameters;
import com.sap.sailing.server.interfaces.RacingEventService;

public class RaceLogConnectivityParams extends AbstractRaceTrackingConnectivityParameters {
    /**
     * A type identifier that needs to be unique for the 
     */
    public static final String TYPE = "RACE_LOG_TRACKING";
    
    private final RacingEventService service;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final Leaderboard leaderboard;
    private final long delayToLiveInMillis;
    private final Regatta regatta;
    private final DomainFactory domainFactory;

    public RaceLogConnectivityParams(RacingEventService service, Regatta regatta, RaceColumn raceColumn, Fleet fleet,
            Leaderboard leaderboard, long delayToLiveInMillis, DomainFactory domainFactory, boolean trackWind,
            boolean correctWindDirectionByMagneticDeclination) throws RaceNotCreatedException {
        super(trackWind, correctWindDirectionByMagneticDeclination);
        this.service = service;
        this.regatta = regatta;
        this.raceColumn = raceColumn;
        this.fleet = fleet;
        this.leaderboard = leaderboard;
        this.delayToLiveInMillis = delayToLiveInMillis;
        this.domainFactory = domainFactory;
        if (!new RaceLogTrackingStateAnalyzer(getRaceLog()).analyze().isForTracking()) {
            throw new RaceNotCreatedException(String.format("Racelog (%s) is not denoted for tracking", getRaceLog()));
        }
    }

    @Override
    public String getTypeIdentifier() {
        return TYPE;
    }

    @Override
    public RaceTracker createRaceTracker(TrackedRegattaRegistry trackedRegattaRegistry, WindStore windStore,
            RaceLogResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds) {
        return createRaceTracker(regatta, trackedRegattaRegistry, windStore, raceLogResolver, leaderboardGroupResolver, timeoutInMilliseconds);
    }

    @Override
    public RaceTracker createRaceTracker(Regatta regatta, TrackedRegattaRegistry trackedRegattaRegistry,
            WindStore windStore, RaceLogResolver raceLogResolver, LeaderboardGroupResolver leaderboardGroupResolver, long timeoutInMilliseconds) {
        if (regatta == null) {
            BoatClass boatClass = new RaceInformationFinder(getRaceLog()).analyze().getBoatClass();
            regatta = service.getOrCreateDefaultRegatta(
                    RegattaImpl.getDefaultName("RaceLog-tracking default Regatta", boatClass.getName()),
                    boatClass.getName(), UUID.randomUUID());
        }
        if (regatta == null) {
            throw new RaceNotCreatedException("No regatta for race-log tracked race");
        }
        DynamicTrackedRegatta trackedRegatta = trackedRegattaRegistry.getOrCreateTrackedRegatta(regatta);
        return new RaceLogRaceTracker(trackedRegatta, this, windStore, raceLogResolver, this, trackedRegattaRegistry);
    }

    @Override
    public Object getTrackerID() {
        return getRaceLog().getId();
    }

    @Override
    public long getDelayToLiveInMillis() {
        return delayToLiveInMillis;
    }

    public RaceLog getRaceLog() {
        return raceColumn.getRaceLog(fleet);
    }

    public RaceColumn getRaceColumn() {
        return raceColumn;
    }

    public Fleet getFleet() {
        return fleet;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    public RegattaLog getRegattaLog() throws DoesNotHaveRegattaLogException {
        if (leaderboard instanceof HasRegattaLike) {
            return (((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog());
        } else {
            throw new DoesNotHaveRegattaLogException();
        }
    }

    public RacingEventService getService() {
        return service;
    }
    
    public DomainFactory getDomainFactory() {
        return domainFactory;
    }
    
    public List<AbstractLog<?, ?>> getLogHierarchy() {
        List<AbstractLog<?, ?>> result = new ArrayList<>();
        result.add(getRaceLog());
        if (leaderboard instanceof HasRegattaLike) {
            result.add(((HasRegattaLike) leaderboard).getRegattaLike().getRegattaLog());
        }
        return result;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName()+" for "+leaderboard.getName()+"/"+raceColumn.getName()+"/"+fleet.getName();
    }
}
